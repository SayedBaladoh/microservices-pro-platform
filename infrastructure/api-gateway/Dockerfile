# ─────────────────────────────────────────────
# Session 9 — Lab 8A: Dockerize api-gateway
# ─────────────────────────────────────────────
#
# Single-stage Dockerfile — works but image is ~650 MB.
#
# TODO 1 (Task 2): Convert to multi-stage build.
#   Runtime stage: FROM eclipse-temurin:21-jre-jammy
#   Copy only the compiled JAR from the builder stage.
#
# TODO 2 (Task 3): Add non-root user (uid 1001, group appuser) in runtime stage.
#
# TODO 3 (Task 3): Add HEALTHCHECK polling /actuator/health on port 8080.
#   Install curl in the runtime stage (apt-get install --no-install-recommends curl).

FROM maven:3.9-eclipse-temurin-21

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

WORKDIR /app
RUN cp /build/target/api-gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
