# Contributing to ACME Learning Center Platform

Thank you for contributing to the **ACME Learning Center Platform**! This guide outlines our architectural standards, coding guidelines, branch strategy, and commit conventions.

---

## 1. Code of Conduct

We are committed to providing a welcoming, inclusive, and professional environment for all contributors. Please treat everyone with respect and empathy.

---

## 2. Architecture & Design Principles

The platform is designed as a **Modular Monolith** using **Domain-Driven Design (DDD)** and **Hexagonal Architecture (Ports and Adapters)**:

- **Bounded Contexts**: All business features reside in dedicated context packages (`iam`, `profiles`, `learning`, or `shared`).
- **Layer Isolation**:
  - `domain`: Contains pure aggregates, entities, value objects, domain events, and repository interfaces. Must not depend on Spring or JPA annotations.
  - `application`: Coordinates use cases via Command Services and Query Services. Contains event handlers and inbound ACL ports.
  - `infrastructure`: Implements persistence adapters (`*RepositoryImpl`), JPA entities (`*PersistenceEntity`), security configurations, and external integrations. Packaging follows `infrastructure.<technology-type>.<manufacturer-technology-name>.configuration`.
  - `interfaces`: REST controllers, DTO resources, request/response assemblers following `[Target]From[Source]Assembler` naming, and ACL facades.
- **Aggregate Encapsulation**: Do NOT use Lombok `@Setter` on aggregates or entities. Expose intention-revealing domain methods and provide `reconstitute(...)` static factories for persistence rehydration.
- **Identity Referencing**: Aggregates must reference other aggregates by their strongly-typed identity value objects (e.g., `CourseId`, `AcmeStudentRecordId`, `ProfileId`), never by direct object references.
- **Error Handling**: Use `Result<T, ApplicationError>` in application layers and `ProblemDetailFromApplicationErrorAssembler` in REST controllers to return RFC 9457 `ProblemDetail` responses.

---

## 3. Development Workflow

1. **Fork or create a branch** from `main` or the active sprint feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Build and test locally**:
   ```bash
   mvn clean compile
   mvn test
   ```
3. **Verify style & linting**: Ensure no compile warnings, nullability issues (using JSpecify `@NullMarked`), or broken tests.
4. **Submit a Pull Request** with a concise description of changes, linking to relevant technical stories or ADRs.

---

## 4. Conventional Commits Standard

We strictly adhere to the [Conventional Commits](https://www.conventionalcommits.org/) specification (v1.0.0).

### Commit Format
```text
<type>(<scope>): <short summary>

[optional body]

[optional footer(s)]
```

### Allowed Types
- **`feat`**: A new feature or capability.
- **`fix`**: A bug fix.
- **`refactor`**: A code change that neither fixes a bug nor adds a feature (e.g., encapsulation, performance improvement).
- **`test`**: Adding missing tests or correcting existing tests.
- **`docs`**: Documentation only changes.
- **`chore`**: Maintenance, build tool, or dependency updates.

### Allowed Scopes
- `iam`, `learning`, `profiles`, `shared`, `config`, `docs`

### Examples
- `feat(learning): introduce CourseId value object`
- `refactor(learning): decouple Enrollment aggregate from Course aggregate root`
- `feat(shared): introduce Loom concurrency configuration with virtual thread task executor`
- `docs(adrs): document Architecture Decision Records for DDD modernizations`

---

## 5. Testing Guidelines

- Unit tests must be fast, deterministic, and isolated.
- Mock external dependencies where appropriate using Mockito.
- Cover normal execution flows, boundary conditions, and negative error branches.
- Maintain test coverage for newly introduced value objects, assemblers, domain algorithms, and repository event publishing lifecycles.
