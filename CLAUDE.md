# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build the project (requires local dependencies to be installed first)
cd mangala-common-security && mvn clean install && cd ..
cd mangala-exception && mvn clean install && cd ..
mvn clean install

# Run the application (port 8082)
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=CreatePortfolioUseCaseTest

# Run a single test method
mvn test -Dtest=CreatePortfolioUseCaseTest#shouldCreatePortfolioSuccessfully
```

## Required Services for Local Development

The application requires these services (or disable them via environment variables):
- **PostgreSQL**: `localhost:5432` (DB: `mangala_dev`, schema: `portfolio`)
- **Redis**: `localhost:6379`
- **Kafka**: `localhost:9092` (or set `KAFKA_ENABLED=false` to disable)
- **OAuth2 JWT Issuer**: `localhost:8080` (for authentication)

## Architecture

This is a Spring Boot 3.4 microservice using **Clean Architecture** with a use-case-driven design pattern.

### Module Structure

Each domain module follows this structure:
```
{module}/
├── adapter/
│   ├── web/          # REST controllers and DTOs
│   └── repository/   # JPA repositories
├── domain/           # Entities and value objects
├── usecase/          # Use case interfaces
│   └── impl/         # Use case implementations
├── service/          # Domain services
└── event/            # Kafka consumers/producers
```

### Core Domains

- **portfolio**: CRUD operations for portfolios and wallet associations
- **holdings**: Aggregates wallet balances with real-time price data (via Kafka)
- **snapshot**: Historical portfolio snapshots for tracking performance

### Key Patterns

- **Use Cases**: Each operation is a separate use case with `Command` record input and `Response` record output
- **JWT Authentication**: User ID extracted from JWT subject (`@AuthenticationPrincipal Jwt jwt`)
- **MapStruct + Lombok**: Used together for DTO mapping (lombok-mapstruct-binding configured)
- **Redisson**: Distributed locking for concurrent operations
- **Flyway**: Database migrations in `src/main/resources/migration/`

### Local Dependencies

The service depends on two local libraries that must be installed first:
- `mangala-common-security`: Security utilities
- `mangala-exception`: Common exception handling

### Testing

Tests use H2 in-memory database with `application-test.yml` profile. Kafka and Redis are disabled/mocked in tests via `TestContainersConfig`.
