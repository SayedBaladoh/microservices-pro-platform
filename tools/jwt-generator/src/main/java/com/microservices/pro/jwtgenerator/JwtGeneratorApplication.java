package com.microservices.pro.jwtgenerator;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/**
 * JwtGeneratorApplication — Session 3 training tool.
 *
 * Training Note: In this session we are VALIDATING JWTs, not ISSUING
 * them. Token generation belongs to the Identity Provider (Session 20).
 * This JWT Generator tool exists only to simulate an external Identity
 * Provider during training, and will be replaced entirely by a real
 * Authorization Server (Keycloak/OAuth2) in Session 20.
 *
 * Usage (built jar — the documented default):
 *   java -jar jwt-generator.jar --username=ahmed --roles=ROLE_ADMIN
 *   java -jar jwt-generator.jar --username=sara --roles=ROLE_CUSTOMER --expiry=10s
 *
 * Or from an IDE / Maven directly (secondary option):
 *   mvn spring-boot:run -Dspring-boot.run.arguments="--username=ahmed --roles=ROLE_ADMIN"
 *
 * SECRET: must match infrastructure/api-gateway/src/main/resources/application.yml's
 * jwt.secret exactly, or every token this tool prints will fail signature
 * validation at the Gateway with no useful error for trainees. Both modules
 * read the JWT_SECRET environment variable first; if it isn't set, both fall
 * back to the SAME hardcoded dev-placeholder string below. Prefer setting
 * JWT_SECRET in your shell over relying on the fallback.
 *
 * See docs/labs/session-03-jwt-testing.md for full usage and test scenarios
 * (admin token, user token, expired token, tampered token).
 */
@SpringBootApplication
public class JwtGeneratorApplication implements CommandLineRunner {

    // DEV ONLY — must be identical to the fallback in api-gateway's
    // application.yml (jwt.secret). NEVER reuse this string in any real system.
    private static final String DEFAULT_SECRET =
            "microservices-pro-course-dev-secret-key-2026-min-256-bits";

    private static final Duration DEFAULT_EXPIRY = Duration.ofMinutes(30);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JwtGeneratorApplication.class);
        app.setLogStartupInfo(false);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        String username = argValue(args, "username", "trainee");
        String roles = argValue(args, "roles", "ROLE_CUSTOMER");
        Duration expiry = parseExpiry(argValue(args, "expiry", null));

        String secret = System.getenv().getOrDefault("JWT_SECRET", DEFAULT_SECRET);
        SecretKey signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiry.toMillis());

        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", roles)
                .setIssuedAt(now)
                .setExpiration(expiresAt)
                .signWith(signingKey)
                .compact();

        System.out.println();
        System.out.println("Subject (X-User-Id) : " + username);
        System.out.println("Role    (X-User-Role): " + roles);
        System.out.println("Expires at           : " + expiresAt);
        System.out.println();
        System.out.println("Authorization: Bearer " + token);
        System.out.println();
        System.out.println("Paste the full \"Authorization: Bearer ...\" line above into");
        System.out.println("Postman or curl -H. See docs/labs/session-03-jwt-testing.md");
        System.out.println("for tampered/expired test scenarios.");
        System.out.println();
    }

    private static String argValue(String[] args, String name, String defaultValue) {
        String prefix = "--" + name + "=";
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return defaultValue;
    }

    /**
     * Parses values like "30m", "10s", "1h". Falls back to the 30-minute
     * default if --expiry is not provided. Deliberately no "1y" / "never"
     * option here — see the class-level training note on realistic lifetimes.
     */
    private static Duration parseExpiry(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_EXPIRY;
        }
        String value = raw.trim();
        char unit = value.charAt(value.length() - 1);
        long amount = Long.parseLong(value.substring(0, value.length() - 1));

        return switch (unit) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            default -> throw new IllegalArgumentException(
                    "Unrecognized --expiry unit '" + unit + "'. Use s, m, or h (e.g. --expiry=10s).");
        };
    }
}
