# AWS Lightsail — WorkNear API deployment

Deploy the WorkNear backend on a **$7/month Lightsail** instance with HTTPS, separate from your local Docker dev setup.

---

## Architecture

```
Internet → Caddy (443/HTTPS) → API :8080 (localhost only)
                                    ↓
                              Postgres + Redis (Docker internal network only)
```

- **Dev (local PC):** `docker compose up` + Android **`devDebug`**
- **Dev (AWS):** Lightsail + `docker-compose.prod.yml` + Android **`devDebug`** with AWS dev URL
- **Prod (AWS):** second Lightsail (or same stack later) + Android **`prodRelease`**

---

## Part 1 — Create Lightsail instance

1. Open [AWS Lightsail](https://lightsail.aws.amazon.com/) → **Create instance**.
2. **Platform:** Linux/Unix  
3. **Blueprint:** Ubuntu 22.04 LTS  
4. **Plan:** $7 USD / month (1 GB RAM is enough for MVP)  
5. **Name:** `worknear-dev-api` (create `worknear-prod-api` later for prod)  
6. **Create instance**.

### Open firewall ports

In Lightsail → your instance → **Networking** → **Firewall**:

| Port | Purpose |
|------|---------|
| 22 | SSH |
| 80 | HTTP (Caddy → HTTPS redirect) |
| 443 | HTTPS |

Do **not** open 5432, 6379, or 8080 to the public.

### Static IP (recommended)

Networking → **Create static IP** → attach to instance. Use this for DNS.

---

## Part 2 — Point domain (optional but recommended)

In your DNS provider (Route 53, Cloudflare, etc.):

```
dev-api.yourdomain.com  →  A record  →  <Lightsail static IP>
```

Without a domain, you can test with the static IP only (HTTPS via Caddy needs a domain for Let's Encrypt).

---

## Part 3 — SSH and install Docker + Caddy

Download the default SSH key from Lightsail, then:

```bash
ssh -i LightsailDefaultKey.pem ubuntu@<STATIC_IP>
```

On the server, either run the bootstrap script from your repo:

```bash
cd /tmp
git clone https://github.com/YOUR_USER/YOUR_REPO.git worknear
cd worknear/backend
sudo bash deploy/lightsail-setup.sh
```

Or install manually (same steps as in `deploy/lightsail-setup.sh`).

---

## Part 4 — Deploy the API

```bash
sudo mkdir -p /opt/worknear
sudo chown ubuntu:ubuntu /opt/worknear
cd /opt/worknear
git clone https://github.com/YOUR_USER/YOUR_REPO.git .
cd backend

cp .env.prod.example .env.prod
nano .env.prod   # set DB_PASSWORD, JWT_SECRET, STORAGE_BASE_URL, domain
```

Generate secrets (on server):

```bash
openssl rand -base64 32   # use for DB_PASSWORD
openssl rand -base64 48   # use for JWT_SECRET
```

Start stack (production compose override):

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build
```

Check logs:

```bash
docker compose logs -f api
curl -s http://127.0.0.1:8080/actuator/health
```

---

## Part 5 — HTTPS with Caddy

```bash
sudo cp deploy/Caddyfile /etc/caddy/Caddyfile
sudo nano /etc/caddy/Caddyfile   # replace dev-api.yourdomain.com
sudo systemctl reload caddy
```

Test from your phone browser:

```
https://dev-api.yourdomain.com/actuator/health
https://dev-api.yourdomain.com/swagger-ui.html
```

(Swagger is disabled when `SPRING_PROFILES_ACTIVE=prod`; use health + API calls. For dev AWS testing with Swagger, set `SPRING_PROFILES_ACTIVE=dev` in `.env.prod` temporarily.)

---

## Part 6 — Connect Android physical device

In `app/build.gradle.kts`, **dev** flavor:

```kotlin
create("dev") {
    buildConfigField("String", "BASE_URL", "\"https://dev-api.yourdomain.com/\"")
    buildConfigField("boolean", "ENABLE_HTTP_LOGS", "true")
}
```

Android Studio → **Build Variants** → **`devDebug`** → Run on phone.

Login: phone `9000000001`, OTP `4821` (while `OTP_MOCK=true`).

---

## Part 7 — View database on AWS

Postgres is **not** public. Options:

### A — psql inside the container (easiest)

```bash
docker exec -it worknear-postgres psql -U worknear -d worknear
\dt
SELECT id, phone, full_name FROM users;
```

### B — SSH tunnel + DBeaver (from your PC)

```bash
ssh -i LightsailDefaultKey.pem -L 5433:127.0.0.1:5432 ubuntu@<STATIC_IP>
```

Temporarily expose Postgres on localhost only on the server (dev debugging only):

```yaml
# emergency only — remove after debugging
ports:
  - "127.0.0.1:5432:5432"
```

Then DBeaver: `localhost:5433`, user `worknear`, password from `.env.prod`.

### C — RDS later (prod scale)

Move Postgres to **RDS PostgreSQL**; point `DB_URL` in `.env.prod` to the RDS endpoint.

---

## Part 8 — Second environment (prod)

**Decisions (locked in):**

| Item | Choice |
|------|--------|
| Prod server | New Lightsail instance (same as dev), name: `worknear-prod-api` |
| Dev server | Existing `worknear-dev-api` (IP `65.1.135.244`) |
| SMS / OTP | Mock on dev for now; real SMS on prod **later** (after app flow is complete) |
| Android dev | `devDebug` → dev API only (`com.worknear.app.dev`) |
| Android prod | `prodRelease` → prod API only (`com.worknear.app`) |
| Prod secrets | Generate **new** `DB_PASSWORD` + `JWT_SECRET` when prod instance is created (never reuse dev) |

### Suggested domain names

Buy one root domain (recommended for India: **`worknear.in`**, or **`worknear.com`**). Then:

| Purpose | DNS name | Points to |
|---------|----------|-----------|
| **Prod API** (Android prod flavor) | `api.worknear.in` | Prod Lightsail static IP |
| **Dev API** (Android dev flavor) | `dev-api.worknear.in` | Dev Lightsail static IP |
| **File uploads** (STORAGE_BASE_URL) | same as API + `/files` | e.g. `https://api.worknear.in/files` |
| **Admin web** (later) | `admin.worknear.in` | separate host or same prod server |

**Until you buy a domain:** use sslip.io on each instance (like dev today):

- Dev: `https://65-1-135-244.sslip.io/`
- Prod: `https://<PROD-STATIC-IP-with-dashes>.sslip.io/` (set after instance is created)

Update `app/src/prod/java/.../AppConfig.kt` → `BASE_URL` when prod IP/domain is ready.

### Prod instance setup (when you create it)

1. Create Lightsail **`worknear-prod-api`** (Ubuntu 22.04, 1 GB, Mumbai `ap-south-1`).
2. Attach a **static IP**; open ports 22, 80, 443.
3. Point **`api.worknear.in`** (or sslip.io) A record → prod static IP.
4. SSH, clone repo to `/opt/worknear`, copy `.env.prod.example` → `.env.prod`.
5. Set prod `.env.prod` (see checklist below).
6. Caddy on prod with host `api.worknear.in` (see `deploy/Caddyfile`).
7. Deploy: same `docker compose ... up -d --build` as dev.

### Before first prod test — secrets checklist (reminder)

When you are ready to test **`prodRelease`**, generate **new** values on the prod server:

```bash
openssl rand -base64 32   # DB_PASSWORD
openssl rand -base64 48   # JWT_SECRET
```

In prod `.env.prod`:

```env
SPRING_PROFILES_ACTIVE=prod
OTP_MOCK=true              # keep true until SMS is wired; then false
PAYMENT_MOCK=true          # same — wire real gateway later
STORAGE_BASE_URL=https://api.worknear.in/files
CORS_ORIGINS=https://admin.worknear.in
```

**Do not copy dev `.env.prod` secrets to prod.**

### Android prod flavor

Prod is already wired via flavor-specific `AppConfig.kt`:

- **Dev:** `app/src/dev/.../AppConfig.kt` → dev API URL, HTTP logs on
- **Prod:** `app/src/prod/.../AppConfig.kt` → prod API URL only, HTTP logs off

Build for store / prod testing:

```bash
./gradlew assembleProdRelease
```

Install **`prodRelease`** on a test device — it must **not** talk to the dev server.

Same Git branch — deploy the same commit to both servers with **different** `.env.prod` files.

---

## Useful commands

```bash
# Restart after code pull
git pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build

# Stop
docker compose -f docker-compose.yml -f docker-compose.prod.yml down

# Logs
docker compose logs -f api

# Disk / volumes (DB data lives here)
docker volume ls
docker volume inspect backend_pgdata
```

---

## Cost summary

| Resource | ~Monthly |
|----------|----------|
| Lightsail dev | $7 |
| Lightsail prod | $7 |
| Static IP | included |
| **Total (dev + prod)** | **~$14** |

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Health check fails | `docker compose logs api` — usually DB password mismatch |
| Phone can't reach API | DNS not propagated; test health URL in mobile browser first |
| Caddy cert error | Domain must point to server IP before HTTPS works |
| 502 from Caddy | API not running — `curl http://127.0.0.1:8080/actuator/health` on server |
| **Invalid OTP code** / empty `devCode` | Mock OTP is off. On the **dev** Lightsail instance, SSH in and run the fix in **Part 9** below. After restart, OTP request must return `"devCode":"4821"`. Login: phone `9000000001`, OTP `4821`. |

---

## Part 9 — Enable mock OTP on the dev server

If OTP request returns `"devCode": null`, the API is not in mock mode. Fix it on the server:

```bash
ssh -i LightsailDefaultKey-ap-south-1.pem ubuntu@65.1.135.244
cd /opt/worknear/backend

# 1) Ensure mock flags are set
grep OTP_MOCK .env.prod || echo "OTP_MOCK missing!"
# If missing or false, edit:
nano .env.prod
#   OTP_MOCK=true
#   SPRING_PROFILES_ACTIVE=dev   # optional; dev profile also enables mock OTP

# 2) Force mock OTP into the running container (works even before git pull)
cat > docker-compose.override.yml << 'EOF'
services:
  api:
    environment:
      WORKNEAR_OTP_MOCK: "true"
      OTP_MOCK: "true"
EOF

# 3) Recreate API container (no full rebuild needed)
docker compose -f docker-compose.yml -f docker-compose.prod.yml -f docker-compose.override.yml --env-file .env.prod up -d --force-recreate api

# 4) Confirm mock mode in logs
docker compose logs api --tail=20 | grep MOCK

# 5) Test from the server
curl -s -X POST http://127.0.0.1:8080/api/v1/auth/otp/request \
  -H "Content-Type: application/json" \
  -d '{"phone":"+919000000001","role":"CUSTOMER"}'
# Expect: "devCode":"4821"
```

After you see `devCode: 4821`, retry login in the Android app (`devDebug` build).

**Later:** `git pull` in `/opt/worknear/backend` picks up repo fixes so `OTP_MOCK=true` in `.env.prod` is enough without the override file.
