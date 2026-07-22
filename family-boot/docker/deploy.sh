#!/usr/bin/env sh
set -eu

APP_SERVICE="family-boot"
COMPOSE_FILE="docker-compose.yml"

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
APP_DIR=$(dirname "$SCRIPT_DIR")
ENV_FILE="$APP_DIR/.env"

cd "$SCRIPT_DIR"

usage() {
  echo "Usage: ./deploy.sh [image-tag]"
  echo "Example: ./deploy.sh abc1234"
}

if [ "${1:-}" = "-h" ] || [ "${1:-}" = "--help" ]; then
  usage
  exit 0
fi

if [ "$#" -gt 1 ]; then
  usage
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  echo "Missing $ENV_FILE. Upload the production .env file first."
  exit 1
fi

update_env_value() {
  key="$1"
  value="$2"
  tmp_file="${ENV_FILE}.tmp.$$"

  if grep -q "^${key}=" "$ENV_FILE"; then
    sed "s|^${key}=.*|${key}=${value}|" "$ENV_FILE" > "$tmp_file"
    mv "$tmp_file" "$ENV_FILE"
  else
    printf "\n%s=%s\n" "$key" "$value" >> "$ENV_FILE"
  fi
}

get_env_value() {
  key="$1"
  grep "^${key}=" "$ENV_FILE" | tail -n 1 | cut -d '=' -f 2- | tr -d '\r'
}

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
  else
    echo "Docker Compose is not installed."
    exit 1
  fi
}

if ! docker info >/dev/null 2>&1; then
  echo "Docker is unavailable or the current user cannot access the Docker daemon."
  exit 1
fi

if [ "${1:-}" != "" ]; then
  update_env_value IMAGE_TAG "$1"
fi

IMAGE_TAG=$(get_env_value IMAGE_TAG || true)

if [ "$IMAGE_TAG" = "" ] || [ "$IMAGE_TAG" = "replace-with-github-sha" ]; then
  usage
  echo "IMAGE_TAG must be provided or configured in $ENV_FILE."
  exit 1
fi

IMAGE_REPOSITORY="registry.cn-hangzhou.aliyuncs.com/mingus/family"
echo "Deploying ${IMAGE_REPOSITORY}:${IMAGE_TAG}"

mkdir -p "$APP_DIR/logs" "$APP_DIR/uploadPath"
if ! chown -R 10001:10001 "$APP_DIR/logs" "$APP_DIR/uploadPath" 2>/dev/null; then
  if command -v sudo >/dev/null 2>&1 && sudo -n true >/dev/null 2>&1; then
    sudo chown -R 10001:10001 "$APP_DIR/logs" "$APP_DIR/uploadPath"
  else
    chmod 777 "$APP_DIR/logs" "$APP_DIR/uploadPath"
    echo "Warning: used permissive directory permissions because chown was unavailable."
  fi
fi

if ! docker network inspect mingus-net >/dev/null 2>&1; then
  echo "Missing external Docker network: mingus-net"
  exit 1
fi

compose config --quiet
compose pull "$APP_SERVICE"
compose up -d --remove-orphans "$APP_SERVICE"
compose ps "$APP_SERVICE"

HEALTH_URL=$(get_env_value HEALTH_URL || true)
HEALTH_RETRY=$(get_env_value HEALTH_RETRY || true)
HEALTH_INTERVAL=$(get_env_value HEALTH_INTERVAL || true)
HEALTH_RETRY=${HEALTH_RETRY:-30}
HEALTH_INTERVAL=${HEALTH_INTERVAL:-3}

if [ "$HEALTH_URL" = "" ]; then
  echo "HEALTH_URL is empty. Skipped HTTP health check."
  exit 0
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is not installed. Skipped HTTP health check."
  exit 0
fi

echo "Checking ${HEALTH_URL}"
i=1
while [ "$i" -le "$HEALTH_RETRY" ]; do
  if curl -fsS "$HEALTH_URL" >/dev/null; then
    echo "Health check passed."
    exit 0
  fi
  sleep "$HEALTH_INTERVAL"
  i=$((i + 1))
done

echo "Health check failed. Recent logs:"
compose logs --tail 120 "$APP_SERVICE"
exit 1