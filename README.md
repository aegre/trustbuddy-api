# trustbuddy-api

Spring Boot REST API for the Trustbuddy insurance quote flow. Pairs with [trustbuddy-frontend](https://github.com/aegre/trustbuddy-frontend).

## Prerequisites

**Docker quick start (recommended):**

- `make`
- Docker + Docker Compose

**Host JVM / local frontend alternatives also need:**

- Java 17+ (API on the host)
- Node.js LTS and npm (Vite in the frontend sibling)
- Sibling clone of [trustbuddy-frontend](https://github.com/aegre/trustbuddy-frontend) at `../trustbuddy-frontend` — or use `make clone-frontend`

## Quick start (Docker) — easiest after clone

Clone this repo, pull in the frontend sibling, then start **everything** in containers. Docker Make targets create `.env` from `.env.example` when missing (`make ensure-env`), so you can try the app right after cloning without hand-copying env files.

```bash
git clone https://github.com/aegre/trustbuddy-api.git
cd trustbuddy-api

make clone-frontend     # clones ../trustbuddy-frontend if missing
make stack-all-up       # API + Postgres/Redis/Kafka + frontend containers
# API:      http://localhost:8080  (Swagger: /swagger-ui.html)
# Frontend: http://localhost:3000
# Login:    dev-user / dev-password
```

`stack-all-up` runs `stack-up` here and in the frontend sibling; each side copies `.env.example` → `.env` if needed. Defaults already allow the SPA on `:3000` and Vite on `:5173` (`CORS_ALLOWED_ORIGINS` in `.env.example`).

```bash
make stack-all-logs     # tail API logs (prints hint for frontend logs)
make stack-all-down     # stop both
```

Override sibling location/URL if needed:

```bash
make clone-frontend FRONTEND_REPO=../trustbuddy-frontend FRONTEND_GIT_URL=https://github.com/aegre/trustbuddy-frontend.git
```

If `../trustbuddy-frontend` is missing and you skip `clone-frontend`, `stack-all-up` warns and starts the API stack only.

## Installation (host development)

For running the API JVM or Vite on the host (not required for the Docker quick start above):

```bash
cp .env.example .env    # or: make ensure-env
make clone-frontend     # if you do not have the sibling yet
```

In the frontend repo: `make install` and optionally `make ensure-env` / `cp .env.example .env` (`VITE_API_BASE_URL=http://localhost:8080`).

## Run the API only (Docker)

```bash
make stack-up           # API + PostgreSQL + Redis + Kafka (creates .env if missing)
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

## Run the frontend (local Vite)

Typical while iterating (API already up):

```bash
cd ../trustbuddy-frontend
make ensure-env         # or cp .env.example .env — once
make install            # once
make run                # or make dev → http://localhost:5173
```

Frontend-only Docker (still needs API on `:8080`):

```bash
cd ../trustbuddy-frontend
make stack-up           # http://localhost:3000 (creates .env if missing)
```

## Dev login

Local API default: `dev-user` / `dev-password`.

```bash
make token              # POST /api/v1/auth/token → JWT body + HttpOnly cookie
```

Session check (Bearer or cookie): `GET /api/v1/auth/me` → `{ "username": "..." }`. Logout: `POST /api/v1/auth/logout`.

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

# Thought Process

So first thing before doing any code is to get the whole picture to translate it to technical requirements.

- Java 17 + Spring Boot 4.1
- Multi-step insurance quote lifecycle (create → coverage → submit)
- PostgreSQL persistence, Redis cache, Kafka submit events
- JWT auth (Bearer + HttpOnly cookie for the SPA; `GET /auth/me` for session check)
- External insurer gateway HTTP call on submit
- REST under `/api/v1/...` with consistent errors and OpenAPI
- Hexagonal / ports-and-adapters layout
- Testing with JUnit + Testcontainers; verify gate (Spotless, Checkstyle, SpotBugs, JaCoCo)

So according to these requirements we need a production-shaped Spring Boot service, not a single-layer demo. Classic controller → service → JPA repository tutorials couple business rules to Spring and the database. Here the quote domain (premium, state transitions, health policy) should stay pure Java, with PostgreSQL, Redis, Kafka, and the insurer HTTP client behind ports so adapters can change without rewriting quote rules. A Makefile is the developer contract (`make help`, `make run-dev`, `make verify`) so infra and quality stay repeatable for reviewers and CI.

## The Plan

Deliver inside-out in small reviewable phases: foundation first, domain before adapters, REST and security after use cases work. Cache, Kafka, and draft expiration can branch in parallel after JWT.

I can foresee 14 main steps (detail in [BUILD_JOURNEY.md](BUILD_JOURNEY.md)):

1. Foundation — Spring Boot shell, profiles, Docker Compose, Makefile, AGENTS.md
2. Domain and persistence — `Quote` aggregate, JPA adapter behind `QuoteRepositoryPort`
3. Premium calculation — multipliers + `PremiumCalculator` with unit tests
4. DTOs and validation — request/response boundaries, coverage health rules
5. Exception handling — domain exceptions → consistent JSON errors
6. Application use cases — `QuoteService`, `QuoteSubmissionService`, insurer gateway port
7. REST controller — versioned `/api/v1/quotes` endpoints
8. JWT authentication — token, logout, `GET /auth/me`, filter, Bearer + cookie carriers
9. Redis quote cache — read-through get, eviction on save
10. Kafka submit events — `quote-submitted` on first successful submit
11. Draft expiration — scheduled `DRAFT` → `EXPIRED`
12. CORS — browser SPA origins
13. Testing polish — Testcontainers ITs, JaCoCo
14. Metrics, OpenAPI export, README / Makefile polish

## Technical extras

springdoc OpenAPI keeps the contract in sync with controllers; a committed [`openapi/openapi.json`](openapi/openapi.json) plus `OpenApiSpecDriftTest` lets the frontend generate typed clients. Docker Compose covers Postgres/Redis/Kafka (and the full API image via `make stack-up`). Quality is not bolted on later: Spotless, Checkstyle, SpotBugs, Error Prone, and JaCoCo run through `make verify`. Sentry reports unexpected 500s and insurer 502s behind an `ErrorReporterPort` when enabled.

# Technical decisions

Documented as **decision**, **why**, and **alternatives** considered.

### Feature-oriented hexagonal architecture

**Decision:** Organize by capability under `quote/` with `domain/`, `application/`, and `infrastructure/`. Domain stays free of Spring/JPA/Kafka/HTTP. Full layout: [ARCHITECTURE.md](ARCHITECTURE.md).

**Why:** Integrations (Postgres, Redis, Kafka, insurer HTTP) are stable for now but might be replaced later. Ports (`QuoteRepositoryPort`, `InsurerGatewayPort`, `QuoteEventPublisherPort`, `QuoteCachePort`) keep premium and state logic untouched when an adapter changes.

**Alternatives:** Classic Spring four-layer stack (web → service → repository) with JPA and `KafkaTemplate` inside use cases.

### Immutable `Quote` + value objects

**Decision:** Model the aggregate with `PersonalInfo`, `CoverageDetails`, and `QuoteAudit`; state changes go through domain/application services rather than mutating fields from controllers.

**Why:** Clear invariants for draft → submitted / failed / expired, and safer transitions under concurrent updates (`@Version` optimistic locking on the entity).

**Alternatives:** Mutable anemic entity shared across layers; expose JPA entities as API DTOs.

### JWT with Bearer and HttpOnly cookie

**Decision:** Same JWT from `POST /api/v1/auth/token` — scripts/Swagger use `Authorization: Bearer`; the SPA uses an HttpOnly cookie with `credentials: 'include'`. `GET /api/v1/auth/me` validates the session (either carrier) and returns the username; `POST /api/v1/auth/logout` clears the cookie. Bearer wins when both are present.

**Why:** One auth mechanism serves Postman and the browser without putting the token in `localStorage`. Cookie flags (`HttpOnly`, `SameSite`, `Secure` in prod) match frontend session restore via `/auth/me`.

**Alternatives:** Bearer-only; opaque server sessions; OAuth2/OIDC from day one.

### Real HTTP insurer gateway (not an in-memory mock)

**Decision:** `InsurerGatewayHttpAdapter` calls a configurable URL (dev default: HTTP status mock). Gateway failure → `SUBMISSION_FAILED` and **502**; submit is idempotent when already `SUBMITTED`.

**Why:** Exercises timeouts, failure mapping, and resubmit the same way a real vendor would — without depending on a proprietary insurer API for the challenge.

**Alternatives:** Hard-coded in-memory “always succeed” stub inside the application service.

### Caching via repository decorator

**Decision:** `@Primary` `CachingQuoteRepositoryAdapter` wraps persistence and evicts Redis on every `save`; get is read-through.

**Why:** Cache policy stays in one place instead of scattering evict annotations across use cases.

**Alternatives:** Spring `@Cacheable` on services; cache only in the controller; no cache.

### OpenAPI as the client contract

**Decision:** springdoc from annotations; export with `make openapi-export`; CI drift check with `make openapi-drift`.

**Why:** Frontend Orval codegen and Postman stay aligned without a hand-maintained second spec.

**Alternatives:** Hand-written OpenAPI YAML; no committed export (live `/v3/api-docs` only).

### given_when_then tests + Testcontainers

**Decision:** BDD-style method names and Given/When/Then blocks (see [AGENTS.md](AGENTS.md)); adapter ITs use Testcontainers for Postgres/Redis/Kafka where needed.

**Why:** Scenario clarity without Cucumber ceremony; real infra for adapters, mocked ports for application tests.

**Alternatives:** Full Gherkin suites; H2 for everything; only `@SpringBootTest` smoke tests.

### Static analysis in the verify gate

**Decision:** Enforce quality in CI and locally via `make verify` — Spotless (format), Checkstyle (style/complexity), SpotBugs (bytecode defects), Error Prone (compile-time), plus JaCoCo coverage reports. Pre-commit runs full-tree Spotless when Java is staged.

**Why:** Catch formatting drift, naming/complexity issues, and common bug patterns before merge, instead of bolting linters on after the quote flow shipped. Same gate for humans and AI-assisted edits ([AGENTS.md](AGENTS.md)).

**Alternatives:** Format-only (Spotless alone); IDE inspections without CI fail; SonarQube as the only quality gate; no coverage reporting.

# Challenges

Hard problems I hit while building, how I handled them, and what is still imperfect.

### Keeping a hexagonal architecture without noise in the domain

**Challenge:** Ship a full Spring Boot quote API (JPA, Redis, Kafka, HTTP gateway, security) while keeping `quote/domain` pure — no Spring annotations, no persistence types, no messaging or HTTP clients leaking into premium or state rules.

**What I did:** Feature-oriented ports and adapters under `quote/` ([ARCHITECTURE.md](ARCHITECTURE.md)). Domain and application depend on port interfaces only; adapters and mappers live in `infrastructure/`. [AGENTS.md](AGENTS.md) and phase reviews guard the dependency rule.

**Still imperfect:** The hexagonal shape is only partly realized. Controllers still call application services directly (no inbound ports), some wiring is Spring-centric at the edges, and the layout is stricter on paper than everywhere in the codebase. A fuller ports-and-adapters implementation — clearer inbound/outbound boundaries and less framework noise at the application edge — is still unfinished work.

### Different runtimes: local JVM, Docker Compose, and production

**Challenge:** Local host development, the Compose stack, and production do not share the same infra hostnames, secrets, or safety defaults. One `application.yml` with localhost baked in breaks Docker; Docker service names break the host JVM; production must not inherit Swagger or loose CORS.

**What I did:** Split profiles — `application.yml` for universal defaults (env-driven), `application-dev.yml` for localhost + `make run` / `make run-dev`, `application-docker.yml` for Compose service names (`make stack-up`), `application-prod.yml` for strict production (Swagger off, secrets from the environment). `.env.example` documents what each mode needs.

**Still imperfect:** Operators still must pick the right profile and env vars; a wrong combo fails late at startup rather than with a single “environment checklist” command.

### OpenAPI drift vs frontend codegen

**Challenge:** The frontend needs a stable OpenAPI JSON for Orval, but keeping a committed [`openapi/openapi.json`](openapi/openapi.json) in sync with live springdoc is fragile. Manual `make openapi-export` (curl + pretty-print) and the drift test’s comparison of “what CI/boot sees” can disagree on formatting or exploded vs nested shapes (e.g. Pageable query params) even when the API is correct — false failures or silent client breakage.

**What I did:** `OpenApiSpecDriftTest` + `make openapi-export` / `make openapi-drift`, and frontend `make openapi-update` after a fresh export. Treat the committed file as a contract snapshot for sibling codegen until something better exists.

**Still imperfect / ideal:** Prefer **not committing** the spec at all — generate it in CI (or pull from a hosted `/v3/api-docs`) and feed Orval from that artifact so there is one generation path, no hand export vs test mismatch, and no stale file in git.

### Cookie auth for the SPA without breaking Bearer clients

**Challenge:** Browser needs cookies + CORS; scripts and Swagger need Bearer; both must share one JWT. The SPA also needs a way to restore “am I logged in?” on refresh without reading the cookie from JavaScript.

**What I did:** Token endpoint sets cookie and returns body token; filter accepts either; `GET /api/v1/auth/me` returns the authenticated username for session bootstrap; CORS from `CORS_ALLOWED_ORIGINS`; logout clears the cookie.

**Still imperfect:** No refresh-token / sliding session — expired JWT means 401 and re-login (acceptable for local/dev).

# AI Driven Development

This project was developed with **Cursor** as the main AI coding tool. Clear guardrails live in [AGENTS.md](AGENTS.md), with a verify suite (`make verify`) for new changes. Discovery and architecture decisions fed a phased plan in [BUILD_JOURNEY.md](BUILD_JOURNEY.md) with boundaries, deliverables, and progress.

# Deferred & out of scope

Work not done yet, or deliberately left out of this delivery. Fine for local/dev use of the main quote flow.

### Schema migrations (highest priority next)

**Not built yet — critical follow-up:** Versioned schema migrations with **Flyway** or **Liquibase**.

**Why it matters now:** Local/dev can lean on JPA schema updates, but shared environments cannot. Reproducible migrations are required before real deploys, team databases, or any schema change that must apply the same way everywhere. This is the top deferred item for the API.

**Direction:** Introduce Flyway (or Liquibase) early in the next hardening pass, baseline the current `quotes` schema, and make every DDL change a migration checked into the repo.

### End-user registration

**Left out:** Sign-up / invite flow for real users.

**Direction considered:** Email OTP or IdP. Skipped for now because the flow is exercised with **development users** (`dev-user` / `dev-password`).

### Token refresh

**Not built:** Silent refresh when the access cookie/JWT expires.

**Current behavior:** Authenticated calls that return **401** clear the SPA session. Acceptable for local development; poor for long production sessions. Refresh-token or sliding session on API + frontend would be the follow-up.

### OpenAPI from the cloud (stop committing the spec)

**Still manual/sibling-based:** Contract sync depends on a local export and a committed `openapi/openapi.json`. Ideal end state: host or CI-publish `/v3/api-docs`, point the frontend/CI at that URL, and drop the committed snapshot (see Challenges).

### Inbound use-case ports

**Left out:** Formal `application/port/in/` interfaces for every use case.

**Why it matters later:** Multiple drivers (REST, jobs, messaging) for the same command would benefit from explicit inbound ports; today REST → application service is enough.

# Sibling repo

https://github.com/aegre/trustbuddy-frontend
