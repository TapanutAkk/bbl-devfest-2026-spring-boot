# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java Spring Boot web application for BBL Tech Dev Fest 2026. Two REST APIs plus an Angular web UI at `/` (source in `frontend/`, built output committed under `src/main/resources/static/`):

- `/users` — user-management CRUD per the "Backend API Development Test" spec; **in-memory storage** (ConcurrentHashMap in `UserService`, seeded with 3 jsonplaceholder users, resets on restart — deliberately no database, don't "fix" it by adding JPA)
- `/api/items` — items CRUD backed by H2 by default (PostgreSQL optional via Docker Compose)

## Environment Constraints

- **Preferred workflow is local Java, not Docker.** The machine has Java 18; Maven 3.9.9 lives at `~/tools/apache-maven-3.9.9` and the repo has a committed Maven Wrapper — always use `./mvnw`, never assume a global `mvn`.
- Docker Desktop on this machine is old (4.14.1) and its engine hangs easily; the disk is also small. Use `docker compose` only when PostgreSQL specifically is needed. Never leave large image pulls running unattended — a full disk here has previously wedged both Docker and the shell.
- Default database is **embedded H2** (file-based at `./data/`, git-ignored) — zero installation. PostgreSQL is used only when the `SPRING_DATASOURCE_*` env vars point to it (Docker Compose sets these automatically).
- Node.js is v22.20.0 — **too old for Angular 22** (needs ≥22.22.3), so the frontend is pinned to Angular 21. Don't `ng update` past 21 without upgrading Node first.

## Common Commands

### Run the application (local, default)

```bash
./mvnw spring-boot:run       # http://localhost:8080 (web UI at /, API at /api/items)

# Dev mode: serve static files straight from src/main/resources so edits to
# static/index.html show up on browser refresh without a restart
./mvnw spring-boot:run -Dspring-boot.run.addResources=true
```

Health check: `curl http://localhost:8080/actuator/health`

Static files (HTML/CSS/JS) are otherwise served from `target/classes` — a plain `spring-boot:run` needs a restart to pick up edits under `src/main/resources/static/`. Java changes always require a restart. When restarting for the user, kill only the LISTEN process on the port (`kill $(lsof -ti :8080 -sTCP:LISTEN)`) — killing every PID that lsof reports on the port has previously hit the browser and caused an unclean H2 shutdown that lost data.

### Frontend (Angular, in `frontend/`)

The web UI is an Angular 21 standalone app (signals, zoneless). `ng build` writes **directly into `src/main/resources/static/`** (configured via `outputPath` in `frontend/angular.json`) and wipes that directory first — never hand-edit files under `static/`; edit `frontend/src/` and rebuild.

```bash
cd frontend
npm start            # dev server on :4200, proxies /api + /actuator to :8080 (proxy.conf.json)
npx ng build         # production build → ../src/main/resources/static/
npx ng test --watch=false   # unit tests (Vitest)
```

After `npx ng build`, a running Spring Boot app still serves the old copy from `target/classes/static` — either restart it or copy the fresh build over: `rm -rf target/classes/static && cp -R src/main/resources/static target/classes/static`.

### Build and test

```bash
./mvnw clean verify                              # full build + all tests
./mvnw test                                      # all tests
./mvnw test -Dtest=ItemControllerTest            # single test class
./mvnw test -Dtest=ItemControllerTest#createReturns201  # single test method
```

Tests use `@WebMvcTest` with mocked services — no database or Docker required.

### Quick API smoke test

```bash
# Users API (in-memory)
curl -s http://localhost:8080/users
curl -s -X POST http://localhost:8080/users -H 'Content-Type: application/json' \
  -d '{"name":"Demo","username":"demo","email":"demo@example.com"}'
curl -s -X PUT http://localhost:8080/users/1 -H 'Content-Type: application/json' \
  -d '{"name":"Demo2","username":"demo","email":"demo@example.com"}'
curl -s -X DELETE http://localhost:8080/users/1

# Items API (H2/Postgres)
curl -s http://localhost:8080/api/items
curl -s -X POST http://localhost:8080/api/items -H 'Content-Type: application/json' -d '{"name":"demo"}'
curl -s -X DELETE http://localhost:8080/api/items/1
```

### Optional: full stack in Docker (PostgreSQL)

```bash
docker compose up -d --build   # app on :8080 + Postgres on :5432
docker compose down            # stop (add -v to wipe DB data)
```

`compose.yaml` wires the app to Postgres via `SPRING_DATASOURCE_*` env vars; no code or config change needed to switch databases.

## Architecture

Single Maven module, layered packages under `com.bbl.devfest`:

- `controller` — thin REST endpoints (`UserController`, `ItemController`); validation via `@Valid` on request DTOs; `GlobalExceptionHandler` (`@RestControllerAdvice`) turns validation failures into 400s listing each bad field, and `server.error.include-message: always` exposes `ResponseStatusException` messages in 404 bodies
- `service` — business logic; throws `ResponseStatusException` for 404s; `UserService` holds the in-memory user map + `AtomicLong` id sequence
- `repository` — Spring Data JPA interfaces (items only; users have no repository)
- `model` — `Item` is a JPA entity, `User` is a plain record; `dto` — request records with Jakarta validation
- `src/main/resources/application.yml` — datasource defaults to H2, overridable by `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` env vars (this is the only switch between H2 and Postgres)
- `.env` (git-ignored; template in `.env.example`) — loaded by Spring via `spring.config.import: optional:file:.env[.properties]` and by Docker Compose natively; holds `SERVER_PORT`, `SPRING_DATASOURCE_*`, `POSTGRES_*`. Real OS env vars override `.env` values.
- `frontend/` — Angular 21 web UI consuming `/api/items`; single root component (`frontend/src/app/app.ts`), no router. Build output is committed in `src/main/resources/static/` so `./mvnw spring-boot:run` works without Node.
- Tests in `src/test/java` mirror main packages; controller tests use MockMvc + `@MockitoBean` (Spring Boot 3.4+ replacement for `@MockBean`); `UserServiceTest` instantiates the service directly (no Spring context)
- `.github/workflows/ci.yml` — CI: `./mvnw clean verify` on JDK 17, then a `docker build` sanity check

## Gotchas

- Schema is managed by `ddl-auto: update` — no migration tool yet; if entities change incompatibly, delete `./data/` to reset the local H2 database.
- If `./mvnw` fails with a permissions error, run `chmod +x mvnw`.
- Port 8080 must be free; a previously started `./mvnw spring-boot:run` may still be holding it.
