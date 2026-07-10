# Trustbuddy API

Backend REST API for the multi-step insurance quote flow (Onboarding team code challenge). Built with Spring Boot 4.1, hexagonal architecture, PostgreSQL, Redis, and Kafka.

## Status

The quote capability is **feature-complete** for the challenge scope:

- Domain model, premium calculation, and state transitions
- PostgreSQL persistence (JPA) with optimistic locking
- REST API under `/quotes` (create, update coverage, submit, get, list)
- JWT authentication (`POST /auth/token`) on all quote endpoints
- Redis quote cache (read-through get, eviction on save)
- Kafka `quote-submitted` events on first successful submit
- Scheduled draft expiration (`DRAFT` → `EXPIRED`)
- CORS for the React frontend origin
- Global error handling, Bean Validation, Checkstyle, SpotBugs, JaCoCo

See [BUILD_JOURNEY.md](BUILD_JOURNEY.md) for how this was delivered in **14 incremental phases** (plan, timeline, and what each phase produced).

## Tech stack

| Area | Choice |
|------|--------|
| Language | Java 17 |
| Framework | Spring Boot 4.1 |
| Build | Maven (`./mvnw`) |
| Persistence | Spring Data JPA + PostgreSQL |
| Cache | Redis |
| Messaging | Kafka |
| Auth | JWT (stateless) |
| API docs | springdoc OpenAPI (Swagger UI) |
| Architecture | Hexagonal (ports & adapters) |
| Observability | Spring Actuator + Micrometer |

## Prerequisites

- Java 17+
- Docker (for PostgreSQL, Redis, Kafka via `make infra-up`)
- `make` (optional; wraps Maven commands)

## Quick start

```bash
cp .env.example .env          # optional — dev profile has defaults
make infra-up                 # PostgreSQL, Redis, Kafka
make run-dev                  # infra + API (dev profile)
make token                    # obtain JWT for API calls
make health                   # check actuator health
make swagger-url              # print Swagger UI URL
```

### Verify

```bash
make test          # unit and integration tests (Docker required for Testcontainers)
make verify        # compile + test + Checkstyle + SpotBugs
make verify-all    # same as verify (includes JaCoCo report)
make coverage      # verify + print path to JaCoCo HTML report
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
make kafka-consume # tail quote-submitted topic (local Docker Kafka)
```

### Configuration

| File | Purpose |
|------|---------|
| `application.yml` | Universal defaults; requires env vars for infra and secrets |
| `application-dev.yml` | Localhost defaults for host JVM (`make run`) |
| `application-docker.yml` | Compose service hostnames (`make stack-up`) |
| `application-prod.yml` | Strict production settings; Swagger disabled |

Local dev only needs `.env` values documented in `.env.example`. Production must set `DATABASE_URL`, `REDIS_HOST`, `KAFKA_BOOTSTRAP_SERVERS`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, etc.

## Architecture

Feature-oriented **hexagonal architecture** under `com.trustbuddy.api` — domain at the center, infrastructure via ports and adapters.

```
quote/
  domain/           # Quote, premium/state logic (no Spring/JPA)
  application/      # QuoteService, QuoteSubmissionService, ports
  infrastructure/   # REST, JPA, Redis, Kafka, HTTP gateway, scheduler
config/             # security, CORS, OpenAPI, metrics, shared beans
```

Full diagrams, port table, and layer rules: [ARCHITECTURE.md](ARCHITECTURE.md).

**Design choices:**

- **Immutable `Quote`** with value objects (`PersonalInfo`, `CoverageDetails`, `QuoteAudit`) for clarity and safe state transitions
- **Repository decorator** (`CachingQuoteRepositoryAdapter`) centralizes cache eviction on every persist
- **Idempotent submit** when quote is already `SUBMITTED`; Kafka event only on first success
- **Real insurer gateway** HTTP client (default `https://httpstat.us/200`), not an in-memory mock
- API paths at `/quotes` per challenge spec (documented deviation from `/api/v1/...` internal convention)

## API

Base URL when running locally: `http://localhost:8080`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/auth/token` | — | Obtain JWT (`username`, `password`) |
| `POST` | `/quotes` | Bearer | Create draft quote |
| `PATCH` | `/quotes/{id}/coverage` | Bearer | Set coverage and health answers; recalculates premium |
| `POST` | `/quotes/{id}/submit` | Bearer | Submit to external insurer gateway |
| `GET` | `/quotes/{id}` | Bearer | Get quote by id |
| `GET` | `/quotes` | Bearer | List quotes (`page`, `size`, `sort`) |

**Submit** requires personal info, coverage, and health answers. For age > 65, pre-existing condition fields are required. On gateway failure the quote becomes `SUBMISSION_FAILED` and can be resubmitted. **Expired** drafts return **409** on submit.

**Insurer gateway** — configure with `INSURER_GATEWAY_URL` (dev default `https://httpstat.us/200`).

Interactive docs (dev/docker profiles):

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Metrics

Actuator exposes health, info, and metrics. Custom counters (Micrometer):

| Metric | When incremented |
|--------|------------------|
| `quote.submissions.total` | First successful submit |
| `quote.submissions.failed` | Insurer gateway failure |
| `quote.expired.total` | Drafts expired by scheduled job |

Example: `GET /actuator/metrics/quote.submissions.total` (when API is running).

## Testing

```bash
make test
make test-one TEST=QuoteSubmissionServiceTest
make test-one TEST='*Premium*'
make verify-all
```

Tests use **given_when_then** naming and Given/When/Then structure — see [AGENTS.md](AGENTS.md).

## Frontend

This API pairs with a separate React frontend repository. Add the sibling repo link here when available.

## AI-assisted development

Built with human-reviewed AI assistance (Cursor Agent). Phase plan, conventions, and delivery narrative: [BUILD_JOURNEY.md](BUILD_JOURNEY.md). Contributor rules: [AGENTS.md](AGENTS.md).
