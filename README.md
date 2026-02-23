# Store — Full-Stack E-Commerce Application
[![CI](https://github.com/Shivaxm/Store/actions/workflows/ci.yml/badge.svg)](https://github.com/Shivaxm/Store/actions/workflows/ci.yml)

A full-stack e-commerce application with a Spring Boot backend and React frontend.
Demonstrates JWT authentication with guest-to-authenticated cart merge, Stripe Checkout integration,
Redis-backed caching, Flyway migrations, and a production-ready single-application deploy model.

**[Live Demo →](https://vinyl.up.railway.app)** 

## Demo Flow

1. Browse products and filter by category.
2. Add items to cart as a guest (no login required).
3. Register an account or log in.
4. Watch your guest cart items merge into your authenticated cart.
5. Proceed to Stripe Checkout.
6. View order history with payment status.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4, Spring Security, PostgreSQL, Redis, Flyway, Stripe API
- **Frontend:** React 19, TypeScript, Vite, Tailwind CSS, React Router
- **Infrastructure:** Docker Compose, GitHub Actions CI, JaCoCo coverage

## Prerequisites

- **Java 21**
- **Docker** (optional, for Postgres + Redis in non-local profile)
- **Node.js** (for frontend hot-reload dev mode only)
- **Stripe account + Stripe CLI** (optional, for checkout/webhook testing)

## Quick Start

### Option A: Single-app mode (recommended)

Build everything into one Spring Boot JAR (backend + frontend static bundle):

```bash
./mvnw clean package -DskipTests
java -jar target/store-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

Visit `http://localhost:8080`.

### Option B: Local development (hot reload)

Terminal 1 — backend:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Terminal 2 — frontend:

```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```

Visit `http://localhost:5173`.

## API Documentation

Primary demo is the frontend UI. For API-level verification:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Postman remains available as a secondary option:
- `StoreAPIs.postman_collection.json`
- `Store.local.postman_environment.json`

## Stripe Checkout Setup

Webhook endpoint:
- `POST /checkout/webhook`

1. Install Stripe CLI: `https://stripe.com/docs/stripe-cli`
2. Authenticate once:

```bash
stripe login
```

3. Forward webhooks to local backend:

```bash
stripe listen --forward-to localhost:8080/checkout/webhook
```

4. Copy CLI signing secret (`whsec_...`) into `.env`:
- `STRIPE_WEBHOOK_SECRET_KEY`

5. Set Stripe secret API key in `.env`:
- `STRIPE_SECRET_KEY=sk_test_...`

Optional webhook trigger for a specific order:

```bash
stripe trigger checkout.session.completed \
  --override "checkout_session:client_reference_id=<orderId>" \
  --override "checkout_session:metadata[order_id]=<orderId>"
```

## Configuration Reference

`.env` is loaded via:
- `spring.config.import=optional:file:.env[.properties]`

Common settings (`example.env`):
- `DB_URL / DB_USER / DB_PASSWORD`
- `REDIS_HOST / REDIS_PORT / REDIS_PASSWORD`
- `SPRING_CACHE_TYPE` (`redis` default, or `simple`)
- `WEBSITE_URL` (Stripe success/cancel URL base)
- `JWT_SECRET / ACCESS_EXPIRATION / REFRESH_EXPIRATION / GUEST_EXPIRATION`
- `STRIPE_SECRET_KEY / STRIPE_WEBHOOK_SECRET_KEY`
- `COOKIE_SECURE` (set `false` for localhost HTTP cookie testing)

## Security

- **[OWASP Top 10 Hardening](SECURITY.md)** — Documented protections against each OWASP Top 10 category
- Rate limiting, security headers, input validation, auth hardening, and security event logging

## Performance Benchmarks

Measured locally; results vary by machine and background load.

- **GET `/products` p95 latency:**
  - Cold: **11.435 ms**
  - Warm: **5.037 ms**
  - Improvement: **55.95% faster p95**
- **Throughput (`ab -n 500 -c 20 /products`):**
  - **1,995.12 req/s**
  - 95th percentile latency: **23 ms**
- **Coverage (JaCoCo):**
  - **39.67%** line coverage (288 / 726)
- **Scope:**
  - **22 endpoints** (GET 8, POST 7, PUT 3, DELETE 4)
  - **8 entities** (`Address`, `Cart`, `CartItem`, `Category`, `Order`, `OrderItem`, `Product`, `User`)

## Running Tests

```bash
./mvnw test
```

Coverage report:

```bash
./mvnw test jacoco:report
```

Open:
- `target/site/jacoco/index.html`
