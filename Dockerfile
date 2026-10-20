# syntax=docker/dockerfile:1
# Vehicle Service Booking Portal - multi-stage image.
#   docker build -t vehicle-service-portal:1.0.0 .
#   docker run -d --name vsbp -p 9090:8080 -v vsbp-data:/data vehicle-service-portal:1.0.0

# ---- Stage 1: build the WAR ---------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only the pom first so the dependency layer is cached until the pom changes
COPY pom.xml .
RUN mvn -B -ntp -q dependency:go-offline || true

COPY src ./src
ARG APP_ENV=docker
# maven.test.skip: tests already ran in the Jenkins pipeline; skipping also avoids downloading Selenium here
RUN mvn -B -ntp -Dmaven.test.skip=true -Dapp.environment=${APP_ENV} clean package

# ---- Stage 2: small runtime image ---------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S vsbp && adduser -S vsbp -G vsbp \
    && mkdir /data && chown vsbp:vsbp /data
WORKDIR /app
COPY --from=build /workspace/target/vehicle-service-portal.war /app/app.war

# Configuration through environment variables (override with docker run -e ...)
ENV APP_ENVIRONMENT=docker \
    DB_URL=jdbc:h2:file:/data/vsbp \
    JAVA_OPTS="-Xms128m -Xmx384m"

EXPOSE 8080
VOLUME /data
USER vsbp

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.war"]
