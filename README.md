# Baseras School Portal — Backend

Spring Boot 3.3 + Java 17 + PostgreSQL + Flyway + JWT auth. Implements the API contract consumed by the React frontend at `../college-website`.

## Quick start

### Option 1 — H2 in-memory (zero setup)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
Boots on `http://localhost:8080`. Schema and seed data load automatically. Swagger UI at `/swagger-ui.html`.

### Option 2 — PostgreSQL (production-like)
```bash
# 1. Start Postgres + create DB
createdb baseras
createuser -s baseras
psql -d baseras -c "ALTER USER baseras WITH PASSWORD 'baseras';"

# 2. Run
mvn spring-boot:run
```

### Override config
```bash
DB_URL=jdbc:postgresql://host:5432/db DB_USER=... DB_PASSWORD=... \
JWT_SECRET=$(openssl rand -hex 32) \
mvn spring-boot:run
```

## Demo accounts (matches frontend mock)

| Role | Username | Password |
|---|---|---|
| Student | `R001` | `R001Aar2024` |
| Teacher | `T001` | `teacher123` |
| Admin | `admin` | `admin123` |
| Principal | `principal` | `principal123` |

## Wiring the frontend

In `college-website/package.json`, add:
```json
"proxy": "http://localhost:8080"
```
Then remove the MSW bootstrap block from `src/index.tsx`. Frontend stays the same — every `/api/v1/*` call now hits this backend.

## Project layout

```
src/main/java/com/baseras/portal/
  BaserasApplication.java
  config/        Security, JWT, CORS, OpenAPI
  common/        Exception handler, audit infrastructure
  entity/        JPA entities (one per table)
  repository/    Spring Data JPA repos
  dto/           Request/response records (per domain)
  service/       Business logic
  controller/    REST endpoints
src/main/resources/
  application.yml
  db/migration/V1__init.sql   schema + seed data
```

## Key endpoints

- `POST /api/v1/auth/login` — JWT issuance
- `GET  /api/v1/auth/me` — current user
- All 54 endpoints from the API contract — see Swagger UI

## Build

```bash
mvn clean package        # builds target/portal-backend-0.1.0.jar
java -jar target/portal-backend-0.1.0.jar
```
