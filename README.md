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
| Observability | Spring Actuator + Micrometer + Sentry (errors) |

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
make format        # apply Spotless formatting (Java sources)
make precommit     # format staged Java files only and re-stage
make verify        # compile + test + Checkstyle + SpotBugs (+ JaCoCo report)
make coverage      # tests + JaCoCo report only (skips Checkstyle/SpotBugs)
```

See [Static analysis](#static-analysis) for tool configuration, individual commands, and CI integration.

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
- **Real insurer gateway** HTTP client (default `https://tools-httpstatus.pickup-services.com/200`), not an in-memory mock
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

**Submit** requires personal info, coverage, and health answers. For age > 65, pre-existing condition fields are required. On gateway failure the quote becomes `SUBMISSION_FAILED` and can be resubmitted. **Expired** or **incomplete** drafts return **409** on submit.

**Insurer gateway** — configure with `INSURER_GATEWAY_URL` (dev default `https://tools-httpstatus.pickup-services.com/200`).

Interactive docs (dev/docker profiles):

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Committed contract export for frontend codegen: [`openapi/openapi.json`](openapi/openapi.json). Regenerate with `make openapi-export` while the API is running (`make run-dev`). Drift is checked by `OpenApiSpecDriftTest` in CI (`make openapi-drift` locally).

### Metrics

Actuator exposes health, info, and metrics. Custom counters (Micrometer):

| Metric | When incremented |
|--------|------------------|
| `quote.submissions.total` | First successful submit |
| `quote.submissions.failed` | Insurer gateway failure |
| `quote.expired.total` | Drafts expired by scheduled job |

Example: `GET /actuator/metrics/quote.submissions.total` (when API is running).

### Error reporting (Sentry)

Unexpected server errors (500) and insurer gateway failures (502) are reported to Sentry when enabled. Expected client errors (4xx) are not sent.

Configure via environment variables (see [`.env.example`](.env.example)):

| Variable | Purpose |
|----------|---------|
| `SENTRY_DSN` | Project DSN from Sentry.io |
| `SENTRY_ENVIRONMENT` | e.g. `development`, `production` |
| `SENTRY_ENABLED` | `true` to send events (default `false`) |

Use separate Sentry projects for local dev and production. Tests and CI run with Sentry disabled.

## Testing

```bash
make test
make test-one TEST=QuoteSubmissionServiceTest
make test-one TEST='*Premium*'
make coverage
```

Tests use **given_when_then** naming and Given/When/Then structure — see [AGENTS.md](AGENTS.md).

## Static analysis

Code quality is enforced with Maven plugins bound to the **`verify`** lifecycle phase. GitHub Actions runs `./mvnw verify` on every push and pull request to `main` (see `.github/workflows/pr-validation.yml`).

| Tool | What it checks | When it runs |
|------|----------------|--------------|
| [Checkstyle](https://checkstyle.org/) | Style, naming, imports, complexity | `verify` |
| [SpotBugs](https://spotbugs.github.io/) | Bug patterns in compiled bytecode | `verify` |
| [Error Prone](https://errorprone.info/) | Likely bugs and bad idioms at compile time | `compile` |
| [JaCoCo](https://www.jacoco.org/) | Test coverage report (not static analysis) | `make coverage` or `verify` |

### Commands

```bash
make lint          # Checkstyle + SpotBugs only (skips tests)
make format        # apply Spotless formatting (Java sources)
make precommit     # format staged Java files only and re-stage
make verify        # compile + test + Checkstyle + SpotBugs (+ JaCoCo report)
make coverage      # tests + JaCoCo report only (skips Checkstyle/SpotBugs)
```

Equivalent Maven invocations:

```bash
./mvnw spotless:apply                      # format Java sources
./mvnw spotless:check                      # check formatting (no changes)
./mvnw checkstyle:check spotbugs:check   # lint only
./mvnw verify                              # full gate
./mvnw test jacoco:report                  # coverage only
./mvnw compile                             # includes Error Prone
```

Run `make format` to format the whole tree, or `make precommit` to format **staged Java files only** (same logic as the git pre-commit hook). On a local git checkout (not CI or Docker builds), hooks are installed automatically on the first Maven build via [git-build-hook-maven-plugin](https://github.com/rudikershaw/git-build-hook). Hook script: [`.githooks/pre-commit`](.githooks/pre-commit).

Use `make lint` for a faster feedback loop when fixing style or SpotBugs findings; use `make coverage` when you only need a coverage report; run `make verify` before opening a PR.

### Spotless (formatting)

Configuration: [`pom.xml`](pom.xml) (`spotless-maven-plugin`).

Formats **main and test** Java sources with Google Java Format (AOSP style), then converts indentation to **tabs** to match project conventions. Also removes unused imports, trims trailing whitespace, and ensures a final newline.

Not enforced in CI or `verify` — run `make format` locally (or `./mvnw spotless:check` to verify without applying).

### Checkstyle

Configuration: [`config/checkstyle/checkstyle.xml`](config/checkstyle/checkstyle.xml) (Checkstyle 10.x via `maven-checkstyle-plugin`).

Applies to **main and test** sources. Notable rules:

- **Imports** — no unused, redundant, star, or illegal imports
- **Naming** — package, type, method, parameter, member, and constant conventions
- **Structure** — one public type per file (`OuterTypeFilename`), braces required, one statement per line, no empty catch blocks
- **Complexity** — cyclomatic complexity ≤ 10; methods ≤ 100 lines; ≤ 7 parameters

Violations fail the build (`failsOnError=true`).

### SpotBugs

Configuration: [`config/spotbugs/exclude-filter.xml`](config/spotbugs/exclude-filter.xml) (`spotbugs-maven-plugin`, effort **Max**, threshold **Low**).

Analyzes compiled classes for common defect patterns (null dereferences, resource leaks, bad equality, etc.). The exclude filter suppresses `EI_EXPOSE_REP` / `EI_EXPOSE_REP2` on JPA entities under `*.entity.*`, where mutable collection/date exposure is accepted for persistence mapping.

HTML report (when generated): `target/spotbugs.html`.

### Error Prone

Configured on `maven-compiler-plugin` in [`pom.xml`](pom.xml) as a compiler annotation processor (`error_prone_core` 2.42.x). Runs on every **`compile`** — including IDE builds that invoke the Maven compiler — and flags issues such as ambiguous overloads, ignored return values, and discouraged APIs.

There is no separate `make` target; fix Error Prone warnings during compilation before they reach `verify`.

### JaCoCo (coverage)

JaCoCo instruments tests via the `prepare-agent` goal and writes an HTML report to `target/site/jacoco/index.html`. Use `make coverage` to run **tests only** (no Checkstyle or SpotBugs) and print the report path. A report is also generated during `make verify`. Coverage is reported for visibility; there is no enforced minimum threshold in the build.

## Extras

Beyond the challenge’s core quote flow, a few tools were added deliberately to improve **operability**, **contract sharing**, and **developer experience**. Each is optional in local dev but pays off as the API grows and more clients integrate.

### Sentry (error monitoring)

Actuator metrics tell you *that* something failed; they do not give stack traces, request context, or alerting. [Sentry](https://sentry.io/) fills that gap for **unexpected 500s** and **operational failures** such as insurer gateway **502** responses — without noise from expected client errors (4xx).

Why Sentry here:

- **Actionable alerts** — production issues surface with stack traces and environment tags instead of digging through logs alone.
- **Scoped reporting** — only unexpected and operational errors are sent; validation and auth failures stay client-side.
- **Safe by default** — disabled in tests/CI; sensitive headers and fields are scrubbed before events leave the app.
- **Decoupled design** — handlers depend on an `ErrorReporterPort`, not the Sentry SDK directly, so the core stays testable.

Setup and env vars: [Error reporting (Sentry)](#error-reporting-sentry).

### OpenAPI / Swagger (API contract)

The REST surface is the integration boundary for the React frontend, Postman collections, and future consumers. [springdoc OpenAPI](https://springdoc.org/) generates a live spec from code and serves Swagger UI in dev/docker profiles.

Why OpenAPI here:

- **Single source of truth** — controllers stay annotated; the spec is derived from the running app, not a hand-maintained duplicate.
- **Easier client tooling** — import `/v3/api-docs` or the committed [`openapi/openapi.json`](openapi/openapi.json) into Postman; frontend teams can generate typed clients from the same file.
- **Drift prevention** — `OpenApiSpecDriftTest` in CI fails if the exported contract falls behind code (`make openapi-drift`).
- **Interactive exploration** — Swagger UI (`make swagger-url`) for manual testing alongside JWT auth.

Regenerate the committed export with `make openapi-export` while the API is running.

## Frontend

This API pairs with a separate React frontend repository. Add the sibling repo link here when available.

## AI-assisted development

Built with human-reviewed AI assistance (Cursor Agent). Phase plan, conventions, and delivery narrative: [BUILD_JOURNEY.md](BUILD_JOURNEY.md). Contributor rules: [AGENTS.md](AGENTS.md).
