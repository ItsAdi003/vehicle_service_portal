# 8. Docker image and container lifecycle (Task 12)

## 8.1 The Dockerfile
See [`Dockerfile`](../Dockerfile). It is a **multi-stage** build:

| Stage | Base image | Purpose |
|-------|-----------|---------|
| `build` | `maven:3.9-eclipse-temurin-17` | Downloads dependencies (cached layer), runs `mvn package` and produces `vehicle-service-portal.war` |
| runtime | `eclipse-temurin:17-jre-alpine` | Small JRE-only image that runs `java -jar /app/app.war` |

Design decisions:

| Decision | Why |
|----------|-----|
| Multi-stage | The final image has no Maven, no source code, no build cache |
| Alpine JRE | Small image, quick pulls |
| Non-root user `vsbp` | Least privilege |
| `VOLUME /data` + `DB_URL=jdbc:h2:file:/data/vsbp` | The H2 database survives container removal |
| `ENV APP_ENVIRONMENT`, `JAVA_OPTS` | Configuration without rebuilding (`docker run -e ...`) |
| `HEALTHCHECK` on `/actuator/health` | Docker reports `healthy` / `unhealthy` and orchestrators can react |
| `.dockerignore` | Small build context, no secrets or local database files |
| `ARG APP_ENV` | Jenkins passes the environment label into the build |

## 8.2 Container lifecycle commands

Run everything in one go and record the output as evidence:

```bash
bash scripts/docker-lifecycle-demo.sh 1.0.0                                    # Linux / macOS / Git Bash
powershell -ExecutionPolicy Bypass -File scripts\docker-lifecycle-demo.ps1     # Windows PowerShell
```
The log is written to `evidence/docker-lifecycle.log` (the folder is git-ignored; attach the file to your report).

Or step by step:

| Phase | Command | What to look for |
|-------|---------|------------------|
| Build and tag | `docker build -t vehicle-service-portal:1.0.0 -t vehicle-service-portal:latest .` | `naming to docker.io/library/vehicle-service-portal:1.0.0` |
| List images | `docker images vehicle-service-portal` | Two tags, same IMAGE ID, size |
| Inspect image | `docker image inspect vehicle-service-portal:1.0.0 --format "{{.Config.ExposedPorts}} {{.Config.User}}"` | `map[8080/tcp:{}] vsbp` |
| Run + map port | `docker run -d --name vsbp -p 9090:8080 -v vsbp-data:/data vehicle-service-portal:1.0.0` | prints the container id |
| Running containers | `docker ps` | `0.0.0.0:9090->8080/tcp`, status `Up ... (healthy)` after ~45 s |
| Open the app | <http://localhost:9090/> and <http://localhost:9090/actuator/health> | Home page, `{"status":"UP"}` |
| Logs | `docker logs --tail 20 vsbp` (add `-f` to follow) | `Started VehicleServicePortalApplication` |
| Shell / command inside | `docker exec -it vsbp sh` or `docker exec vsbp ls /data` | `vsbp.mv.db` after first start |
| Resource use | `docker stats --no-stream vsbp` | CPU %, memory |
| Stop | `docker stop vsbp` | `docker ps -a` shows `Exited` |
| Start again | `docker start vsbp` | Data from before is still there (volume) |
| Restart | `docker restart vsbp` | `STATUS` shows a new uptime |
| Remove container | `docker rm -f vsbp` | gone from `docker ps -a`; `docker volume ls` still shows `vsbp-data` |
| Remove image / volume (optional) | `docker rmi vehicle-service-portal:1.0.0`, `docker volume rm vsbp-data` | cleanup |

### Lifecycle diagram (states)
`docker build` -> **image** -> `docker run` -> **running** <-> `docker stop` / `docker start` -> **exited** -> `docker rm` -> **removed**
(`docker restart` = stop + start; `docker logs`, `exec`, `inspect` work while running).

## 8.3 Publish the image
```bash
docker login
docker tag vehicle-service-portal:1.0.0 <dockerhub-user>/vehicle-service-portal:1.0.0
docker push <dockerhub-user>/vehicle-service-portal:1.0.0
```
In the pipeline this is automated (next chapter). Evidence: the repository page on hub.docker.com showing the version tag.

## 8.4 Evidence for Task 12
Dockerfile; `docker-lifecycle.log`; screenshot of `docker ps` showing `(healthy)` and the port mapping; screenshot of the application
at `http://localhost:9090/`; screenshot of `docker images`.
