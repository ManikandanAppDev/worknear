# WorkNear API

Backend for **WorkNear**, a nearby home-services marketplace connecting customers with professionals (Electrician, Plumber, AC Mechanic, Painter, Carpenter, Cleaner). Serves three clients: the Customer app, the Professional app, and the Web Admin panel.

## Tech stack

- **Java 17 + Spring Boot 3.3** (Web, Data JPA, Security, Validation, WebSocket, Actuator)
- **PostgreSQL 16** with **Flyway** migrations
- **Redis** (OTP storage, caching, rate-limiting)
- **JWT** auth (access + refresh) with role-based access control
- **springdoc-openapi** (Swagger UI)
- **Docker / docker-compose**

External integrations (OTP SMS, payments, push, object storage) are **stubbed** behind interfaces and toggled by `*_MOCK` flags, so the app runs fully offline. Swap in real providers (Razorpay, MSG91/Twilio, FCM, S3) later without touching callers.

## Quick start (Docker)

```bash
cd backend
cp .env.example .env
docker compose up --build
```

API: `http://localhost:8080` · Swagger UI: `http://localhost:8080/swagger-ui.html`

## Quick start (local, without Docker)

Requires Java 17 + Maven, plus a running Postgres and Redis.

```bash
cd backend
mvn spring-boot:run
```

## Roles

| Role | Description |
|------|-------------|
| `CUSTOMER` | Books and tracks services |
| `PROFESSIONAL` | Offers services, accepts jobs, gets paid |
| `ADMIN` | Verifies pros, manages bookings, payouts, disputes |

## Auth flow (mock OTP)

1. `POST /api/v1/auth/otp/request` `{ "phone": "+919876543210", "role": "CUSTOMER" }`
2. With `OTP_MOCK=true` the OTP is `4821` (also echoed in the response for dev).
3. `POST /api/v1/auth/otp/verify` `{ "phone": "+919876543210", "code": "4821" }` → returns `accessToken` + `refreshToken`.
4. Send `Authorization: Bearer <accessToken>` on protected endpoints.
5. `POST /api/v1/auth/refresh` `{ "refreshToken": "..." }` to rotate tokens.

## Module map

| Module | Path | Responsibility |
|--------|------|----------------|
| auth | `auth/` | OTP, JWT, refresh, social login (stub) |
| user | `user/` | Profile, addresses, customer/pro onboarding, documents |
| catalog | `catalog/` | Service categories, professional services, availability, nearby search |
| booking | `booking/` | Create/list bookings, status transitions, photos |
| chat | `chat/` | Threads + messages (REST + WebSocket) |
| wallet | `wallet/` | Balances, transactions, top-up |
| payment | `payment/` | Payment orders + webhook (stub) |
| payout | `payout/` | Professional withdrawals |
| review | `review/` | Ratings & reviews |
| admin | `admin/` | Verification queue, bookings mgmt, payouts, dashboard KPIs |
| notification | `notification/` | Device tokens + push (stub) |

## Seed accounts (dev)

Loaded by Flyway seed migration. Log in via OTP with these phones (OTP `4821`):

- Customer: `+919000000001`
- Professional (verified): `+919000000002`
- Admin: `+919000000009`

## Project layout

```
src/main/java/com/worknear/api
├── common        # base entity, exceptions, api response, pagination
├── config        # security, jwt, openapi, cors, web
├── auth          # authentication
├── user          # users, profiles, addresses, documents
├── catalog       # categories, pro services, availability
├── booking       # bookings + photos
├── chat          # threads + messages
├── wallet        # wallet + transactions
├── payment       # payment orders (stub gateway)
├── payout        # pro payouts
├── review        # reviews
├── admin         # admin operations
└── notification  # device tokens + push (stub)
```
