# Architecture Decision Records (ADRs) — ACME Learning Center Platform

This document maintains Architecture Decision Records (ADRs) for the **ACME Learning Center Platform** following the [Michael Nygard format](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

---

## Index

- [ADR-001: Modular Monolith with Hexagonal Architecture and Domain-Driven Design](#adr-001-modular-monolith-with-hexagonal-architecture-and-domain-driven-design)
- [ADR-002: Aggregate Invariant Encapsulation and Reconstitution Factory Pattern](#adr-002-aggregate-invariant-encapsulation-and-reconstitution-factory-pattern)
- [ADR-003: Standardized Domain Event Dispatching via DomainEventPublisher](#adr-003-standardized-domain-event-dispatching-via-domaineventpublisher)
- [ADR-004: Aggregate References by Identity (CourseId)](#adr-004-aggregate-references-by-identity-courseid)
- [ADR-005: Adoption of RFC 9457 Problem Details for REST Error Responses](#adr-005-adoption-of-rfc-9457-problem-details-for-rest-error-responses)
- [ADR-006: Virtual Threads and Concurrency Optimization via Project Loom](#adr-006-virtual-threads-and-concurrency-optimization-via-project-loom)
- [ADR-007: Modern Sequenced Collections and Stream Gatherers in Domain Model](#adr-007-modern-sequenced-collections-and-stream-gatherers-in-domain-model)

---

## ADR-001: Modular Monolith with Hexagonal Architecture and Domain-Driven Design

### Status
Accepted

### Context
The platform requires clear boundaries between distinct business domains (IAM, Student Profiles, Learning Lifecycle) while maintaining development velocity, transactional simplicity, and operational manageability without the distributed systems overhead of microservices.

### Decision
We structure the system as a **Modular Monolith** applying **Domain-Driven Design (DDD)** and **Hexagonal Architecture (Ports and Adapters)**:
1. **Bounded Contexts**: `iam`, `profiles`, `learning`, and `shared`.
2. **Layer Separation**: Each context defines `domain`, `application`, `infrastructure`, and `interfaces`.
3. **Decoupled Persistence**: Domain aggregates are pure Java classes decoupled from JPA `@Entity` annotations. Persistence entities (`*PersistenceEntity`) reside solely in `infrastructure`.
4. **Anti-Corruption Layer (ACL)**: Cross-context communication is governed through explicit facades (`IamContextFacade`, `ProfilesContextFacade`) and domain integration events.

### Consequences
- **Positive**: Strict domain boundaries, high testability of business logic without database dependencies, clean evolution path toward independent services if needed.
- **Negative**: Requires mapping layers (`*PersistenceAssembler`, `*ResourceAssembler`) between domain models, persistence entities, and REST DTOs.

---

## ADR-002: Aggregate Invariant Encapsulation and Reconstitution Factory Pattern

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

## ADR-003: Standardized Domain Event Dispatching via DomainEventPublisher

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

## ADR-004: Aggregate References by Identity (CourseId)

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

## ADR-005: Adoption of RFC 9457 Problem Details for REST Error Responses

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

## ADR-006: Virtual Threads and Concurrency Optimization via Project Loom

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

## ADR-007: Modern Sequenced Collections and Stream Gatherers in Domain Model

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
