# trustbuddy-api

Spring Boot REST API for the Trustbuddy insurance quote flow. Pairs with [trustbuddy-frontend](https://github.com/aegre/trustbuddy-frontend).

## Prerequisites

- Java 17+
- `make` (wraps Maven / `./mvnw`)
- Docker + Docker Compose (PostgreSQL, Redis, Kafka, optional full API stack)
- Sibling clone of [trustbuddy-frontend](https://github.com/aegre/trustbuddy-frontend) next to this repo (`../trustbuddy-frontend`) — use `make clone-frontend` if you do not have it yet

## Installation

```bash
cp .env.example .env    # AUTH_USERNAME / AUTH_PASSWORD, JWT_SECRET, CORS, Postgres password
make clone-frontend     # clones ../trustbuddy-frontend if missing
```

For browser login from the Vite SPA and/or the frontend Docker image, set:

```bash
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

Override clone location/URL if needed:

```bash
make clone-frontend FRONTEND_REPO=../trustbuddy-frontend FRONTEND_GIT_URL=https://github.com/aegre/trustbuddy-frontend.git
```

In the frontend repo, also copy `.env.example` → `.env` (`VITE_API_BASE_URL=http://localhost:8080`).

## Run everything in Docker

With the frontend sibling checked out beside this repo:

```bash
make stack-all-up       # API + Postgres/Redis/Kafka + frontend
# API:      http://localhost:8080  (Swagger: /swagger-ui.html)
# Frontend: http://localhost:3000
make stack-all-down     # stop both
make stack-all-logs     # tail API logs (prints hint for frontend logs)
```

If `../trustbuddy-frontend` is missing, `stack-all-up` warns and starts the API stack only.

## Run the API (Docker)

```bash
cp .env.example .env
make stack-up           # API + PostgreSQL + Redis + Kafka
# API: http://localhost:8080
make stack-logs         # tail all service logs
make stack-down         # stop full stack
```

| Service    | Port | Purpose |
|------------|------|---------|
| PostgreSQL | 5432 | Quote persistence |
| Redis      | 6379 | Quote cache |
| Kafka      | 9094 | Submit events (host); `kafka:9092` inside compose network |
| API        | 8080 | REST API |

Host JVM alternative (infra still in Docker):

```bash
make infra-up
make run-dev            # API on http://localhost:8080 (or: make run after infra-up)
make token              # obtain JWT for Postman / curl
make health             # actuator health
make swagger-url        # print Swagger UI URL
```

Other infra helpers:

```bash
make infra-logs         # tail Postgres/Redis/Kafka
make infra-down         # stop infra containers
make infra-reset        # stop and wipe volumes
make docker-build       # build API image only (trustbuddy-api:local)
make kafka-consume      # tail quote-submitted topic (local Docker Kafka)
```

Profiles: `application-dev.yml` for host JVM (`make run` / `make run-dev`), `application-docker.yml` for Compose (`make stack-up`), `application-prod.yml` for production.

## Run the frontend

Typical while iterating (API already up):

```bash
cd ../trustbuddy-frontend
cp .env.example .env    # once
make install            # once
make run                # or make dev → http://localhost:5173
```

Frontend-only Docker (still needs API on `:8080`):

```bash
cd ../trustbuddy-frontend
make stack-up           # http://localhost:3000
```

## Dev login

Local API default: `dev-user` / `dev-password`.

```bash
make token              # POST /api/v1/auth/token → JWT body + HttpOnly cookie
```

## Verify / OpenAPI

```bash
make verify             # compile + test + Spotless + Checkstyle + SpotBugs (+ JaCoCo)
make test               # unit and integration tests (Docker required for Testcontainers)
make test-one TEST=QuoteSubmissionServiceTest
make coverage           # tests + JaCoCo report only
make lint               # Spotless + Checkstyle + SpotBugs (skips tests)
make format             # apply Spotless formatting
```

After contract changes (API must be running):

```bash
make openapi-export     # write openapi/openapi.json from /v3/api-docs
make openapi-drift      # fail if committed spec ≠ live springdoc
```

Then in the frontend repo: `make openapi-update`.

Interactive docs (dev/docker profiles): `http://localhost:8080/swagger-ui.html`

---

# Thought process

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
- Public REST paths versioned under `/api/v1/...` (see [AGENTS.md](AGENTS.md))

## API

Base URL when running locally: `http://localhost:8080`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/auth/token` | — | Obtain JWT (`username`, `password`); sets HttpOnly cookie and returns Bearer token in body |
| `POST` | `/api/v1/auth/logout` | — | Clear access-token cookie (browser clients) |
| `GET` | `/api/v1/auth/me` | JWT | Return authenticated username (validates Bearer header or access-token cookie) |
| `POST` | `/api/v1/quotes` | JWT | Create draft quote |
| `PATCH` | `/api/v1/quotes/{id}` | JWT | Update draft personal info |
| `PATCH` | `/api/v1/quotes/{id}/coverage` | JWT | Set coverage and health answers; recalculates premium |
| `POST` | `/api/v1/quotes/{id}/submit` | JWT | Submit to external insurer gateway |
| `GET` | `/api/v1/quotes/{id}` | JWT | Get quote by id |
| `GET` | `/api/v1/quotes` | JWT | List quotes — `page` (0-based), `size` (values above 100 are capped to 100), `sort=<field>,asc\|desc` (repeat `sort` for multiple fields, e.g. `sort=status,asc&sort=createdAt,desc`; allowed fields: `createdAt`, `updatedAt`, `status`, `name`, `email`, `age`; default `createdAt,desc`) |

**Authentication** — same JWT, two carriers:

| Client | How |
|--------|-----|
| Postman, scripts, Swagger | `Authorization: Bearer <token>` from `POST /api/v1/auth/token` response body |
| Browser frontend | HttpOnly `access_token` cookie set by `/api/v1/auth/token`; send requests with `credentials: 'include'`; call `GET /api/v1/auth/me` to check the session and `POST /api/v1/auth/logout` to clear |

Bearer takes precedence when both are present. Cookie flags: `HttpOnly`, `SameSite=Lax`, `Secure` in production (`JWT_COOKIE_SECURE=true`). Configure via `JWT_COOKIE_NAME`, `JWT_COOKIE_SAME_SITE` in [`.env.example`](.env.example).

**Submit** requires personal info, coverage, and health answers. For age > 65, pre-existing condition fields are required. On gateway failure the quote becomes `SUBMISSION_FAILED` and can be resubmitted. **Expired** or **incomplete** drafts return **409** on submit.

**Insurer gateway** — configure with `INSURER_GATEWAY_URL` (dev default `https://tools-httpstatus.pickup-services.com/200`).

Committed contract export for frontend codegen: [`openapi/openapi.json`](openapi/openapi.json).

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
| [Spotless](https://github.com/diffplug/spotless) | Java formatting (Google Java Format AOSP + tabs) | `verify` |
| [Checkstyle](https://checkstyle.org/) | Style, naming, imports, complexity | `verify` |
| [SpotBugs](https://spotbugs.github.io/) | Bug patterns in compiled bytecode | `verify` |
| [Error Prone](https://errorprone.info/) | Likely bugs and bad idioms at compile time | `compile` |
| [JaCoCo](https://www.jacoco.org/) | Test coverage report (not static analysis) | `make coverage` or `verify` |

### Commands

```bash
make lint          # Spotless + Checkstyle + SpotBugs only (skips tests)
make format        # apply Spotless formatting (Java sources)
make precommit     # run full-tree Spotless when Java is staged (pre-commit hook)
make verify        # compile + test + Spotless + Checkstyle + SpotBugs (+ JaCoCo report)
make coverage      # tests + JaCoCo report only (skips Spotless/Checkstyle/SpotBugs)
```

Equivalent Maven invocations:

```bash
./mvnw spotless:apply                      # format Java sources
./mvnw spotless:check                      # check formatting (no changes)
./mvnw spotless:check checkstyle:check spotbugs:check   # lint only
./mvnw verify                              # full gate
./mvnw test jacoco:report                  # coverage only
./mvnw compile                             # includes Error Prone
```

Run `make format` to format the whole tree. The git pre-commit hook runs **full-tree** Spotless when any Java file is staged (same as `make precommit`), because per-file Spotless can miss wrapping and other cross-file rules. On a local git checkout (not CI or Docker builds), hooks are installed automatically on the first Maven build via [git-build-hook-maven-plugin](https://github.com/rudikershaw/git-build-hook). Hook script: [`.githooks/pre-commit`](.githooks/pre-commit).

Use `make lint` for a faster feedback loop when fixing formatting, style, or SpotBugs findings; use `make coverage` when you only need a coverage report; run `make verify` before opening a PR.

### Spotless (formatting)

Configuration: [`pom.xml`](pom.xml) (`spotless-maven-plugin`).

Formats **main and test** Java sources with Google Java Format (AOSP style), then converts indentation to **tabs** to match project conventions. Also removes unused imports, trims trailing whitespace, and ensures a final newline.

Enforced in **`verify`** and CI via `spotless:check`. Run `make format` locally to apply fixes.

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

Analyzes compiled classes for common defect patterns (null dereferences, resource leaks, bad equality, etc.). The exclude filter suppresses `EI_EXPOSE_REP` / `EI_EXPOSE_REP2` on JPA entities under `*.entity.*` (mutable collection/date exposure for persistence mapping) and on Spring-managed packages (`config`, `*.application`, `*.infrastructure`), where constructor injection stores container-owned singletons.

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

This API pairs with [trustbuddy-frontend](https://github.com/aegre/trustbuddy-frontend).

## AI-assisted development

Built with human-reviewed AI assistance (Cursor Agent). Phase plan, conventions, and delivery narrative: [BUILD_JOURNEY.md](BUILD_JOURNEY.md). Contributor rules: [AGENTS.md](AGENTS.md).
