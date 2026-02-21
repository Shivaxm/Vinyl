# Store Frontend (React + Vite + TypeScript)

Frontend for the Spring Boot Store backend.

## Tech

- React + TypeScript (Vite)
- Tailwind CSS
- React Router

## Features

- Product catalog with category filter
- Product detail page with quantity selector
- Guest cart using backend `guestToken` cookie
- Login/register flow
- Guest cart merge message after login
- Cart management (update quantity, remove, clear)
- Checkout redirect using `POST /checkout`
- Order history (`/orders`) for authenticated users
- Success page for Stripe redirect

## API Contract Used

- OpenAPI: `http://localhost:8080/v3/api-docs`
- Base API URL is configured via `VITE_API_URL`

## Local Setup

1. Start backend (repo root):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

2. Create frontend env file:

```bash
cd frontend
cp .env.example .env
```

3. Install and run frontend:

```bash
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`.

## Environment Variables

- `VITE_API_URL` (default in `.env.example`: `/api`)
- `VITE_API_PROXY_TARGET` (default in `.env.example`: `http://localhost:8080`)

Using `/api` with Vite proxy avoids CORS changes in backend local dev.

## Scripts

- `npm run dev` - start dev server
- `npm run build` - production build
- `npm run preview` - preview production build

## Notes

- Access token is kept in memory (React context), not localStorage.
- Refresh token is HttpOnly cookie; frontend uses `POST /auth/refresh` for silent refresh.
- All API requests include `credentials: 'include'`.
- Backend currently redirects Stripe success to `/checkout-success`; this frontend supports both `/checkout-success` and `/orders/success`.
