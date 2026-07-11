.DEFAULT_GOAL := help

MVN := ./mvnw
RUN_PROFILE := dev
DOCKER_IMAGE := trustbuddy-api:local
COMPOSE := docker compose
API_PORT ?= 8080

# Load .env when present; export only uncommented KEY= assignments
ifneq (,$(wildcard .env))
include .env
export $(shell grep -E '^[A-Za-z_][A-Za-z0-9_]*=' .env | cut -d= -f1)
endif

.PHONY: help compile test test-one verify lint format precommit openapi-export openapi-drift run run-dev token health swagger-url \
	infra-up infra-down infra-logs infra-reset docker-build stack-up stack-down stack-logs \
	kafka-consume coverage test-state test-submit

help: ## Show available targets
	@echo "Trustbuddy API"
	@echo ""
	@echo "Build:"
	@echo "  compile        Compile sources"
	@echo "  format         Apply Spotless formatting to Java sources"
	@echo "  precommit      Format staged Java via full-tree Spotless (pre-commit hook)"
	@echo "  openapi-export Write openapi/openapi.json from running API"
	@echo "  openapi-drift  Check committed OpenAPI spec matches springdoc"
	@echo "  lint           Run Spotless, Checkstyle, and SpotBugs"
	@echo ""
	@echo "Test:"
	@echo "  test           Run unit and integration tests"
	@echo "  test-one       Run tests matching TEST (Surefire -Dtest pattern)"
	@echo "  test-state     Run quote state transition unit tests"
	@echo "  test-submit    Run quote submission application tests"
	@echo "  verify         Compile, test, and static analysis"
	@echo "  coverage       Run tests + JaCoCo report (skips Checkstyle/SpotBugs)"
	@echo ""
	@echo "Infrastructure:"
	@echo "  infra-up       Start PostgreSQL, Redis, and Kafka (Docker)"
	@echo "  infra-down     Stop infrastructure containers"
	@echo "  infra-logs     Tail infrastructure container logs"
	@echo "  infra-reset    Stop infrastructure and remove volumes"
	@echo "  docker-build   Build API Docker image"
	@echo "  stack-up       Build and start full stack (API + infra) in Docker"
	@echo "  stack-down     Stop full stack including API"
	@echo "  stack-logs     Tail logs for all services including API"
	@echo "  kafka-consume  Tail quote-submitted Kafka topic locally"
	@echo ""
	@echo "Development:"
	@echo "  run            Run API locally (dev profile; requires make infra-up)"
	@echo "  run-dev        Start infra, then run API with dev profile"
	@echo "  token          Obtain JWT from running API"
	@echo "  health         Check actuator health endpoint"
	@echo "  swagger-url    Print local Swagger UI URL"

compile: ## Compile sources
	$(MVN) compile -q

format: ## Apply Spotless formatting to Java sources
	$(MVN) spotless:apply -q

precommit: ## Format staged Java files and re-stage them (same as pre-commit hook)
	@bash .githooks/pre-commit

openapi-export: ## Write openapi/openapi.json from running API (requires API on localhost)
	@mkdir -p openapi
	@curl -sf "http://localhost:$(API_PORT)/v3/api-docs" | python3 -m json.tool > openapi/openapi.json
	@echo "OpenAPI spec: $$(pwd)/openapi/openapi.json"

openapi-drift: ## Check committed OpenAPI spec matches springdoc (requires Docker)
	$(MVN) test -Dtest=OpenApiSpecDriftTest -Dsurefire.exitTimeout=5 -q
	@echo "OpenAPI spec drift check passed (openapi/openapi.json matches springdoc)"

test: ## Run unit and integration tests
	$(MVN) test -q

# Surefire -Dtest patterns: ClassName, *Premium*, pkg.**.*Test, Class#method
# Examples: make test-one TEST=QuoteSubmissionServiceTest
#           make test-one TEST='*Premium*'
#           make test-one TEST='com.trustbuddy.api.quote.domain.service.*'
test-one: ## Run tests matching TEST (Surefire -Dtest pattern)
	@test -n "$(TEST)" || (echo "Usage: make test-one TEST=QuoteSubmissionServiceTest" && exit 1)
	$(MVN) test -Dtest="$(TEST)" -q

test-state: ## Run quote state transition unit tests
	$(MVN) test -Dtest="com.trustbuddy.api.quote.domain.service.QuoteStateTransitionServiceTest" -q

test-submit: ## Run quote submission application tests
	$(MVN) test -Dtest="com.trustbuddy.api.quote.application.service.QuoteSubmissionServiceTest" -q

verify: ## Compile, test, and static analysis
	$(MVN) verify -q

coverage: ## Run tests + JaCoCo report (skips Checkstyle/SpotBugs)
	$(MVN) test jacoco:report -q
	@echo "JaCoCo report: $$(pwd)/target/site/jacoco/index.html"

lint: ## Run Spotless, Checkstyle, and SpotBugs (also runs during verify)
	$(MVN) spotless:check checkstyle:check spotbugs:check -q

run: ## Run API locally (dev profile; requires make infra-up)
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=$(RUN_PROFILE)

run-dev: infra-up ## Start infra, then run API with dev profile
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=$(RUN_PROFILE)

token: ## Obtain JWT from running API (uses AUTH_USERNAME / AUTH_PASSWORD from .env)
	@curl -s -X POST http://localhost:$(API_PORT)/api/v1/auth/token \
		-H "Content-Type: application/json" \
		-d '{"username":"$${AUTH_USERNAME:-dev-user}","password":"$${AUTH_PASSWORD:-dev-password}"}' \
		| python3 -m json.tool

health: ## Check actuator health endpoint
	@curl -sf "http://localhost:$(API_PORT)/actuator/health" | python3 -m json.tool

swagger-url: ## Print local Swagger UI URL
	@echo "http://localhost:$(API_PORT)/swagger-ui.html"

infra-up: ## Start PostgreSQL, Redis, and Kafka (Docker)
	$(COMPOSE) up -d postgres redis kafka

infra-down: ## Stop infrastructure containers
	$(COMPOSE) down

infra-logs: ## Tail infrastructure container logs
	$(COMPOSE) logs -f postgres redis kafka

kafka-consume: ## Tail quote-submitted Kafka topic locally
	@docker exec trustbuddy-kafka /opt/kafka/bin/kafka-console-consumer.sh \
		--bootstrap-server localhost:9092 \
		--topic $${KAFKA_TOPIC:-quote-submitted} \
		--from-beginning

infra-reset: ## Stop infrastructure and remove volumes
	$(COMPOSE) down -v

docker-build: ## Build API Docker image
	docker build -t $(DOCKER_IMAGE) .

stack-up: ## Build and start full stack (API + infra) in Docker
	$(COMPOSE) up -d --build

stack-down: ## Stop full stack including API
	$(COMPOSE) down

stack-logs: ## Tail logs for all services including API
	$(COMPOSE) logs -f
