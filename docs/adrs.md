# Architecture Decision Records (ADRs) — ACME Learning Center Platform

This document maintains Architecture Decision Records (ADRs) for the **ACME Learning Center Platform** following the [Michael Nygard format](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

---

## Index

- [ADR-001: Modular Monolith with Layered Architecture and Domain-Driven Design](#adr-001-modular-monolith-with-layered-architecture-and-domain-driven-design)
- [ADR-002: Command Query Responsibility Segregation (CQRS) Pattern](#adr-002-command-query-responsibility-segregation-cqrs-pattern)
- [ADR-003: Anti-Corruption Layer (ACL) and Context Facades for Cross-Context Integration](#adr-003-anti-corruption-layer-acl-and-context-facades-for-cross-context-integration)
- [ADR-004: Decoupling Domain Aggregates from JPA Persistence Entities](#adr-004-decoupling-domain-aggregates-from-jpa-persistence-entities)
- [ADR-005: Functional Error Handling with Sealed Result Type](#adr-005-functional-error-handling-with-sealed-result-type)
- [ADR-006: Stateless JWT Authentication and Spring Security Integration](#adr-006-stateless-jwt-authentication-and-spring-security-integration)
- [ADR-007: Standardized Technology Packaging Taxonomy for Infrastructure](#adr-007-standardized-technology-packaging-taxonomy-for-infrastructure)
- [ADR-008: Aggregate Invariant Encapsulation and Reconstitution Factory Pattern](#adr-008-aggregate-invariant-encapsulation-and-reconstitution-factory-pattern)
- [ADR-009: Standardized Domain Event Dispatching via DomainEventPublisher](#adr-009-standardized-domain-event-dispatching-via-domaineventpublisher)
- [ADR-010: Aggregate References by Identity (CourseId)](#adr-010-aggregate-references-by-identity-courseid)
- [ADR-011: Adoption of RFC 9457 Problem Details for REST Error Responses](#adr-011-adoption-of-rfc-9457-problem-details-for-rest-error-responses)
- [ADR-012: Virtual Threads and Concurrency Optimization via Project Loom](#adr-012-virtual-threads-and-concurrency-optimization-via-project-loom)
- [ADR-013: Modern Sequenced Collections and Stream Gatherers in Domain Model](#adr-013-modern-sequenced-collections-and-stream-gatherers-in-domain-model)

---

## ADR-001: Modular Monolith with Layered Architecture and Domain-Driven Design

### Status
Accepted

### Context
The platform requires clear boundaries between distinct business domains (IAM, Student Profiles, Learning Lifecycle) while maintaining development velocity, transactional simplicity, and operational manageability without the distributed systems overhead of microservices.

### Decision
We structure the system as a **Modular Monolith** applying **Domain-Driven Design (DDD)** and **Classic 4-Layer Architecture**:
1. **Bounded Contexts**: `iam`, `profiles`, `learning`, and `shared`.
2. **Layer Separation**: Each context defines `domain`, `application`, `infrastructure`, and `interfaces`.
3. **Dependency Rule**: Dependencies point inward toward the `domain` layer. Domain classes must never depend on application, infrastructure, or interface layers.
4. **Anti-Corruption Layer (ACL)**: Cross-context communication is governed through explicit facades (`IamContextFacade`, `ProfilesContextFacade`) and domain integration events.

### Consequences
- **Positive**: Strict domain boundaries, high testability of business logic without database dependencies, clean evolution path toward independent services if needed.
- **Negative**: Requires mapping layers (`*PersistenceAssembler`, `*ResourceAssembler`) between domain models, persistence entities, and REST resources.

---

## ADR-002: Command Query Responsibility Segregation (CQRS) Pattern

### Status
Accepted

### Context
In complex domains, models optimized for state mutation (invariants, validations, transactional consistency) often diverge from models required for presentation and query retrieval. Mixing read and write logic into monolithic service classes leads to bloated controllers and leaked business logic.

### Decision
We implement a lightweight **Command Query Responsibility Segregation (CQRS)** pattern across all bounded contexts:
1. **Command Services**: Dedicated interfaces (`*CommandService`) accepting immutable Java `record` commands (e.g., `CreateCourseCommand`, `SignInCommand`) for state-changing operations and returning `Result<T, ApplicationError>`.
2. **Query Services**: Dedicated interfaces (`*QueryService`) accepting immutable Java `record` queries (e.g., `GetCourseByIdQuery`, `GetAllEnrollmentsQuery`) for read-only operations and returning domain aggregates or collections.
3. **Immutability**: All commands and queries are immutable Java `record`s.

### Consequences
- **Positive**: Clear separation of side-effecting operations and read operations, easier optimization of query paths, highly testable service contracts.
- **Negative**: Increases the number of interfaces and record types per aggregate root.

---

## ADR-003: Anti-Corruption Layer (ACL) and Context Facades for Cross-Context Integration

### Status
Accepted

### Context
Bounded contexts need to interact (e.g., the `learning` context needs to verify or create profiles managed by the `profiles` context). Allowing direct dependencies between internal aggregate repositories across contexts creates tight coupling and violates bounded context autonomy.

### Decision
We implement **Anti-Corruption Layers (ACL)** and **Context Facades** for cross-context collaboration:
1. **Exposed Facades**: Contexts expose an interface in `interfaces.acl` (e.g., `ProfilesContextFacade`, `IamContextFacade`) backed by an internal implementation in `application.acl` that delegates strictly to local command/query services.
2. **Outbound Adapters**: Consuming contexts interact with external facades through outbound adapter services in `application.internal.outboundservices.acl` (e.g., `ExternalProfileService`).
3. **Integration Events**: Asynchronous cross-context notifications are published as integration events (e.g., `ProfileCreatedIntegrationEvent` triggering `LearningProfileCreatedEventHandler`).

### Consequences
- **Positive**: Bounded contexts remain autonomous; changes to internal context structures do not break external consumers.
- **Negative**: Requires intermediary facade methods and event translation boilerplate.

---

## ADR-004: Decoupling Domain Aggregates from JPA Persistence Entities

### Status
Accepted

### Context
Using JPA annotations directly on Domain Aggregate Roots couples core business logic and invariants to ORM frameworks, database schema constraints, and no-arg constructor requirements.

### Decision
We strictly separate the **Domain Model** from the **Persistence Model**:
1. **Domain Aggregates**: Pure Java classes extending `AbstractDomainAggregateRoot<T>`, residing in `domain.model.aggregates` with zero JPA annotations.
2. **Persistence Entities**: Reside in `infrastructure.persistence.jpa.entities`, extending `AuditableAbstractPersistenceEntity` and decorated with JPA annotations (`@Entity`, `@Table`, `@Column`).
3. **Persistence Assemblers**: Static assemblers (`*PersistenceAssembler`) in `infrastructure.persistence.jpa.assemblers` translate bi-directionally between domain aggregates and persistence entities.

### Consequences
- **Positive**: Domain models remain pure, expressive, and testable with pure unit tests without Spring or database contexts.
- **Negative**: Requires bidirectional mapping logic and manual sync between persistence entities and domain aggregates.

---

## ADR-005: Functional Error Handling with Sealed Result Type

### Status
Accepted

### Context
Using runtime exceptions for business rule violations and expected domain flow control creates hidden control flow, performance overhead from stack traces, and uncommunicative method signatures.

### Decision
We introduce a functional, sealed `Result<T, E>` algebraic data type in `com.acme.center.platform.shared.application.result`:
1. **Sealed Interface**: `Result<T, E> permits Success, Failure`, supporting exhaustive Java `switch` pattern matching and record deconstruction.
2. **Standardized ApplicationError**: `ApplicationError(code, message, details)` record captures business failure reasons without throwing exceptions.
3. **Assembly to HTTP**: `ResponseEntityAssembler` and `ProblemDetailFromApplicationErrorAssembler` transform `Result.Failure` into appropriate HTTP responses.

### Consequences
- **Positive**: Explicit error handling in method signatures, exhaustive pattern matching, high performance without unnecessary stack trace generation.
- **Negative**: Service callers must unpack `Result` instances via `fold` or pattern matching rather than relying on global catch blocks.

---

## ADR-006: Stateless JWT Authentication and Spring Security Integration

### Status
Accepted

### Context
The platform requires secure authentication and role-based authorization for REST endpoints while maintaining statelessness suitable for cloud horizontal scaling.

### Decision
We implement stateless JWT (JSON Web Token) authentication integrated with Spring Security:
1. **JWT Generation & Validation**: `TokenService` / `TokenServiceImpl` issues and validates signed HMAC-SHA256 JWT tokens.
2. **Password Hashing**: `HashingService` / `HashingServiceImpl` uses BCrypt password hashing.
3. **Security DSL**: `WebSecurityConfiguration` configures stateless session management (`SessionCreationPolicy.STATELESS`), method security (`@EnableMethodSecurity`), and JWT bearer token extraction filters.
4. **Roles**: Standardized roles `ROLE_USER`, `ROLE_INSTRUCTOR`, `ROLE_ADMIN` seeded automatically on startup via `ApplicationReadyEventHandler`.

### Consequences
- **Positive**: Stateless, scalable authentication; standards-compliant Bearer token authorization; declarative endpoint protection with `@PreAuthorize`.
- **Negative**: Tokens cannot be revoked before expiration without maintaining a distributed token blacklist.

---

## ADR-007: Standardized Technology Packaging Taxonomy for Infrastructure

### Status
Accepted

### Context
Infrastructure components (persistence, security, concurrency, documentation, internationalization) were previously placed inconsistently across packages, making technology-specific configurations difficult to locate.

### Decision
We standardize infrastructure layer packaging following the explicit taxonomy:
`infrastructure.<technology-type>.<manufacturer-technology-name>.configuration`

Examples across contexts:
- `shared.infrastructure.persistence.jpa.configuration` (Jakarta Persistence / Hibernate)
- `shared.infrastructure.documentation.openapi.configuration` (SpringDoc OpenAPI)
- `shared.infrastructure.concurrency.loom.configuration` (Project Loom Virtual Threads)
- `shared.infrastructure.i18n.configuration` (Spring Localization)
- `iam.infrastructure.authorization.sfs.configuration` (Spring Framework Security)

### Consequences
- **Positive**: Predictable, consistent architecture navigation across all bounded contexts and shared modules.
- **Negative**: Deeper package hierarchies.

---

## ADR-008: Aggregate Invariant Encapsulation and Reconstitution Factory Pattern

### Status
Accepted

### Context
Aggregate roots (`Enrollment`, `Student`, `Course`, `User`, `Profile`) originally exposed public `@Setter` methods for JPA/Lombok compatibility. This leaked encapsulation, allowing callers to mutate internal state directly and bypass critical domain invariants and business rules.

### Decision
1. Eliminate all public setters on Aggregate Roots and inner Entities.
2. Require all state mutations to pass through expressive domain business methods (e.g., `confirm()`, `cancel()`, `completeTutorial()`, `updateInformation()`, `addRole()`).
3. Introduce dedicated static `reconstitute(...)` factory methods for persistence assemblers to rehydrate existing entity state from the database without triggering domain creation invariants or domain events.

### Consequences
- **Positive**: Aggregates strictly protect their transactional and consistency boundaries. Accidental invalid state transitions are eliminated at compile time.
- **Negative**: Persistence assemblers must explicitly pass all persistent fields into reconstitution methods.

---

## ADR-009: Standardized Domain Event Dispatching via DomainEventPublisher

### Status
Accepted

### Context
Domain Aggregate Roots accumulate domain events via `registerDomainEvent(...)` from `AbstractDomainAggregateRoot`. However, because domain aggregates are not Spring Data JPA entities, Spring Data's `@DomainEvents` does not fire automatically upon repository `save()`. Event publication was previously implemented ad-hoc or omitted across repository adapters.

### Decision
1. Introduce `DomainEventPublisher` component in `com.acme.center.platform.shared.infrastructure.events`.
2. Inject `DomainEventPublisher` into all domain repository adapters (`EnrollmentRepositoryImpl`, `StudentRepositoryImpl`, `CourseRepositoryImpl`, `UserRepositoryImpl`, `ProfileRepositoryImpl`).
3. Standardize repository `save()` implementations to extract pending events via `aggregate.domainEvents()`, publish them through Spring's `ApplicationEventPublisher`, and clear the aggregate's event collection via `aggregate.clearDomainEvents()`.

### Consequences
- **Positive**: Uniform event dispatching lifecycle across all bounded contexts. Domain event handlers (`TutorialCompletedEventHandler`, `ProfileCreatedEventHandler`) reliably receive and process cross-boundary side effects.
- **Negative**: Repository adapters must coordinate with `DomainEventPublisher` during persistence.

---

## ADR-010: Aggregate References by Identity (CourseId)

### Status
Accepted

### Context
`Enrollment` directly referenced the `Course` aggregate root (`private Course course;`), violating the DDD principle that aggregate roots must reference other aggregate roots strictly by identity. This forced unnecessary loading of the entire `Course` aggregate and coupled the persistence lifecycles of `Enrollment` and `Course`.

### Decision
1. Introduce the `CourseId` value object record in `learning.domain.model.valueobjects`.
2. Refactor `Enrollment` to reference `CourseId` instead of `Course`.
3. Refactor operations requiring `LearningPath` (`confirm(LearningPath)`, `completeTutorial(TutorialId, LearningPath)`) to receive the learning path explicitly from `EnrollmentCommandServiceImpl`.
4. Introduce `CourseIdPersistenceConverter` for JPA persistence mapping.

### Consequences
- **Positive**: Enforces aggregate boundary isolation, reduces memory footprint during enrollment retrieval, and establishes consistent identity referencing alongside `AcmeStudentRecordId` and `ProfileId`.
- **Negative**: Application command service coordinates loading the `LearningPath` before delegating to `Enrollment` domain methods.

---

## ADR-011: Adoption of RFC 9457 Problem Details for REST Error Responses

### Status
Accepted

### Context
REST API error responses used custom `ErrorResource` JSON schemas. Modern cloud-native APIs and Spring Boot 4 / Spring Framework 6 provide native support for **RFC 9457 Problem Details for HTTP APIs** (superseding RFC 7807), improving interoperability with API gateways and client SDKs.

### Decision
1. Replace `ErrorResource` and `ErrorResponseAssembler` with `ProblemDetailFromApplicationErrorAssembler` in `shared.interfaces.rest.transform`.
2. Map application error codes to standardized type URIs under `/api/v1/errors/{error-slug}` (e.g., `/api/v1/errors/not-found`, `/api/v1/errors/validation-error`).
3. Update `GlobalExceptionHandler` to return `ResponseEntity<ProblemDetail>` with localized `detail` strings and structured `invalidParams` arrays for validation errors.
4. Align assembler naming with codebase conventions: `[Target]From[Source]Assembler`.

### Consequences
- **Positive**: Strict adherence to IETF standards, seamless integration with Spring's native `ProblemDetail`, rich localized diagnostic information for clients.
- **Negative**: API consumers must parse standard RFC 9457 fields (`type`, `title`, `status`, `detail`, `code`, `invalidParams`).

---

## ADR-012: Virtual Threads and Concurrency Optimization via Project Loom

### Status
Accepted

### Context
Synchronous I/O operations (blocking database queries and REST processing) on standard platform threads can lead to thread pool starvation under high concurrency. Java 27 and Spring Boot 4 offer production-ready Project Loom Virtual Threads.

### Decision
1. Enable `spring.threads.virtual.enabled=true` in `application.properties` for virtual thread request dispatching in embedded Tomcat.
2. Introduce `VirtualThreadsConfiguration` in `com.acme.center.platform.shared.infrastructure.concurrency.loom.configuration` complying with the packaging convention `infrastructure.<technology-type>.<manufacturer-technology-name>.configuration`.
3. Configure `@EnableAsync` with a virtual-thread-backed `applicationTaskExecutor` bean for asynchronous operations.

### Consequences
- **Positive**: High concurrency throughput and lower memory footprint per request without adopting complex reactive paradigms (WebFlux).
- **Negative**: Database connection pool capacity (HikariCP) must be sized appropriately to avoid connection pool contention.

---

## ADR-013: Modern Sequenced Collections and Stream Gatherers in Domain Model

### Status
Accepted

### Context
Linked item traversal in `LearningPath` (finding last items, checking sequence continuity) previously used linear $O(N)$ filter scans and imperative index loops.

### Decision
1. Refactor `LearningPath` and `ProgressRecord` to use Java 21+ `SequencedCollection` operations (`getFirst()`, `getLast()`, `reversed()`).
2. Utilize Java `Stream Gatherers` (`java.util.stream.Gatherers.windowSliding(2)`) to validate sequence continuity across learning path items.
3. Utilize `Gatherers.scan(...)` to compute cumulative progressive timeline milestones in `ProgressRecord`.

### Consequences
- **Positive**: $O(1)$ access for terminal elements, declarative domain invariant assertions, expressive cumulative calculations without mutable external state.
- **Negative**: Requires modern Java runtime (Java 21+ / Java 26+).
