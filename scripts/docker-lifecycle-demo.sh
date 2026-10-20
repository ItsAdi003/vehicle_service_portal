#!/usr/bin/env bash
# Runs the complete container lifecycle and records every command and its output in evidence/docker-lifecycle.log
# Usage (from the repository root, Docker running):   bash scripts/docker-lifecycle-demo.sh [version]
set -u
VERSION="${1:-1.0.0}"
IMAGE="vehicle-service-portal"
NAME="vsbp-demo"
PORT=9090
mkdir -p evidence
LOG="evidence/docker-lifecycle.log"
: > "$LOG"

step() { echo; echo "### $*" | tee -a "$LOG"; }
run()  { echo "\$ $*" | tee -a "$LOG"; "$@" 2>&1 | tee -a "$LOG"; }
wait_healthy() {
  for i in $(seq 1 30); do
    if curl --fail --silent "http://localhost:${PORT}/actuator/health" >/dev/null 2>&1; then
      echo "healthy after $((i*3))s" | tee -a "$LOG"; return 0
    fi
    sleep 3
  done
  echo "NOT healthy after 90s" | tee -a "$LOG"; return 1
}

step "1. Tools"
run docker version --format 'client {{.Client.Version}} / server {{.Server.Version}}'

step "2. Build and tag the image"
run docker build -t "${IMAGE}:${VERSION}" -t "${IMAGE}:latest" .
run docker images "${IMAGE}"

step "3. Inspect the image"
run docker image inspect "${IMAGE}:${VERSION}" --format 'id={{.Id}} size={{.Size}} created={{.Created}} ports={{.Config.ExposedPorts}} user={{.Config.User}}'
run docker history --no-trunc=false "${IMAGE}:${VERSION}"

step "4. Run a container and map port ${PORT} -> 8080"
run docker rm -f "$NAME"
run docker run -d --name "$NAME" -p "${PORT}:8080" -e APP_ENVIRONMENT=docker-demo -v "${NAME}-data:/data" "${IMAGE}:${VERSION}"
run docker ps --filter "name=${NAME}"
wait_healthy
run curl --silent "http://localhost:${PORT}/actuator/health"
echo | tee -a "$LOG"

step "5. Logs and a command inside the container"
run docker logs --tail 15 "$NAME"
run docker exec "$NAME" ls -l /app /data
run docker inspect --format 'health={{.State.Health.Status}} started={{.State.StartedAt}} ip={{.NetworkSettings.IPAddress}}' "$NAME"
run docker stats --no-stream "$NAME"

step "6. Stop"
run docker stop "$NAME"
run docker ps -a --filter "name=${NAME}"

step "7. Start again (same container, same data volume)"
run docker start "$NAME"
wait_healthy

step "8. Restart"
run docker restart "$NAME"
wait_healthy

step "9. Remove the container (the data volume stays)"
run docker rm -f "$NAME"
run docker ps -a --filter "name=${NAME}"
run docker volume ls --filter "name=${NAME}-data"

step "Done - evidence saved in $LOG"
