# ─────────────────────────────────────────────
# Session 9 — Lab 8A: Dockerize eureka-server
# ─────────────────────────────────────────────
#
# This is a single-stage Dockerfile — it works, but it includes the
# entire JDK and Maven in the final image (~650 MB). Your job in Lab 8A
# is to convert it to a multi-stage build.
#
# TODO 1 (Task 2): Convert to a multi-stage build.
#   Add a second FROM stage using eclipse-temurin:21-jre-jammy.
#   The builder stage below (maven:3.9-eclipse-temurin-21) compiles the JAR;
#   the runtime stage copies only the JAR. No JDK, no Maven, no source code.
#   Expected result: image shrinks from ~650 MB to ~250 MB.
#
# TODO 2 (Task 3): Add a non-root user to the runtime stage.
#   groupadd --system --gid 1001 appuser
#   useradd  --system --uid 1001 --gid appuser --no-create-home appuser
#   Switch to it with USER appuser before ENTRYPOINT.
#   Why: see Knowledge Check Q2 — root inside container = security risk.
#
# TODO 3 (Task 3): Add a HEALTHCHECK directive.
#   HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
#       CMD curl -f http://localhost:8761/actuator/health || exit 1
#   Requires curl in the runtime image — add to apt-get install step.

FROM maven:3.9-eclipse-temurin-21

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

WORKDIR /app
RUN cp /build/target/eureka-server-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
