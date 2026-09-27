# Session 17 — Lab 13A: Observability (Three Pillars)

**Duration:** 2.5 hours online
**Services:** product-service + order-service (primary); all others benefit automatically
**Grading:** Feature 70% + Code Quality 20% + Design Choice badge 10%

## The Three Pillars

| Pillar | Tool | What it answers |
|---|---|---|
| Traces | Zipkin | "Where did this request spend its time?" |
| Metrics | Prometheus + Grafana | "How is the system behaving over time?" |
| Logs | JSON + traceId | "What happened in detail for this request?" |

## Task 1 — Add dependencies (15 min)

Add to `pom.xml` for **product-service** and **order-service**:

```xml
<!-- Distributed Tracing -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>

<!-- Prometheus metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Structured JSON logging -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

## Task 2 — Configure application.yml (15 min)

Add to both services:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
  tracing:
    sampling:
      probability: 1.0  # DEV ONLY — production: 0.1
    zipkin:
      endpoint: http://localhost:9411/api/v2/spans
```

## Task 3 — ObservabilityConfig + @Timed (20 min)

Create `ObservabilityConfig.java` in both services:

```java
@Configuration
public class ObservabilityConfig {
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
```

**Without this bean:** `@Timed` is silently ignored — no timing metrics appear.

Add `@Timed` to key methods:
```java
// product-service ProductService.java
@Timed(value = "product.findById", description = "Time to fetch a product by ID")
public Optional<Product> findById(Long id) { ... }

// order-service OrderService.java
@Timed(value = "order.create", description = "Time to create an order")
public OrderResponse createOrder(OrderRequest request) { ... }
```

## Task 4 — Structured JSON logging (15 min)

Create `src/main/resources/logback-spring.xml` in both services:

```xml
<configuration>
    <springProperty scope="context" name="appName" source="spring.application.name"/>
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"application":"${appName}"}</customFields>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
    </root>
</configuration>
```

Every log line now includes `traceId` and `spanId` as JSON fields.

## Task 5 — Add Zipkin + Prometheus + Grafana to docker-compose.yml (15 min)

Add the three service blocks from `docker-compose.yml` delta:
- `zipkin` (port 9411)
- `prometheus` (port 9090, mounts `observability/prometheus.yml`)
- `grafana` (port 3000, mounts datasource auto-provisioning)

Create `observability/prometheus.yml` with scrape configs for all 8 services.

## Verification

```bash
docker compose up -d zipkin prometheus grafana

# 1. Traces
curl http://localhost:8080/api/v1/products
open http://localhost:9411   # find the trace, see product-service span

# 2. Metrics
open http://localhost:9090   # Prometheus UI
# Query: http_server_requests_seconds_count{application="product-service"}

# 3. Custom metric
open http://localhost:9090
# Query: product_findById_seconds_count

# 4. Grafana
open http://localhost:3000   # admin/admin
# Add dashboard, query: product_findById_seconds_sum / product_findById_seconds_count

# 5. Structured logs
docker compose logs product-service | head -5
# Should be JSON, with traceId field
```

## ⚖ ENGINEERING DECISION — Sampling Rate

| Rate | Use case | Trade-off |
|---|---|---|
| `1.0` (100%) | DEV — trace everything | High Zipkin load in production |
| `0.1` (10%) | Production default | Miss some traces, but manageable load |
| `0.01` (1%) | High-traffic production | Very low overhead, coarse visibility |

Always use `1.0` in dev. Never use `1.0` in production.

## Acceptance criteria

- [ ] `GET /actuator/prometheus` on product-service returns metrics (200 OK)
- [ ] `product_findById_seconds_count` appears in Prometheus UI
- [ ] Zipkin shows a trace with spans from product-service
- [ ] Log lines contain `traceId` JSON field
- [ ] Grafana datasource shows "Data source is working"
- [ ] Commit: `session-17: add-observability-zipkin-prometheus-grafana`
