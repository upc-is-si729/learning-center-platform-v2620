# ACME Learning Center Platform

ACME Learning Center Platform is a state-of-the-art RESTful API built on the foundations of Domain-Driven Design (DDD). Developed with Java 27, Spring Boot 4.1.1, and MySQL, the platform provides a robust and scalable solution for managing educational ecosystems. It features a modular architecture organized into bounded contexts and strictly adheres to Command Query Responsibility Segregation (CQRS) principles, ensuring a clean separation between data modification and retrieval operations.

---

## Tech Stack

- **Language:** Java 27 (with Project Loom Virtual Threads, Sequenced Collections, and Stream Gatherers)
- **Framework:** Spring Boot 4.1.1
- **Database:** MySQL 8+
- **Persistence:** Spring Data JPA / Hibernate with Physical Snake Case Naming Strategy
- **Concurrency:** Project Loom Virtual Threads (`spring.threads.virtual.enabled=true`)
- **API Standards:** RFC 9457 Problem Details for HTTP APIs
- **Security:** Spring Security with JWT (JSON Web Tokens) & BCrypt
- **Documentation:** SpringDoc OpenAPI (Swagger UI) & Structurizr DSL
- **Build Tool:** Maven 3.9+
- **Containerization:** Docker

---

## Prerequisites

- Java 27 JDK
- Maven 3.9+
- MySQL 8.0+
- Docker (optional)

---

## Project Structure

The project is organized into bounded contexts following Layered Domain-Driven Design (DDD):

```text
src/main/java/com/acme/center/platform/
├── iam/        # Identity and Access Management (Auth, Users, Roles, JWT, BCrypt)
├── learning/   # Learning context (Courses, Learning Paths, Students, Enrollments)
├── profiles/   # User Profile lifecycle (Names, Addresses, Email)
└── shared/     # Shared kernel (Domain base aggregate, Result type, ProblemDetail, Concurrency, i18n)
```

Each bounded context follows a classic 4-layer structure:
- `domain`: Aggregates, entities, value objects, domain events, and repository port interfaces.
- `application`: Command services, query services, internal implementations, event handlers, and outbound ACL ports.
- `infrastructure`: JPA persistence entities, repository adapters, assemblers, security configurations, and technology adapters.
- `interfaces`: REST controllers, resources, request/response assemblers, and ACL facades.

---

## Configuration

The project uses profile-based configuration located in `src/main/resources`:
- `application.properties`: Shared defaults, database naming strategy, and Virtual Threads configuration.
- `application-dev.properties`: Development settings (default).
- `application-prod.properties`: Production settings.

### Environment Variables

Required environment variables (especially for the `prod` profile):

| Variable                 | Description                | Default (Dev)                         |
|--------------------------|----------------------------|---------------------------------------|
| `DATABASE_URL`           | MySQL host address         | `localhost`                           |
| `DATABASE_PORT`          | MySQL port                 | `3306`                                |
| `DATABASE_NAME`          | Database name              | `learning_center_platform`            |
| `DATABASE_USER`          | Database username          | `root`                                |
| `DATABASE_PASSWORD`      | Database password          | `password`                            |
| `PORT`                   | Application port           | `8080`                                |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile      | `dev`                                 |
| `JWT_SECRET`             | Secret key for JWT signing | `replace-with-a-strong-random-secret` |

---

## Getting Started

### 1) Database Setup
Ensure MySQL is running and create the database specified in `DATABASE_NAME`.

### 2) Run the Application

#### Using Maven
```bash
# Run with default 'dev' profile
./mvnw clean spring-boot:run

# Run with specific profile and environment variables
SPRING_PROFILES_ACTIVE=prod \
DATABASE_URL=localhost \
DATABASE_PORT=3306 \
DATABASE_NAME=learning-center-os \
DATABASE_USER=root \
DATABASE_PASSWORD=password \
JWT_SECRET=your-secret \
PORT=8080 \
./mvnw clean spring-boot:run
```

#### Using Docker
```bash
# Build the image
docker build -t acme-learning-platform .

# Run the container
docker run -p 8080:8080 \
  -e DATABASE_URL=host.docker.internal \
  -e DATABASE_NAME=learning-center-os \
  -e DATABASE_USER=root \
  -e DATABASE_PASSWORD=password \
  -e JWT_SECRET=your-secret \
  acme-learning-platform
```

### 3) API Documentation
Once running, access the interactive documentation at:
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Scripts and Commands

- `mvn clean compile`: Compile the project.
- `mvn test`: Run unit and repository tests.
- `mvn package`: Package the application into a JAR file.
- `mvn spring-boot:run`: Launch the application.

Note: Fully configured for Java 27 with Project Loom virtual threads enabled.

---

## API Surface

### IAM (Identity and Access Management)
- `POST /api/v1/users`: Register a new user (Sign-up).
- `POST /api/v1/tokens`: Authenticate and obtain a JWT token (Sign-in).
- `GET /api/v1/users`: User query and management.
- `/api/v1/roles`: Role management.

### Learning
- `/api/v1/courses`: Course management.
- `/api/v1/courses/{courseId}/learning-path-items/{tutorialId}`: Learning path curation.
- `/api/v1/students`: Student management.
- `/api/v1/enrollments`: Enrollment management and tutorial progression.

### Profiles
- `/api/v1/profiles`: User profile management.

---

## Architecture & Development Conventions

- **Domain-Driven Design (DDD) & CQRS:** Write operations use `CommandService`, read operations use `QueryService`.
- **Aggregate Encapsulation:** Aggregates protect their transactional invariants without public `@Setter`s, using domain business methods and `reconstitute(...)` factories.
- **Identity Referencing:** Cross-aggregate references use strongly-typed ID value objects (`CourseId`, `AcmeStudentRecordId`, `ProfileId`).
- **Standardized Domain Event Dispatching:** Aggregates register events via `registerDomainEvent(...)`, which are automatically published by `DomainEventPublisher` upon persistence.
- **Virtual Threads (Project Loom):** Embedded Tomcat and asynchronous executors (`@Async`) run on lightweight virtual threads.
- **RFC 9457 Error Handling:** REST errors return standard `ProblemDetail` responses under `/api/v1/errors/{error-slug}` with localized messages and validation field mappings.
- **Sequenced Collections & Stream Gatherers:** Domain collection traversal leverages Java 21+ `SequencedCollection` and `java.util.stream.Gatherers`.

---

## Documentation

Refer to the `docs/` folder and project root for detailed specifications:
- [Architecture Decision Records (ADRs)](docs/adrs.md)
- [User Stories & API Technical Stories](docs/user-stories.md)
- [Class Diagram (PlantUML)](docs/class-diagram.puml)
- [Software Architecture (Structurizr DSL)](docs/software-architecture.dsl)
- [Contributing Guide](CONTRIBUTING.md)
- [Changelog](CHANGELOG.md)

---

## License

This project is licensed under the MIT License. See the [LICENSE.md](LICENSE.md) file for details.
