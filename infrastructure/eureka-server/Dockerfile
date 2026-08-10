# ─────────────────────────────────────────────
# Stage 1 — builder
# Compiles the JAR. The JDK, Maven, and all build
# tools stay in this stage and never reach production.
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml first — Docker caches this layer.
# If pom.xml hasn't changed, Maven dependencies are
# not re-downloaded on the next build. Changing only
# src/ code hits the cache here and skips the download.
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ─────────────────────────────────────────────
# Stage 2 — runtime
# JRE-only image (~250 MB vs ~650 MB single-stage).
# No JDK, no Maven, no source code.
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS runtime

# Install curl for the HEALTHCHECK below.
# --no-install-recommends keeps the layer minimal.
# rm -rf clears the apt cache so it is not baked into the layer.
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Non-root user — security principle (Session 9, Knowledge Check Q2):
# If the container process is compromised, a non-root user limits the
# blast radius of a container breakout.
# uid/gid 1001 is consistent across all platform services.
RUN groupadd --system --gid 1001 appuser \
    && useradd --system --uid 1001 --gid appuser --no-create-home appuser

WORKDIR /app

COPY --from=builder /build/target/eureka-server-*.jar app.jar

# Switch to non-root before the process starts.
USER appuser

EXPOSE 8761

# HEALTHCHECK — polls Spring Boot Actuator every 10s.
# start_period gives the JVM time to start before failures count.
HEALTHCHECK --interval=10s --timeout=5s --start-period=40s --retries=5 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
