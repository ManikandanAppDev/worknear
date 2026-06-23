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

1. Create another Lightsail instance `worknear-prod-api`.
2. Copy repo, use **different** `.env.prod` secrets and domain `api.yourdomain.com`.
3. Set `OTP_MOCK=false` when SMS is wired.
4. Android **prod** flavor:

```kotlin
create("prod") {
    buildConfigField("String", "BASE_URL", "\"https://api.yourdomain.com/\"")
    buildConfigField("boolean", "ENABLE_HTTP_LOGS", "false")
}
```

Build **`prodRelease`** for store / prod testing.

Same Git branch — deploy the same commit to both servers with different `.env.prod` files.

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
