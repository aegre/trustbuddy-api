.DEFAULT_GOAL := help

MVN := ./mvnw
RUN_PROFILE := dev
DOCKER_IMAGE := trustbuddy-api:local
COMPOSE := docker compose

# Load .env when present; export only .env keys (not Make internals)
ifneq (,$(wildcard .env))
include .env
export $(shell sed -n 's/=.*//p' .env)
endif

.PHONY: help compile test test-one verify lint run run-dev token infra-up infra-down infra-logs infra-reset docker-build stack-up stack-down stack-logs

help: ## Show available targets
	@echo "Trustbuddy API — available targets:"
	@echo ""
	@grep -E '^[a-zA-Z0-9_.-]+:.*##' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*## "}; {printf "  \033[36m%-14s\033[0m %s\n", $$1, $$2}'

compile: ## Compile sources
	$(MVN) compile -q

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

lint: ## Run Checkstyle and SpotBugs (also runs during verify)
	$(MVN) checkstyle:check spotbugs:check -q

run: ## Run API locally (dev profile; requires make infra-up)
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=$(RUN_PROFILE)

run-dev: infra-up ## Start infra, then run API with dev profile
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=$(RUN_PROFILE)

token: ## Obtain JWT from running API (uses AUTH_USERNAME / AUTH_PASSWORD from .env)
	@curl -s -X POST http://localhost:8080/auth/token \
		-H "Content-Type: application/json" \
		-d '{"username":"$${AUTH_USERNAME:-dev-user}","password":"$${AUTH_PASSWORD:-dev-password}"}' \
		| python3 -m json.tool

infra-up: ## Start PostgreSQL, Redis, and Kafka (Docker)
	$(COMPOSE) up -d postgres redis kafka

infra-down: ## Stop infrastructure containers
	$(COMPOSE) down

infra-logs: ## Tail infrastructure container logs
	$(COMPOSE) logs -f postgres redis kafka

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
