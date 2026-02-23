# Security

## OWASP Top 10 Coverage

| # | Category | Status | Details |
|---|----------|--------|---------|
| A01 | Broken Access Control | ✅ Mitigated | Product write routes are restricted to admins in `src/main/java/com/shivam/store/config/SecurityConfig.java`, and profile read/update/delete paths enforce self-or-admin checks in `src/main/java/com/shivam/store/controllers/UserController.java`. Order and cart ownership checks are enforced in `OrderService` and `CartService`. |
| A02 | Cryptographic Failures | ✅ Mitigated | Password hashing uses BCrypt (`PasswordEncoder` in `src/main/java/com/shivam/store/config/SecurityConfig.java`). JWT and Stripe secrets are read from environment-backed properties (`application.properties`), and no production secret is hardcoded in local profile config. |
| A03 | Injection | ✅ Mitigated | Persistence uses Spring Data JPA parameterized queries/repository methods (no string-built SQL in repository layer). Request validation is enforced with Jakarta Bean Validation across auth/user/product/cart DTOs and controller `@Valid` boundaries. |
| A04 | Insecure Design | ✅ Mitigated | Stripe webhook events require signature verification via Stripe SDK in `src/main/java/com/shivam/store/payments/StripePaymentGateway.java`. Guest cart promotion now requires a valid, non-expired guest JWT before merge (`CartOwnershipService` and `CartOwnerArgumentResolver`). |
| A05 | Security Misconfiguration | ✅ Mitigated | Security headers (CSP, HSTS, frame/content/referrer protections) are explicitly configured in `SecurityConfig`. CORS uses an explicit allowlist (`app.cors.allowed-origins`) instead of wildcard. Production error detail leakage is reduced via `server.error.*` settings and generic global exception handling. |
| A06 | Vulnerable and Outdated Components | ⚠️ CI Managed | Dependency freshness and CVE scanning should be enforced in CI pipelines (outside this hardening patch). |
| A07 | Identification and Authentication Failures | ✅ Mitigated | Login endpoint rate limiting (10 attempts/minute per IP) is enforced in `src/main/java/com/shivam/store/filters/LoginRateLimitFilter.java` with Redis-backed counting and local fallback. Access/refresh token expirations are configured and enforced in `JwtService`, and login failure responses remain generic. |
| A08 | Software and Data Integrity Failures | ✅ Mitigated | Webhook signature validation is mandatory before processing payment events. Redis polymorphic cache deserialization was restricted to expected package allowlists in `CacheConfig` to reduce unsafe type materialization risk. |
| A09 | Security Logging and Monitoring Failures | ✅ Mitigated | Structured security-event logs were added for failed logins, auth failures/forbidden access, login rate-limit triggers, rejected JWTs, Stripe webhook verification failures, and unexpected server errors. Sensitive values (passwords/tokens/card data) are not logged. |
| A10 | Server-Side Request Forgery (SSRF) | ✅ Not Applicable | The backend does not expose user-controlled URL fetch functionality (no endpoint accepts an arbitrary URL and performs server-side requests). |
