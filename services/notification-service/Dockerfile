# Session 13 — Lab 11A: Dockerize notification-service
#
# Single-stage Dockerfile — works but image is ~650 MB.
#
# TODO 1 (matches S9 pattern): Convert to multi-stage build.
#   Runtime stage: FROM eclipse-temurin:21-jre-jammy
#
# TODO 2: Add non-root user (uid 1001, group appuser).
#
# TODO 3: Add HEALTHCHECK polling /actuator/health on port 8085.
#   Install curl in the runtime stage first.

FROM maven:3.9-eclipse-temurin-21

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

WORKDIR /app
RUN cp /build/target/notification-service-*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
