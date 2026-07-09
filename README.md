# Trustbuddy API

Backend REST API for the multi-step insurance quote flow (Onboarding team code challenge). Built with Spring Boot 4.1, hexagonal architecture, PostgreSQL, Redis, and Kafka.

## Status

Quote capability is in progress. Implemented today:

- Domain model, premium calculation, and state transitions
- PostgreSQL persistence (JPA)
- REST API under `/quotes` (create, update coverage, submit, get, list)
- Global error handling and request validation
- External insurer submission via configurable HTTP gateway ([httpstat.us](https://httpstat.us) by default)

Still planned: JWT authentication, Redis quote cache, Kafka submit events, draft expiration job.

## Tech stack

| Area | Choice |
|------|--------|
| Language | Java 17 |
| Framework | Spring Boot 4.1 |
| Build | Maven (`./mvnw`) |
| Persistence | Spring Data JPA + PostgreSQL |
| Cache | Redis |
| Messaging | Kafka |
| Auth | JWT |
| API docs | springdoc OpenAPI (Swagger UI) |
| Architecture | Hexagonal (ports & adapters) |

## Prerequisites

- Java 17+
- Docker (for PostgreSQL, Redis, Kafka via `make infra-up`)
- `make` (optional; wraps Maven commands)

## Quick start

```bash
# Copy environment template (optional — dev profile has defaults)
cp .env.example .env

# Start infrastructure (PostgreSQL, Redis, Kafka)
make infra-up

# List available commands
make help

# Compile
make compile

# Run tests
make test

# Run API (dev profile; requires make infra-up)
make run

# Or start infra and API together
make run-dev
```

### Verify

```bash
make verify   # compile + tests (Testcontainers; requires Docker)
```

### Docker infrastructure

| Service    | Port | Purpose        |
|------------|------|----------------|
| PostgreSQL | 5432 | Quote persistence |
| Redis      | 6379 | Quote cache    |
| Kafka      | 9094 | Submit events (host); `kafka:9092` inside compose network |
| API        | 8080 | REST API (`make stack-up`) |

```bash
make infra-up      # start infra only (for make run on host)
make stack-up      # build and start API + infra in Docker
make stack-logs    # tail all service logs
make stack-down    # stop full stack
make infra-logs    # tail infra logs
make infra-down    # stop infra containers
make infra-reset   # stop and wipe volumes
make docker-build  # build API image only (trustbuddy-api:local)
```

### Configuration

| File | Purpose |
|------|---------|
| `application.yml` | Universal defaults; requires env vars for infra and secrets |
| `application-dev.yml` | Localhost defaults for host JVM (`make run`) |
| `application-docker.yml` | Compose service hostnames (`make stack-up`) |
| `application-prod.yml` | Strict production settings; Swagger disabled |

Local dev only needs `.env` values documented in `.env.example`. Production must set `DATABASE_URL`, `REDIS_HOST`, `KAFKA_BOOTSTRAP_SERVERS`, `JWT_SECRET`, etc.

## Project layout

Feature-oriented hexagonal architecture under `com.trustbuddy.api` — see [ARCHITECTURE.md](ARCHITECTURE.md) for full detail.

```
TrustbuddyApiApplication.java
config/                          # shared Spring configuration

quote/                           # quote capability
  application/port/              # outbound ports (repository, insurer gateway)
  application/service/           # QuoteService, QuoteSubmissionService
  domain/model/                  # Quote, enums, premium/state logic
  infrastructure/web/            # REST controllers, DTOs, exception handling
  infrastructure/persistence/    # JPA entities, adapters, repositories
  infrastructure/client/         # InsurerGatewayHttpAdapter (httpstat.us)
```

See [AGENTS.md](AGENTS.md) for REST conventions and agent instructions.

## API

Base URL when running locally: `http://localhost:8080`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/quotes` | Create draft quote (name, email, age, zip code) |
| `PATCH` | `/quotes/{id}/coverage` | Set coverage type and health answers; returns recalculated premium |
| `POST` | `/quotes/{id}/submit` | Submit completed quote to external insurer gateway |
| `GET` | `/quotes/{id}` | Get quote by id |
| `GET` | `/quotes` | List quotes (paginated; `page`, `size`, `sort`) |

**Submit** requires personal info, coverage selection, and all health answers (`takesPrescriptionMedication`, `usesTobacco`, `needsSpouseCoverage`). For age > 65, pre-existing condition fields are also required. Submit is idempotent when the quote is already `SUBMITTED`. On gateway failure the quote moves to `SUBMISSION_FAILED` and can be resubmitted.

**Insurer gateway** — not a mock. Dev defaults to `https://httpstat.us/200` (`INSURER_GATEWAY_URL` in `.env`). Use paths like `/500` or `/200?sleep=3000` to exercise errors and latency.

Interactive docs (dev/docker profiles):

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

```bash
make test
make test-pricing   # premium calculation unit tests
make test-state     # quote state transition tests
make test-submit    # quote submission service tests
make verify         # compile, test, and static analysis
```

## Frontend

This API is designed to pair with a separate React frontend repository. Link to the sibling repo will be added here once available.
