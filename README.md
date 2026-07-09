# Trustbuddy API

Backend REST API for the multi-step insurance quote flow (Onboarding team code challenge). Built with Spring Boot 4.1, hexagonal architecture, PostgreSQL, Redis, and Kafka.

## Status

Early development — Phase 1 foundation complete (deps, config, Docker, tests). Quote domain and API endpoints start in Phase 2.

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

### Verify foundation (Phase 1)

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

Hexagonal structure under `com.trustbuddy.api`:

```
domain/           # Business rules (pure Java)
application/      # Use cases + outbound port interfaces
adapter/in/       # REST controllers, security, schedulers
adapter/out/      # JPA, HTTP gateway, Kafka, Redis
config/           # Spring bean wiring
```

See [AGENTS.md](AGENTS.md) for full conventions.

## API (planned)

Challenge contract uses `/quotes` paths:

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/quotes` | Create draft quote |
| `PATCH` | `/quotes/{id}/coverage` | Set coverage + recalculate premium |
| `POST` | `/quotes/{id}/submit` | Submit to external insurer API |
| `GET` | `/quotes/{id}` | Get quote (cached) |
| `GET` | `/quotes` | List all quotes |

Swagger UI (when running): `http://localhost:8080/swagger-ui.html`

## Testing

```bash
make test
# Coverage report (after JaCoCo wiring):
./mvnw verify
# open target/site/jacoco/index.html
```

## Frontend

This API is designed to pair with a separate React frontend repository. Link to the sibling repo will be added here once available.
