## Store (Spring Boot) — Backend

This repo contains a **Spring Boot** backend for a simple store: products, carts (guest + authenticated), orders, JWT auth, Redis caching, Flyway migrations, and **Stripe Checkout** payments.

### Prerequisites

- **Java 21**
- **Docker** (for Postgres + Redis via `docker-compose`)
- (Optional) a Stripe account + Stripe CLI for webhook testing

### Quick start (local)

- **Option A (recommended for interviews): zero-setup local profile (no Docker)**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

This runs with an in-memory H2 database and Flyway seeds, plus relaxed local defaults (so you can immediately use Postman).

- **Option B: Start Postgres + Redis (Docker)**

```bash
docker compose up -d
```

- **2) Create your local env file**

```bash
cp example.env .env
```

Fill in at least:
- **JWT_SECRET**: must be long enough for HMAC (recommend 32+ chars)
- **STRIPE_SECRET_KEY**: your Stripe secret key (starts with `sk_test_...`) if you want checkout to work
- **STRIPE_WEBHOOK_SECRET_KEY**: signing secret (starts with `whsec_...`) if you want webhooks to work

- **3) Run the API**

```bash
./mvnw spring-boot:run
```

The API starts on **`http://localhost:8080`** by default.

### API docs (Swagger)

This project includes SpringDoc OpenAPI. Once running:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Postman (interviewer-friendly)

You have two easy options:

- **Option A: Import OpenAPI into Postman (recommended)**
  - In Postman: **Import → Link/File**
  - Use the OpenAPI endpoint: `http://localhost:8080/v3/api-docs`
  - Postman will generate a collection automatically.

- **Option B: Manual testing**
  - **Login**: `POST /auth/login` → copy the `token` from the JSON response
  - For authenticated endpoints, add header: `Authorization: Bearer <accessToken>`

You can also import the included Postman collection JSON:
- `StoreAPIs.postman_collection.json`

For a “zero-click” setup, also import the included Postman environment:
- `Store.local.postman_environment.json`

Then select **Store Local** in Postman’s environment dropdown (top-right). This supplies `{{protocol}}`, `{{host}}`, `{{port}}`, etc. to the collection without editing any requests.

**Important for localhost:** this app uses cookies (`guestToken`, `refreshToken`). Cookies marked **Secure** won’t be sent back over plain HTTP, so for local/Postman set:
- `COOKIE_SECURE=false` in `.env`

### Running tests

```bash
./mvnw test
```

### Stripe Checkout + webhooks (how to run locally)

This backend exposes a webhook endpoint at:
- `POST /checkout/webhook`

The easiest way to test locally is **Stripe CLI**, which is why it asks you to “log in”.

- **1) Install Stripe CLI**
  - See Stripe’s docs: `https://stripe.com/docs/stripe-cli`

- **2) Login (one-time)**

```bash
stripe login
```

- **3) Forward webhooks to your local server**

```bash
stripe listen --forward-to localhost:8080/checkout/webhook
```

The CLI prints a signing secret like `whsec_...`. Put that into:
- `STRIPE_WEBHOOK_SECRET_KEY` in your `.env`

- **Optional: trigger a webhook for a specific order**
  - Call `POST /checkout` and copy the returned `orderId`
  - Then trigger the event with overrides so the backend can match the order:

```bash
stripe trigger checkout.session.completed \
  --override "checkout_session:client_reference_id=<orderId>" \
  --override "checkout_session:metadata[order_id]=<orderId>"
```

- **4) Set your Stripe secret key**

From the Stripe Dashboard (Developers → API keys), copy your test secret key (`sk_test_...`) into:
- `STRIPE_SECRET_KEY` in your `.env`

### Configuration reference

The app loads `.env` via:
- `spring.config.import=optional:file:.env[.properties]`

Common settings (see `example.env`):
- **DB_URL / DB_USER / DB_PASSWORD**
- **REDIS_HOST / REDIS_PORT**
- **SPRING_CACHE_TYPE**: `redis` (default) or `simple`
- **WEBSITE_URL**: used to build Stripe success/cancel URLs
- **JWT_SECRET / ACCESS_EXPIRATION / REFRESH_EXPIRATION / GUEST_EXPIRATION**
- **STRIPE_SECRET_KEY / STRIPE_WEBHOOK_SECRET_KEY**
