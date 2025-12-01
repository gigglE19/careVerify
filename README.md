# CareVerify — Provider Verification & Network Eligibility API

CareVerify is a small Spring Boot microservice that validates healthcare providers
against the public NPI Registry. It normalizes data from the registry, applies
simple business validation rules (state and specialty), and returns a concise
eligibility response. The service is implemented using Spring WebFlux for
non-blocking I/O, uses a local Caffeine cache for fast responses, and includes
correlation-id tracing for end-to-end request logging.

Key features
- Reactive, asynchronous server using Spring WebFlux (Netty)
- Non-blocking HTTP client with correlation-id propagation
- Circuit breaker protection for external calls (configured via Spring)
- Caffeine in-memory cache with fire-and-forget updates
- Structured logging with SLF4J and MDC-based correlation id
- OpenAPI (Swagger) UI for API exploration

Quickstart — build and run locally
1) Build the project (from repo root):
```bash
mvn -T1C clean package -DskipTests
```
2) Run the API module (from repo root):
```bash
cd careverify-api
mvn spring-boot:run
```
3) Open the OpenAPI/Swagger UI in your browser:
http://localhost:8080/swagger-ui.html

4) Example request (replace NPI and query params as needed):
```bash
curl -H "X-Correlation-Id: demo-1" "http://localhost:8080/api/v1/providers/verify?npi=1194276360&state=NY"
```

What to look for in logs
- Server request/response logs include the correlation id.
- WebClient request/response logs show outgoing external calls and their correlation id.
- Cache logs show MISS (when a lookup triggers a backend fetch), and scheduled/complete
  messages when the in-memory cache is updated.

Notes for reviewers
- The project was implemented with an emphasis on reactive programming,
  observable logs, and clear separation of adapter, core, cache, and API layers.
- To prepare for submission, exclude editor/project metadata (for example the
  `.idea/` directory) from the published repository.

License / attribution
- This project is provided for demonstration and interview purposes.
  Use and adapt as needed.
