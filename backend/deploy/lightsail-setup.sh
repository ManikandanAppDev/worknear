#!/usr/bin/env bash
# WorkNear — AWS Lightsail / Ubuntu bootstrap
# Run on a fresh Ubuntu 22.04 instance as root or with sudo:
#   curl -fsSL ... | bash   OR   bash lightsail-setup.sh
set -euo pipefail

echo "==> Installing Docker..."
apt-get update -qq
apt-get install -y ca-certificates curl git
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  > /etc/apt/sources.list.d/docker.list
apt-get update -qq
apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

echo "==> Installing Caddy (HTTPS reverse proxy)..."
apt-get install -y debian-keyring debian-archive-keyring apt-transport-https
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list
apt-get update -qq
apt-get install -y caddy

echo "==> Creating app directory..."
mkdir -p /opt/worknear
echo ""
echo "Done. Next steps:"
echo "  1. Clone your repo into /opt/worknear (or scp the backend folder)"
echo "  2. cd /opt/worknear/backend"
echo "  3. cp .env.prod.example .env.prod && nano .env.prod"
echo "  4. docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d --build"
echo "  5. cp deploy/Caddyfile /etc/caddy/Caddyfile && nano /etc/caddy/Caddyfile"
echo "  6. systemctl reload caddy"
echo "  7. curl https://YOUR_DOMAIN/actuator/health"
