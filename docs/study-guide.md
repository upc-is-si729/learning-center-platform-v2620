# ACME Learning Center Platform — Study Guide

> A comprehensive learning resource for Software Engineering students, based on the ACME Learning Center Platform codebase. This guide explains the architectural patterns, design principles, framework features, and language capabilities applied in this RESTful API project.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture: Hexagonal Architecture & DDD](#2-architecture-hexagonal-architecture--ddd)
   - [SOLID Principles](#solid-principles-in-this-architecture)
3. [Domain-Driven Design Patterns](#3-domain-driven-design-patterns)
4. [CQRS: Command Query Responsibility Segregation](#4-cqrs-command-query-responsibility-segregation)
5. [Modern Java Features (Java 21+)](#5-modern-java-features-java-21)
6. [Spring Boot Framework Patterns](#6-spring-boot-framework-patterns)
7. [Spring Security & JWT Authentication](#7-spring-security--jwt-authentication)
8. [REST API Design & Best Practices](#8-rest-api-design--best-practices)
9. [Persistence with Spring Data JPA](#9-persistence-with-spring-data-jpa)
10. [Testing Strategies](#10-testing-strategies)
11. [Cross-Cutting Concerns](#11-cross-cutting-concerns)
12. [Recommended Learning Resources](#12-recommended-learning-resources)
    - [Architecture & Design Fundamentals](#architecture--design-fundamentals)
    - [Domain-Driven Design](#domain-driven-design)
    - [Architectural Styles](#architectural-styles)
    - [CQRS & Event Sourcing](#cqrs--event-sourcing)
    - [SOLID Principles](#solid-principles)
    - [Java Modern Features](#java-modern-features)
    - [Spring Boot](#spring-boot)
    - [REST API Design](#rest-api-design)
    - [Testing](#testing)

---

## 1. Project Overview

### What is this project?

The **ACME Learning Center Platform** is a RESTful API for managing an educational ecosystem. It handles courses, enrollments, students, user profiles, and authentication — organized into bounded contexts following Domain-Driven Design principles.

### Tech Stack

| Category        | Technology                                              |
|-----------------|---------------------------------------------------------|
| Language        | **Java 27** (latest LTS with Project Loom)              |
| Framework       | **Spring Boot 4.1.1**                                   |
| Database        | **MySQL 8+**                                            |
| Persistence     | **Spring Data JPA / Hibernate**                         |
| Security        | **Spring Security + JWT (jjwt) + BCrypt**               |
| API Docs        | **SpringDoc OpenAPI (Swagger UI)**                      |
| Build Tool      | **Maven 3.9+**                                          |
| Containerization| **Docker**                                              |
| Concurrency     | **Project Loom Virtual Threads**                        |

### Bounded Contexts

The application is divided into four modules, each representing a **bounded context**:

| Module       | Responsibility                                                    |
|--------------|-------------------------------------------------------------------|
| `iam/`       | Identity & Access Management: users, roles, authentication, JWT   |
| `learning/`  | Learning domain: courses, enrollments, students, learning paths   |
| `profiles/`  | User profiles: names, addresses, email                            |
| `shared/`    | Shared kernel: base classes, Result type, error handling, i18n    |

---

## 2. Architecture: Hexagonal Architecture & DDD

### What is Hexagonal Architecture?

Also known as **Ports and Adapters** architecture, it keeps the domain logic at the center of the application, isolated from external concerns like databases, web frameworks, and UI.

**Why this matters**: This is the single most important architectural decision in this project. By keeping the domain pure, you can:
- **Test business logic without Spring or a database** — pure unit tests run in milliseconds
- **Swap frameworks freely** — replace Spring with Micronaut, or MySQL with PostgreSQL, without touching domain code
- **Reason about your code** — the domain layer tells you what the business does, not how it's stored or served

**Origin**: Introduced by **Alistair Cockburn** in 2005 ([original article](https://alistair.cockburn.us/hexagonal-architecture-for-the-enterprise/)). Related styles include **Clean Architecture** by **Robert C. Martin** ([blog post](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html)), **Layered Architecture** popularized by **Martin Fowler** ([article](https://martinfowler.com/bliki/LayeredArchitecture.html)), and **Onion Architecture** by **Jeffrey Palermo** ([blog series](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/)). All share one insight: **your business logic should not depend on frameworks**.

### How this project implements it

Each bounded context follows a **4-layer structure**:

```
bounded_context/
├── domain/          # Core business logic (pure Java, no framework dependencies)
├── application/     # Application services orchestrating use cases
├── infrastructure/  # Adapters for external technologies (JPA, Security, etc.)
└── interfaces/      # Entry points (REST controllers, events, ACL facades)
```

### Layer Responsibilities

#### Domain Layer (`domain/`)
- **Aggregates**: `Course`, `Enrollment`, `Student`, `User`, `Profile`
- **Entities**: `Role`, `LearningPathItem`, `ProgressRecordItem`
- **Value Objects**: `CourseId`, `EnrollmentStatus`, `LearningPath`, `TutorialId`
- **Repository Ports**: Pure Java interfaces (e.g., `CourseRepository`)
- **Commands**: `CreateCourseCommand`, `UpdateCourseCommand`
- **Queries**: `GetCourseByIdQuery`, `GetAllCoursesQuery`
- **Domain Events**: `TutorialCompletedEvent`
- **Domain Exceptions**: `CourseNotFoundException`, `EnrollmentNotFoundException`

**Key principle**: The domain layer has **zero dependencies** on Spring, JPA, or any framework. It is pure Java.

#### Application Layer (`application/`)
- **Command Services**: Handle write operations (create, update, delete)
- **Query Services**: Handle read operations (find, list)
- **Event Handlers**: React to domain events
- **Outbound Services (ACL)**: Communicate with other bounded contexts

**Key pattern**: Services return `Result<T, ApplicationError>` instead of throwing exceptions for expected errors.

#### Infrastructure Layer (`infrastructure/`)
- **Repository Adapters**: Implement domain repository ports using Spring Data JPA
- **Persistence Entities**: JPA entities (e.g., `CoursePersistenceEntity`)
- **Assemblers**: Map between domain objects and persistence entities
- **Converters**: Convert value objects to/from database columns
- **Security Configuration**: Spring Security setup
- **Token Services**: JWT implementation

**Key pattern**: The **Anti-Corruption Layer** — assemblers ensure JPA concerns never leak into the domain.

#### Interfaces Layer (`interfaces/`)
- **REST Controllers**: Expose HTTP endpoints
- **Resources**: DTOs for request/response bodies
- **Transformers**: Map between resources and commands/domain objects
- **ACL Facades**: Allow other bounded contexts to interact with this context

### Dependency Direction

```
interfaces → application → domain ← infrastructure
```

Dependencies point **inward** toward the domain. The domain never depends on outer layers.

### SOLID Principles in This Architecture

This architecture embodies the **SOLID principles** defined by **Robert C. Martin**:

| Principle | Author | How this project applies it |
|-----------|--------|-----------------------------|
| **S**ingle Responsibility | Robert C. Martin | Each class has one reason to change: `Course` handles course rules, `CourseRepository` handles persistence |
| **O**pen/Closed | Robert C. Martin | Extend behavior via new command handlers without modifying existing services |
| **L**iskov Substitution | Barbara Liskov | `CourseRepositoryImpl` can replace `CourseRepository` interface transparently |
| **I**nterface Segregation | Robert C. Martin | Separate interfaces for commands (`CourseCommandService`) and queries (`CourseQueryService`) |
| **D**ependency Inversion | Robert C. Martin | High-level services depend on repository interfaces, not concrete implementations |

### Why this matters

- **Testability**: Domain logic can be tested without Spring or a database
- **Maintainability**: Changing the database or web framework doesn't affect domain logic
- **Clarity**: Each layer has a single, well-defined responsibility
- **Framework Independence**: Your business logic isn't locked into Spring
- **Dependency Rule**: As **Robert C. Martin** states: "The whole point of having specifications and abstractions is to reduce coupling between modules"

---

## 3. Domain-Driven Design Patterns

### 3.1 Aggregate Root

An **Aggregate Root** is the entry point for a cluster of related objects (an aggregate) that should be treated as a single unit for data changes.

**Why this matters**: Aggregates are the fundamental unit of consistency in your application. Every business rule that must be enforced atomically lives inside an aggregate. Without aggregates, you risk inconsistent state when multiple parts of your domain change simultaneously.

**Origin**: From **Eric Evans'** "Domain-Driven Design" (2003) ([book](https://www.informit.com/title/0321125215)). Further clarified by **Vaughn Vernon** in "Implementing Domain-Driven Design" (2013) ([book](https://www.oreilly.com/library/view/implementing-domain-driven-design/9780137200038/)).

**Example**: `Course.java`

```java
@Getter
public class Course extends AbstractDomainAggregateRoot<Course> {
    private Long id;
    private String title;
    private String description;
    private LearningPath learningPath;

    public Course(String title, String description) {
        this.title = title != null ? title : Strings.EMPTY;
        this.description = description != null ? description : Strings.EMPTY;
        this.learningPath = new LearningPath();
    }

    public static Course reconstitute(Long id, String title, String description, LearningPath learningPath) {
        var course = new Course(title, description);
        course.id = id;
        course.learningPath = learningPath != null ? learningPath : new LearningPath();
        return course;
    }

    public void addTutorialToLearningPath(TutorialId tutorialId) {
        this.learningPath.addItem(this, tutorialId);
    }

    public void addTutorialToLearningPath(TutorialId tutorialId, TutorialId nextTutorialId) {
        this.learningPath.addItem(this, tutorialId, nextTutorialId);
    }
}
```

**Key concepts**:
- Aggregates encapsulate business rules and invariants
- External objects reference aggregates only by ID (e.g., `CourseId`)
- All modifications go through aggregate methods, not setters
- Aggregates extend `AbstractDomainAggregateRoot` for domain event support

### 3.2 Entity

An **Entity** is an object defined by its identity rather than its attributes. Entities exist within an aggregate and are only accessed through the aggregate root.

**Why this matters**: Entities let you model objects that change over time while maintaining a consistent identity. A `LearningPathItem` might change its position or tutorial, but it remains the same item within the course.

**Origin**: Appears in both DDD (**Eric Evans**) and the **Gang of Four** Design Patterns book (1994) ([book](https://www.oreilly.com/library/view/design-patterns-elements/0201633612/)) in the context of the MVC pattern.

**Example**: `LearningPathItem.java` — has an ID and belongs to a `Course` aggregate.

### 3.3 Value Object

A **Value Object** is immutable and defined by its attributes, not by an identity. Two value objects with the same attributes are considered equal.

**Why this matters**: Value objects eliminate a whole class of bugs because they can't change after creation. They're safe to share across threads, and equality is straightforward: compare attributes, not references. Use them for anything that is defined by what it is, not who it is (e.g., `CourseId`, `Money`, `Address`).

**Origin**: **Eric Evans** introduced Value Objects in DDD. The idea relates to the **Flyweight pattern** from the **Gang of Four** (1994) ([book](https://www.oreilly.com/library/view/design-patterns-elements/0201633612/)).

**Examples**:

```java
// Simple identity value object
public record CourseId(Long courseId) {
    public CourseId {
        if (courseId == null || courseId < 1) {
            throw new IllegalArgumentException("Course id cannot be null or less than 1");
        }
    }
}

// Complex value object with behavior
public class LearningPath {
    private List<LearningPathItem> learningPathItems;

    public TutorialId getFirstTutorialInLearningPath() {
        return isEmpty() ? null : learningPathItems.getFirst().getTutorialId();
    }

    public LearningPathItem getLastItemInLearningPath() {
        return isEmpty() ? null : learningPathItems.getLast();
    }

    public boolean isSequenceConsistent() {
        if (learningPathItems.size() < 2) {
            return true;
        }
        return learningPathItems.stream()
                .gather(Gatherers.windowSliding(2))
                .allMatch(window -> window.getFirst().getNextItem() == window.getLast());
    }
}
```

**Key concepts**:
- Value objects are **immutable** (use `record` for simple ones)
- They can contain **behavior** (not just data)
- They validate their own state in the constructor
- Cross-aggregate references use value objects (`CourseId`, `AcmeStudentRecordId`)

### 3.4 Repository Pattern

A **Repository** provides a collection-like interface for accessing aggregates.

**Why this matters**: The repository pattern is the bridge between your pure domain and the messy reality of databases. By defining the interface in the domain layer, you can swap implementations (JPA, MongoDB, even an in-memory test repo) without changing any business logic. This is the key to testable domain code.

**Origin**: Introduced by **Martin Fowler** in "Patterns of Enterprise Application Architecture" (2002) ([book](https://martinfowler.com/eaaCatalog/)). **Eric Evans** later adopted it as a core DDD pattern.

**Domain Port** (interface in the domain layer):

```java
public interface CourseRepository {
    Optional<Course> findById(Long id);
    List<Course> findAll();
    Course save(Course course);
    void deleteById(Long id);
}
```

**Infrastructure Adapter** (implementation in the infrastructure layer):

```java
@Repository
public class CourseRepositoryImpl implements CourseRepository {
    private final CoursePersistenceRepository coursePersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public Course save(Course course) {
        var saved = coursePersistenceRepository.save(
            CoursePersistenceAssembler.toPersistenceFromDomain(course)
        );
        var savedCourse = CoursePersistenceAssembler.toDomainFromPersistence(saved);
        domainEventPublisher.publishAndClearEvents(course);
        return savedCourse;
    }
}
```

**Key concepts**:
- The domain defines the **port** (interface) — no framework dependencies
- The infrastructure provides the **adapter** (implementation) — uses Spring Data JPA
- The adapter handles domain event publishing after persistence

### 3.5 Domain Events

**Domain Events** represent something significant that happened in the domain.

**Why this matters**: Domain events are how bounded contexts communicate without knowing about each other. When a tutorial is completed, the `Enrollment` aggregate doesn't need to know who cares — it just publishes an event. Other parts of the system (notifications, analytics, progress tracking) react independently. This is the foundation of loose coupling.

**Origin**: **Eric Evans** introduced Domain Events in DDD. The pattern relates to the **Observer pattern** from the **Gang of Four** (1994) ([book](https://www.oreilly.com/library/view/design-patterns-elements/0201633612/)).

**Event Definition**:

```java
@Getter
public class TutorialCompletedEvent extends ApplicationEvent {
    private final Long enrollmentId;
    private final TutorialId tutorialId;

    public TutorialCompletedEvent(Object source, Long enrollmentId, TutorialId tutorialId) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.tutorialId = tutorialId;
    }
}
```

**Base Aggregate Root** (domain event support):

```java
public abstract class AbstractDomainAggregateRoot<T extends AbstractDomainAggregateRoot<T>>
        extends AbstractAggregateRoot<T> {

    protected void registerDomainEvent(Object event) {
        super.registerEvent(event);
    }

    public Collection<Object> domainEvents() {
        return super.domainEvents();
    }

    public void clearDomainEvents() {
        super.clearDomainEvents();
    }
}
```

**Registering an Event** (in the aggregate):

```java
public void completeTutorial(TutorialId tutorialId, LearningPath learningPath) {
    this.progressRecord.completeTutorial(tutorialId, learningPath);
    this.registerDomainEvent(new TutorialCompletedEvent(this, this.getId(), tutorialId));
}
```

**Handling an Event** (in the application layer):

```java
@Service
public class TutorialCompletedEventHandler {
    @EventListener(TutorialCompletedEvent.class)
    public void on(TutorialCompletedEvent event) {
        // React to the event
    }
}
```

**Key concepts**:
- Events are registered in aggregates via `registerDomainEvent()`
- They are published after persistence by `DomainEventPublisher`
- Event handlers react to events in the application layer
- Events enable loose coupling between bounded contexts

### 3.6 Factory Methods

Aggregates use **static factory methods** for reconstruction from persistence.

**Why this matters**: Naming a factory method `reconstitute` instead of using a constructor makes the code self-documenting. When you read `Course.reconstitute(...)`, you immediately know this object is being loaded from storage, not created fresh. This distinction matters because reconstituted objects might need different initialization than new ones.

**Origin**: The **Factory Method pattern** is one of the 23 classic patterns from the **Gang of Four** book (1994) ([book](https://www.oreilly.com/library/view/design-patterns-elements/0201633612/)).

```java
public static Course reconstitute(Long id, String title, String description, LearningPath learningPath) {
    var course = new Course(title, description);
    course.id = id;
    course.learningPath = learningPath != null ? learningPath : new LearningPath();
    return course;
}
```

**Why `reconstitute` instead of a constructor?**
- Separates creation (new objects) from reconstitution (loaded from database)
- Makes the intent clear: this object is being rebuilt from stored state
- Allows the assembler to control the reconstruction process

### 3.7 Specification & Validation in Commands

Commands validate their own data:

```java
public record CreateCourseCommand(String title, String description) {
    public CreateCourseCommand {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description cannot be null or blank");
        }
    }
}
```

**Key concept**: Use **compact constructors** in records for validation logic.

---

## 4. CQRS: Command Query Responsibility Segregation

### What is CQRS?

**CQRS** (Command Query Responsibility Segregation) separates read and write operations into different models:
- **Commands** modify state (create, update, delete)
- **Queries** retrieve data (find, list)

**Why this matters**: Separating reads from writes makes your code's intent obvious. A command service tells you "this changes something" and a query service tells you "this only reads." This separation also enables independent scaling: read operations can be optimized differently from writes, and in complex systems, you can even use different databases for each.

**Origin**: Coined by **Greg Young** in 2009 ([talk](https://www.infoq.com/presentations/CQRS-Event-Processing)), building on **Command-Query Separation** by **Bertrand Meyer** (1988). Popularized by **Udi Dahan** ([blog post](https://udidahan.com/2009/08/31/cqrs-and-event-sourcing/)).

### How this project implements CQRS

Each aggregate has two service interfaces:

```java
// Command Service — handles writes
public interface CourseCommandService {
    Result<Long, ApplicationError> handle(CreateCourseCommand command);
    Result<Course, ApplicationError> handle(UpdateCourseCommand command);
    Result<Long, ApplicationError> handle(DeleteCourseCommand command);
}

// Query Service — handles reads
public interface CourseQueryService {
    Optional<Course> handle(GetCourseByIdQuery query);
    List<Course> handle(GetAllCoursesQuery query);
}
```

### The Result Pattern

Instead of throwing exceptions for expected errors, services return a `Result` type:

```java
public sealed interface Result<T, E> {
    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}

    static <T, E> Result<T, E> success(T value) { return new Success<>(value); }
    static <T, E> Result<T, E> failure(E error) { return new Failure<>(error); }

    default boolean isSuccess() { return this instanceof Success; }
    default boolean isFailure() { return this instanceof Failure; }

    default Optional<T> toOptional() {
        return switch (this) {
            case Success<T, E> s -> Optional.of(s.value);
            case Failure<T, E> f -> Optional.empty();
        };
    }

    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T, E> s -> s.value;
            case Failure<T, E> f -> defaultValue;
        };
    }

    default <T2> Result<T2, E> map(Function<T, T2> f) {
        return switch (this) {
            case Success<T, E> s -> Result.success(f.apply(s.value));
            case Failure<T, E> failure -> Result.failure(failure.error);
        };
    }

    default <T2> Result<T2, E> flatMap(Function<T, Result<T2, E>> f) {
        return switch (this) {
            case Success<T, E> s -> f.apply(s.value);
            case Failure<T, E> failure -> Result.failure(failure.error);
        };
    }

    default <E2> Result<T, E2> mapError(Function<E, E2> f) {
        return switch (this) {
            case Success<T, E> s -> Result.success(s.value);
            case Failure<T, E> failure -> Result.failure(f.apply(failure.error));
        };
    }

    default Result<T, E> recover(Function<E, Result<T, E>> f) {
        return switch (this) {
            case Success<T, E> s -> this;
            case Failure<T, E> failure -> f.apply(failure.error);
        };
    }
}
```

**Usage in a controller**:

```java
var result = courseCommandService.handle(createCourseCommand)
    .flatMap(courseId -> courseQueryService.handle(new GetCourseByIdQuery(courseId))
        .<Result<Course, ApplicationError>>map(Result::success)
        .orElseGet(() -> Result.failure(ApplicationError.notFound("Course", courseId.toString()))));

return ResponseEntityAssembler.toResponseEntityFromResult(
    result,
    CourseResourceFromEntityAssembler::toResourceFromEntity,
    HttpStatus.CREATED
);
```

**Key concepts**:
- `Result` makes error handling **explicit** in the type system
- `map()` transforms the success value
- `flatMap()` chains operations that may fail
- This is inspired by functional programming (Scala's `Either`, Rust's `Result`)

### ApplicationError Factory Methods

```java
public record ApplicationError(String code, String message, String details) {

    public static ApplicationError validationError(String fieldOrConcept, String reason) {
        return new ApplicationError(
                "VALIDATION_ERROR",
                "Validation failed: %s".formatted(fieldOrConcept),
                reason);
    }

    public static ApplicationError notFound(String resourceType, String identifier) {
        return new ApplicationError(
                "%s_NOT_FOUND".formatted(resourceType.toUpperCase()),
                "%s not found: %s".formatted(resourceType, identifier),
                null);
    }

    public static ApplicationError businessRuleViolation(String rule, String reason) {
        return new ApplicationError(
                "BUSINESS_RULE_VIOLATION",
                "Business rule violation: %s".formatted(rule),
                reason);
    }

    public static ApplicationError conflict(String resource, String reason) {
        return new ApplicationError(
                "%s_CONFLICT".formatted(resource.toUpperCase()),
                "Conflict with %s".formatted(resource),
                reason);
    }

    public static ApplicationError unexpected(String context, String reason) {
        return new ApplicationError(
                "UNEXPECTED_ERROR",
                "Unexpected error in %s".formatted(context),
                reason);
    }
}
```

---

## 5. Modern Java Features (Java 21+)

This project uses cutting-edge Java features. Understanding these will make you a stronger Java developer.

**Why this matters**: Java has evolved dramatically in recent years. Features like records, sealed types, and pattern matching reduce boilerplate by 50–80% compared to traditional Java. Virtual threads enable millions of concurrent operations without reactive programming complexity. Using these features makes your code more expressive, safer, and easier to maintain.

**Origin**: Java's evolution is driven by the **OpenJDK community** through JEPs. Key contributors include **Brian Goetz** (records, pattern matching), **Alex Buckley** (Project Loom), and **Rémi Forax** (records, sealed types).

### 5.1 Records (Java 16+)

**Records** are concise classes for carrying immutable data:

```java
public record CreateCourseCommand(String title, String description) {
    public CreateCourseCommand {
        // Compact constructor for validation
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }
    }
}
```

**Why this matters**: Records replace 30+ lines of boilerplate (constructor, getters, `equals`, `hashCode`, `toString`) with one line. They're the go-to choice for DTOs, commands, queries, and value objects — anything that carries data without behavior.

**Origin**: **JEP 395** (Preview, 2020) and **JEP 396** (Java 16, 2021) ([JEP 396](https://openjdk.org/jeps/396)), led by **Rémi Forax** and **Alan Bateman**.

**Benefits**:
- Automatically generates `equals()`, `hashCode()`, `toString()`
- Immutability by default
- Less boilerplate than traditional classes
- Perfect for DTOs, commands, queries, and value objects

**Where used**: Commands, queries, resources (DTOs), `ApplicationError`, `Result.Success`, `Result.Failure`

### 5.2 Sealed Interfaces (Java 17+)

**Sealed interfaces** restrict which classes can implement them:

```java
public sealed interface Result<T, E> {
    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}
}
```

**Why this matters**: Sealed types enable exhaustive pattern matching — the compiler guarantees you handle every possible case. This eliminates entire classes of bugs where a new variant is added but forgotten in switch statements. Combined with records, they're the foundation of algebraic data types in Java.

**Origin**: **JEP 409** (Java 17, 2021) ([JEP 409](https://openjdk.org/jeps/409)), designed by **Alan Bateman** and **Jan Lahoda**.

**Benefits**:
- Exhaustive pattern matching — the compiler knows all possible implementations
- Better documentation — it's clear what variants exist
- Enables safe `switch` expressions on the type

### 5.3 Pattern Matching for Switch (Java 21+)

**Pattern matching** lets you deconstruct objects in switch expressions:

```java
return switch (result) {
    case Result.Success<T, ApplicationError> success ->
        new ResponseEntity<>(successResourceAssembler.apply(success.value()), successStatus);
    case Result.Failure<T, ApplicationError> failure ->
        ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
};
```

**Why this matters**: Pattern matching eliminates the boilerplate of `instanceof` checks followed by casting. When combined with sealed types, the compiler ensures you handle every case, making your code both shorter and safer. This is a game-changer for working with discriminated unions like `Result<T, E>`.

**Origin**: `instanceof` pattern matching in **JEP 394** (Java 16) ([JEP 394](https://openjdk.org/jeps/394)), `switch` pattern matching in **JEP 441** (Java 21) ([JEP 441](https://openjdk.org/jeps/441)), led by **Brian Goetz** and **Jan Lahoda**.

**Benefits**:
- No need for `instanceof` checks and casting
- Exhaustive — compiler ensures all cases are covered (with sealed types)
- Cleaner and more readable than traditional if-else chains

### 5.4 Virtual Threads — Project Loom (Java 21)

**Virtual threads** are lightweight threads managed by the JVM:

```java
@Bean(name = "applicationTaskExecutor")
public TaskExecutor applicationTaskExecutor() {
    var executor = new SimpleAsyncTaskExecutor("loom-exec-");
    executor.setVirtualThreads(true);
    return executor;
}
```

Configuration:
```properties
spring.threads.virtual.enabled=true
```

**Why this matters**: Virtual threads let you write simple blocking code that scales to millions of concurrent operations. Before Loom, high-throughput Java required complex reactive programming (Project Reactor, RxJava). Now you can write straightforward synchronous code that handles thousands of I/O-bound requests per thread. This is the biggest productivity improvement in Java concurrency in decades.

**Origin**: **Project Loom** initiated in **JEP 389** (2019) ([JEP 389](https://openjdk.org/jeps/389)), finalized in **JEP 444** (Java 21) ([JEP 444](https://openjdk.org/jeps/444)), led by **Alex Buckley**.

**Benefits**:
- Millions of threads can run concurrently (vs. thousands with platform threads)
- Simplifies concurrent code — use threads instead of reactive patterns
- Better throughput for I/O-bound applications

### 5.5 Sequenced Collections (Java 21)

**SequencedCollections** provide first/last access to ordered collections:

```java
public TutorialId getFirstTutorialInLearningPath() {
    return isEmpty() ? null : learningPathItems.getFirst().getTutorialId();
}

public LearningPathItem getLastItemInLearningPath() {
    return isEmpty() ? null : learningPathItems.getLast();
}

// Reverse iteration to find previous item
public Optional<LearningPathItem> getPreviousItem(TutorialId tutorialId) {
    LearningPathItem target = null;
    for (LearningPathItem item : learningPathItems.reversed()) {
        if (target != null) {
            return Optional.of(item);
        }
        if (item.getTutorialId().equals(tutorialId)) {
            target = item;
        }
    }
    return Optional.empty();
}
```

**Why this matters**: Before JEP 431, accessing the last element of a list required `list.get(list.size() - 1)`, which is error-prone and inefficient for some collection types. `getLast()`, `getFirst()`, and `reversed()` make ordered collection operations explicit and efficient.

**Origin**: **JEP 431** (Java 21) ([JEP 431](https://openjdk.org/jeps/431)), designed by **Stephen Colebourne**.

**Benefits**:
- Efficient first/last element access
- Built-in reverse iteration
- Works with `ArrayList`, `LinkedList`, and other ordered collections

### 5.6 Stream Gatherers (Java 21)

**Gatherers** transform streams with stateful operations:

```java
public boolean isSequenceConsistent() {
    if (learningPathItems.size() < 2) {
        return true;
    }
    return learningPathItems.stream()
            .gather(Gatherers.windowSliding(2))
            .allMatch(window -> window.getFirst().getNextItem() == window.getLast());
}
```

**Why this matters**: Gatherers fill a gap in Java's Stream API: stateful transformations. Before JEP 448, operations like sliding windows, grouping consecutive elements, or custom iterators required manual loops. Gatherers bring these patterns to the functional style, making code like sequence validation concise and readable.

**Origin**: **JEP 448** (Java 21) ([JEP 448](https://openjdk.org/jeps/448)), designed by **Ivan Gerasimov** and **Markus Krell**.

**Benefits**:
- Sliding window operations on streams
- Stateful stream transformations
- More expressive than manual iteration

### 5.7 Text Blocks & String Templates (Java 21+)

String templates for formatted strings:

```java
return Result.failure(ApplicationError.conflict(
    "Course",
    "Title '%s' already exists".formatted(command.title())
));
```

**Why this matters**: String templates eliminate the error-prone pattern of concatenating strings with `+` or using `String.format()`. The `.formatted()` method makes string interpolation explicit and type-safe, reducing bugs where placeholder counts don't match argument counts. This is especially valuable for error messages, logging, and i18n keys.

**Origin**: String templates previewed in **JEP 430** (Java 21) ([JEP 430](https://openjdk.org/jeps/430)), designed by **Brian Goetz** and **Jan Lahoda**. Text blocks introduced in **JEP 378** (Java 15) ([JEP 378](https://openjdk.org/jeps/378)).

---

## 6. Spring Boot Framework Patterns

### 6.1 Dependency Injection

The project uses **constructor injection** exclusively.

```java
@Service
public class CourseCommandServiceImpl implements CourseCommandService {
    private final CourseRepository courseRepository;

    public CourseCommandServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
}
```

**Why this matters**: Dependency Injection (DI) is the foundation of testable code. By injecting dependencies through constructors, you can swap real services for mocks in tests. Constructor injection makes dependencies explicit — you can see what a class needs just by reading its constructor. This is far superior to field injection, which hides dependencies and makes testing harder.

**Origin**: The term "Dependency Injection" was coined by **Martin Fowler** in 2004 ([article](https://martinfowler.com/articles/injection.html)), building on **Dependency Inversion Principle** by **Robert C. Martin**.

**Why constructor injection?**
- Dependencies are explicit and non-nullable
- Easier to test (no `@Autowired` fields needed)
- Immutability — dependencies can be `final`

### 6.2 Component Scanning & Stereotype Annotations

| Annotation      | Purpose                              |
|-----------------|--------------------------------------|
| `@Service`      | Application service beans            |
| `@Repository`   | Repository adapter beans             |
| `@Configuration`| Configuration classes                |
| `@Bean`         | Method-level bean definition         |
| `@RestController`| REST controller beans               |

**Why this matters**: These annotations tell Spring which classes to manage. Understanding them helps you debug bean creation issues and organize your code. Spring's component scanning automatically finds and wires these beans, reducing configuration boilerplate.

### 6.3 Profile-Based Configuration

```properties
# application.properties (shared)
spring.profiles.default=dev

# application-dev.properties (development overrides)
# application-prod.properties (production overrides)
```

**Why this matters**: Profile-based configuration lets you run the same code against different environments without changing anything. Database URLs, security settings, and feature flags can all vary by profile, activated via `SPRING_PROFILES_ACTIVE`.

### 6.4 Maven Resource Filtering

Inject Maven properties into configuration files:

```properties
documentation.application.description=@project.description@
documentation.application.version=@project.version@
```

### 6.5 SpringDoc OpenAPI

Automatic API documentation with annotations:

```java
@PostMapping
@Operation(summary = "Create a new course", description = "Creates a new course...")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Course created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input data")
})
public ResponseEntity<?> createCourse(@RequestBody CreateCourseResource resource) {
    // ...
}
```

Access at: `http://localhost:8080/swagger-ui/index.html`

**Why this matters**: SpringDoc automatically generates OpenAPI (Swagger) documentation from your code. Clients can explore your API, generate SDKs, and test endpoints without reading code. The `@Operation` and `@ApiResponse` annotations provide the descriptions that appear in the generated docs.

**Reference**: [SpringDoc Official Docs](https://springdoc.org/)

### 6.6 Lombok

Reduces boilerplate with annotations:

| Annotation       | Purpose                          |
|------------------|----------------------------------|
| `@Getter`        | Generates getter methods         |
| `@Setter`        | Generates setter methods         |
| `@NoArgsConstructor` | Generates no-arg constructor |
| `@Slf4j`         | Injects a logger                 |

**Note**: Lombok is configured as an annotation processor in `pom.xml` for better IDE support.

**Reference**: [Lombok Documentation](https://projectlombok.org/)

---

## 7. Spring Security & JWT Authentication

### 7.1 Stateless JWT Authentication

The application uses **JSON Web Tokens (JWT)** for stateless authentication:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrfConfigurer -> csrfConfigurer.disable())
        .sessionManagement(customizer -> customizer
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorizeRequests -> authorizeRequests
            .requestMatchers(HttpMethod.POST, "/api/v1/users", "/api/v1/tokens").permitAll()
            .anyRequest().authenticated());
    http.addFilterBefore(authorizationRequestFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

**Why this matters**: Stateless JWT authentication means your API doesn't store session data on the server. Each request carries its own credentials, making horizontal scaling trivial — any server can validate any token. This is essential for microservices and cloud-native deployments where sessions can't be shared across instances.

**Key concepts**:
- **Stateless**: No server-side session — each request carries a JWT token
- **Custom Filter**: `BearerAuthorizationRequestFilter` validates JWT tokens
- **Public Endpoints**: Sign-up and sign-in are accessible without authentication
- **Protected Endpoints**: All other requests require a valid JWT

**Origin**: JWT standardized as **RFC 7519** (2015) ([RFC 7519](https://www.rfc-editor.org/rfc/rfc7519)). **Spring Security** reference: [Spring Security Docs](https://docs.spring.io/spring-security/reference/).

### 7.2 BCrypt Password Hashing

Passwords are hashed using BCrypt:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return hashingService; // BCryptHashingService
}
```

**Why this matters**: Never store plain-text passwords. BCrypt is computationally expensive by design, making brute-force attacks impractical. Its built-in salt ensures identical passwords produce different hashes, and the adaptive cost factor means you can increase security as hardware improves.

**Why BCrypt?**
- Computationally expensive — resists brute-force attacks
- Built-in salt — each hash is unique even for the same password
- Industry standard for password storage

**Origin**: Designed by **Niels Provos** and **David Mazieres** in 1999, based on **Blowfish** cipher by **Bruce Schneier** (1993).

### 7.3 Method-Level Security

```java
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration { }
```

Enables `@PreAuthorize`, `@PostAuthorize`, and `@Secured` annotations on service methods.

**Why this matters**: Method-level security lets you declare authorization rules directly on the business logic, not in controllers. This keeps security close to the code it protects and ensures the rules apply regardless of how the method is called (REST API, internal service, etc.).

**Origin**: Builds on **Role-Based Access Control (RBAC)** formalized by **David Ferraiolo** and **Dana Kuhn** at NIST (1992).

**Reference**: [Spring Security Method Security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)

---

## 8. REST API Design & Best Practices

### 8.1 Resource-Oriented Design

The API follows REST conventions:

| HTTP Method | Endpoint                              | Action              |
|-------------|---------------------------------------|---------------------|
| `POST`      | `/api/v1/courses`                     | Create a course     |
| `GET`       | `/api/v1/courses`                     | List all courses    |
| `GET`       | `/api/v1/courses/{id}`               | Get course by ID    |
| `PUT`       | `/api/v1/courses/{id}`               | Update a course     |
| `DELETE`    | `/api/v1/courses/{id}`               | Delete a course     |

**Why this matters**: REST conventions make your API predictable. When a developer sees `GET /courses/123`, they know exactly what to expect without reading documentation. Following these conventions reduces the learning curve for API consumers and enables tools like Swagger UI to work automatically.

**Origin**: **REST** defined by **Roy Fielding** in his 2000 PhD dissertation ([dissertation](https://www.ics.uci.edu/~fielding/pubs/dissertation/)). **Richardson Maturity Model** by **Leonard Richardson** ([article](https://restfulapi.net/resource-oriented-design/)).

### 8.2 Versioning

The API is versioned in the URL path: `/api/v1/...`

**Why this matters**: Versioning lets you evolve your API without breaking existing clients. When you need to make incompatible changes, you introduce `v2` while `v1` continues to work. This is essential for public APIs where clients can't update immediately.

### 8.3 RFC 9457 Problem Details

Error responses follow the **RFC 9457** standard:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var applicationError = ApplicationError.validationError("request-body", errorDetails);
        var problemDetail = ProblemDetailFromApplicationErrorAssembler
            .toProblemDetailFromApplicationError(applicationError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
}
```

**Why this matters**: Standardized error responses mean clients can parse errors consistently across all endpoints. RFC 9457 provides `type`, `title`, `status`, `detail`, and `instance` fields that every client can rely on. Without this standard, each endpoint might return errors in a different format, forcing clients to handle dozens of error shapes.

**Benefits**:
- Standardized error format across all endpoints
- Machine-readable error codes
- Human-readable messages
- Support for localized messages (i18n)

**Origin**: **RFC 9457** (originally RFC 7807, 2016) by **Mark Nottingham** and **Mike Kelly** ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html)).

### 8.4 The Transformer Pattern

The project uses a consistent **transformer/assembler** pattern for mapping between layers:

```
Resource (DTO) → Transformer → Command → Service → Domain → Repository → Persistence Entity
```

**Examples**:
- `CreateCourseCommandFromResourceAssembler` — Resource → Command
- `CourseResourceFromEntityAssembler` — Domain → Resource
- `CoursePersistenceAssembler` — Domain ↔ Persistence Entity

**Why this matters**: Without transformers, mapping logic leaks into controllers and services, making them hard to test and maintain. Dedicated assemblers isolate the translation between layers, so each can evolve independently. If you change a database column, only the persistence assembler needs updating.

**Benefits**:
- Clear separation of concerns
- Each transformation is isolated and testable
- No mapping logic in controllers or services

**Origin**: Variation of the **Data Mapper pattern** by **Martin Fowler** ([PoEAA](https://martinfowler.com/eaaCatalog/dataMapperPattern.html)).

### 8.5 ResponseEntityAssembler

A shared utility for converting `Result` types to HTTP responses:

```java
public final class ResponseEntityAssembler {
    private ResponseEntityAssembler() {
    }

    public static <T, R> ResponseEntity<?> toResponseEntityFromResult(
            Result<T, ApplicationError> result,
            Function<T, R> successResourceAssembler,
            HttpStatusCode successStatus
    ) {
        return switch (result) {
            case Result.Success<T, ApplicationError> success ->
                    new ResponseEntity<>(successResourceAssembler.apply(success.value()), successStatus);
            case Result.Failure<T, ApplicationError> failure ->
                    ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
        };
    }
}
```

**Why this matters**: This utility eliminates repetitive boilerplate in every controller method. Instead of writing the same switch statement for handling success and failure in each endpoint, you call one method. This reduces controller methods to their essential logic: receive input, call service, return response.

**Origin**: Combines the **Result pattern** from functional programming with **Response Enricher** concept from **Martin Fowler's** PoEAA.

**Reference**: [REST API Design Rulebook](https://restfulapi.net/)

---

## 9. Persistence with Spring Data JPA

### 9.1 Separation of Domain and Persistence Models

The project maintains **two separate models**:

| Domain Model              | Persistence Model                  |
|---------------------------|------------------------------------|
| `Course` (aggregate)      | `CoursePersistenceEntity` (JPA)    |
| `Enrollment` (aggregate)  | `EnrollmentPersistenceEntity` (JPA)|
| `LearningPath` (VO)       | `LearningPathItemPersistenceEntity`|

**Why this matters**: Keeping domain and persistence models separate is the single best thing you can do to keep your business logic framework-independent. If your `Course` aggregate has `@Entity` annotations, it's coupled to JPA forever. With two models, you can swap databases, add caching layers, or even use event sourcing — all without touching domain code. The assemblers are the only place that knows about both models.

**Origin**: Follows the **Data Mapper pattern** by **Martin Fowler** ([PoEAA](https://martinfowler.com/eaaCatalog/dataMapperPattern.html)). **JPA** led by **Gavin King** ([Hibernate](https://www.hibernate.org/)).

### 9.2 JPA Entity Pattern

```java
@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
public class CoursePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false)
    private String title;

    @Column(nullable =)
    private String description;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LearningPathItemPersistenceEntity> learningPathItems = new ArrayList<>();
}
```

**Why this matters**: JPA entities are plain data carriers with no business logic. All behavior lives in the domain model. This separation ensures persistence concerns (lazy loading, cascading, fetch joins) never leak into business rules.

### 9.3 Auditable Base Entity

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractPersistenceEntity {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Date updatedAt;
}
```

**Why this matters**: Audit trails are required for compliance and debugging. Instead of manually setting timestamps in every entity, Spring Data JPA does it automatically. Every entity that extends this base gets `createdAt` and `updatedAt` for free.

**Benefits**:
- Automatic timestamp management
- Reusable across all entities
- Keeps JPA concerns in the infrastructure layer

**Origin**: Spring Data auditing feature using JPA **entity listeners** from the JPA specification.

### 9.4 Custom Physical Naming Strategy

```properties
spring.jpa.hibernate.naming.physical-strategy=\
  com.acme.center.platform.shared.infrastructure.persistence.jpa.configuration.strategy.\
  SnakeCaseWithPluralizedTablePhysicalNamingStrategy
```

Converts Java class names to snake_case pluralized table names:
- `CoursePersistenceEntity` → `courses`
- `LearningPathItemPersistenceEntity` → `learning_path_items`

**Why this matters**: Database conventions (snake_case, plural tables) differ from Java conventions (camelCase, singular classes). A naming strategy bridges this gap automatically, so you don't need `@Table(name = "...")` on every entity.

### 9.5 Attribute Converters

Convert value objects to/from database columns:

```java
@Converter
public class CourseIdPersistenceConverter implements AttributeConverter<CourseId, Long> {
    @Override
    public Long convertToDatabaseColumn(CourseId attribute) {
        return attribute != null ? attribute.courseId() : null;
    }

    @Override
    public CourseId convertToEntityAttribute(Long dbData) {
        return dbData != null ? new CourseId(dbData) : null;
    }
}
```

**Why this matters**: Databases don't understand Java value objects. Converters bridge the gap automatically, so your persistence entities can use rich types like `CourseId` instead of raw `Long`. This keeps the type safety of value objects while storing simple database columns.

**Origin**: `AttributeConverter` from **JPA 2.1** (2013), addressing the **Object-Relational Impedance Mismatch** described by **Martin Fowler**.

**Reference**: [Spring Data JPA Docs](https://docs.spring.io/spring-data/jpa/reference/)

---

## 10. Testing Strategies

### 10.1 Domain Tests (Pure Unit Tests)

Domain objects are tested without any framework:

```java
class LearningPathTest {
    @Test
    void emptyLearningPathReturnsNullForFirstAndLastItems() {
        var learningPath = new LearningPath();
        assertTrue(learningPath.isEmpty());
        assertNull(learningPath.getFirstTutorialInLearningPath());
    }

    @Test
    void streamGathererWindowSlidingValidatesSequenceConsistency() {
        var course = new Course("Modern Java", "Java 27 Capabilities");
        var learningPath = new LearningPath();
        learningPath.addItem(course, new TutorialId(100L));
        learningPath.addItem(course, new TutorialId(200L));
        assertTrue(learningPath.isSequenceConsistent());
    }
}
```

**Why this matters**: Domain tests are the most valuable tests you can write. They run in milliseconds because they don't need Spring or a database. They verify the core business rules that make your application valuable. If a domain test fails, you know exactly what's broken: a business rule. Aim for comprehensive domain tests first, then add infrastructure tests.

**Key principles**:
- No mocking — test real domain objects
- Fast execution — no Spring context needed
- Focus on business rules and invariants

**Origin**: **Unit testing** popularized by **Kent Beck** and **Erich Gamma** with **JUnit**. **Arrange-Act-Assert** pattern by **Gojko Boga**.

**Reference**: [JUnit 5 Docs](https://junit.org/junit5/docs/current/user-guide/)

### 10.2 Infrastructure Tests (Mocking Tests)

Infrastructure adapters are tested with mocked dependencies:

```java
@ExtendWith(MockitoExtension.class)
class CourseRepositoryImplTest {
    @Mock
    private CoursePersistenceRepository coursePersistenceRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void saveShouldPersistCourseAndPublishDomainEvents() {
        // Arrange
        var course = new Course("Java 27 & DDD", "Course Description");
        when(coursePersistenceRepository.save(any(CoursePersistenceEntity.class)))
            .thenReturn(persistenceEntity);

        // Act
        var saved = courseRepository.save(course);

        // Assert
        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(course.domainEvents()).isEmpty();
    }
}
```

**Why this matters**: Infrastructure tests verify that adapters correctly translate between layers. By mocking dependencies, you test the adapter's logic in isolation — does it call the right methods, transform data correctly, and publish events? Without these tests, bugs in assemblers or converters would only surface at runtime.

**Key principles**:
- Use **Mockito** for mocking dependencies
- Follow **Arrange-Act-Assert** pattern
- Use **AssertJ** for fluent assertions
- Test the adapter logic, not the framework

### 10.3 Testing Tools

| Tool          | Purpose                                    | Reference |
|---------------|--------------------------------------------|-----------|
| JUnit 5       | Test framework                             | [Docs](https://junit.org/junit5/) |
| Mockito       | Mocking dependencies                       | [Docs](https://site.mockito.org/) |
| AssertJ       | Fluent assertion library                   | [Docs](https://assertj.github.io/doc/) |
| Spring Boot Test | Integration testing with Spring context | [Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing) |

**Reference**: [xUnit Test Patterns Catalog](https://xunitpatterns.com/)

---

## 11. Cross-Cutting Concerns

### 11.1 Internationalization (i18n)

The project supports localized messages:

```properties
# messages.properties (English)
validation.field.prefix=Field

# messages_es.properties (Spanish)
validation.field.prefix=Campo
```

Used in exception handlers:

```java
private String resolveMessageOrDefault(String key, String defaultValue) {
    var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, LocaleContextHolder.getLocale());
    return bundle.containsKey(key)
        ? MessageFormat.format(bundle.getString(key))
        : defaultValue;
}
```

**Why this matters**: Internationalization makes your application accessible to users worldwide. By externalizing all user-facing strings into resource bundles, you can add new languages without changing code. Error messages, validation messages, and labels all become translatable. This is essential for any application with a global audience.

**Origin**: Java's `ResourceBundle` dates back to JDK 1.1, following the Unicode standard.

**Reference**: [Java i18n Documentation](https://docs.oracle.com/javase/tutorial/i18n/index.html)

### 11.2 ACL (Access Control List) Facades

Bounded contexts communicate through **facade** interfaces:

```java
// Interface defined in the providing context
public interface ProfilesContextFacade {
    Optional<Profile> getProfileById(UUID profileId);
}

// Implementation in the providing context
public class ProfilesContextFacadeImpl implements ProfilesContextFacade {
    // ...
}
```

**Why this matters**: In a modular architecture, bounded contexts must communicate without leaking internal details. Facades provide a stable, minimal contract that other contexts depend on. If you change the internal implementation of the profiles context, only the facade implementation changes — other contexts are unaffected. This is the key to maintaining independence between modules.

**Benefits**:
- Loose coupling between bounded contexts
- Clear contract for cross-context communication
- Easy to mock in tests

**Origin**: **Facade pattern** from the **Gang of Four** ([book](https://www.oreilly.com/library/view/design-patterns-elements/0201633612/)). **Anti-Corruption Layer** pattern from **Eric Evans'** DDD.

### 11.3 Domain Event Publishing

```java
public class DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishAndClearEvents(AbstractDomainAggregateRoot<?> aggregate) {
        var events = aggregate.domainEvents();
        events.forEach(applicationEventPublisher::publishEvent);
        aggregate.clearDomainEvents();
    }
}
```

**Why this matters**: Domain events enable loose coupling across your entire application. When an aggregate publishes an event, it doesn't know or care who listens. This means you can add new reactions to existing events without modifying the code that created them. For example, adding a notification system requires only a new event handler — no changes to the enrollment or course logic.

**Flow**:
1. Aggregate registers events via `registerDomainEvent()`
2. Repository adapter calls `publishAndClearEvents()` after save
3. Spring publishes events to registered `@EventListener` methods

**Origin**: **Publisher-Subscriber pattern** from the **Gang of Four**'s **Observer pattern**. Spring's `ApplicationEventPublisher` implements this using the **Template Method** approach.

**Reference**: [Spring Events Documentation](https://docs.spring.io/spring-framework/reference/core/beans/context-introduction.html#context-functionality-events)

---

## 12. Recommended Learning Resources

### Architecture & Design Fundamentals

| Resource | Author | Description |
|----------|--------|-------------|
| ["Design Patterns: Elements of Reusable Object-Oriented Software"](https://www.oreilly.com/library/view/design-patterns-elements/0201633612/) | Gang of Four (Gamma, Helm, Johnson, Vlissides) | The foundational book on 23 classic design patterns (Factory, Observer, Strategy, etc.) |
| ["Patterns of Enterprise Application Architecture"](https://martinfowler.com/eaaCatalog/) | Martin Fowler | Catalog of patterns for enterprise apps: Repository, Unit of Work, Service Layer, Specification |
| ["Clean Architecture: A Craftsman's Guide to Software Structure and Design"](https://www.oreilly.com/library/view/clean-architecture/9780134494166/) | Robert C. Martin (Uncle Bob) | Dependency Rule, SOLID principles, and architectural boundaries |
| ["Clean Code: A Handbook of Agile Software Craftsmanship"](https://www.oreilly.com/library/view/clean-code/9780132350884/) | Robert C. Martin (Uncle Bob) | Code-level craftsmanship: naming, functions, classes, error handling |
| ["Refactoring: Improving the Design of Existing Code"](https://martinfowler.com/books/refactoring.html) | Martin Fowler | Systematic techniques for improving code structure without changing behavior |
| ["Head First Design Patterns"](https://www.oreilly.com/library/view/head-first-design/9780596007126/) | Eric Freeman & Elisabeth Robson | Accessible introduction to GoF patterns with visual learning |

### Domain-Driven Design

| Resource | Author | Description |
|----------|--------|-------------|
| ["Domain-Driven Design: Tackling Complexity in the Heart of Software"](https://www.informit.com/title/0321125215) | Eric Evans | The blue book — foundational DDD text introducing aggregates, value objects, bounded contexts |
| ["Implementing Domain-Driven Design"](https://www.oreilly.com/library/view/implementing-domain-driven-design/9780137200038/) | Vaughn Vernon | The red book — practical DDD implementation with Java examples |
| ["Domain-Driven Design Distilled"](https://www.oreilly.com/library/view/domain-driven-design-distilled/9780134434082/) | Vaughn Vernon | Concise introduction to DDD for beginners |
| ["Learning Domain-Driven Design"](https://learning.oreilly.com/library/view/learning-domain-driven-design/9781492085403/) | Vlad Khononov | Modern DDD with tactical patterns and Java examples |
| [ddd.underscore.io](https://ddd.underscore.io/) | — | Online catalog of DDD patterns with examples |

### Architectural Styles

| Resource | Author | Description |
|----------|--------|-------------|
| ["Hexagonal Architecture for the Enterprise"](https://alistair.cockburn.us/hexagonal-architecture-for-the-enterprise/) | Alistair Cockburn | Original article introducing Ports and Adapters architecture |
| ["Clean Architecture" articles](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html) | Robert C. Martin (Uncle Bob) | Series on dependency inversion and architectural boundaries |
| ["Onion Architecture"](https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/) | Jeffrey Palermo | Blog series on domain-centric architecture |
| ["Continuous Integration"](https://martinfowler.com/articles/continuousIntegration.html) | Martin Fowler | Article on CI practices and their role in maintainable architecture |

### CQRS & Event Sourcing

| Resource | Author | Description |
|----------|--------|-------------|
| ["CQRS and Event Sourcing"](https://udidahan.com/2009/08/31/cqrs-and-event-sourcing/) | Udi Dahan | Original blog post popularizing CQRS as a practical pattern |
| [Microsoft CQRS Journey](https://github.com/Microsoft/CQRS) | Microsoft Patterns & Practices | Comprehensive guide to adopting CQRS in enterprise |
| [Greg Young's CQRS presentations](https://www.infoq.com/presentations/CQRS-Event-Processing) | Greg Young | Original CQRS talks and videos (coined the term CQRS) |
| ["Enterprise Integration Patterns"](https://www.oreilly.com/library/view/enterprise-integration-patterns/0596007927/) | Gregor Hohpe & Bobby Woolf | Messaging patterns including Event Driven Architecture |

### SOLID Principles

| Resource | Author | Description |
|----------|--------|-------------|
| ["Agile Software Principles, Patterns, and Practices"](https://www.oreilly.com/library/view/agile-software-principles/9780131976773/) | Robert C. Martin (Uncle Bob) | Comprehensive treatment of SOLID with examples |
| [SOLID Principles articles](https://blog.cleancoder.com/uncle-bob/2015/06/25/AnAgingDeveloperViewsofSOLID.html) | Robert C. Martin (Uncle Bob) | Original blog posts defining each principle |
| ["Working Effectively with Legacy Code"](https://www.oreilly.com/library/view/working-effectively-with/9780321360017/) | Michael Feathers | Practical techniques for applying SOLID to existing code |

### Java Modern Features

| Resource | Author | Description |
|----------|--------|-------------|
| ["Modern Java in Action"](https://www.oreilly.com/library/view/modern-java-in/9781484249641/) | Raoul-Gabriel Urma, Mario Fusco, Alan Mycroft | Java 9–17 features including records, sealed classes, pattern matching |
| [OpenJDK Project Loom](https://openjdk.org/projects/loom/) | — | Virtual threads documentation and JEPs |
| [JEP 431: Sequenced Collections](https://openjdk.org/jeps/431) | — | JDK enhancement proposal |
| [JEP 448: Stream Gatherers](https://openjdk.org/jeps/448) | — | JDK enhancement proposal |
| [JEP 441: Pattern Matching for switch](https://openjdk.org/jeps/441) | — | JDK enhancement proposal |

### Spring Boot

| Resource | Author | Description |
|----------|--------|-------------|
| ["Spring Boot in Action"](https://www.manning.com/books/spring-boot-in-action-third-edition) | Craig Walls | Practical Spring Boot development |
| [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/) | — | Official reference documentation |
| [Spring Security Reference](https://docs.spring.io/spring-security/reference/) | — | Security configuration guide |
| [Baeldung Tutorials](https://www.baeldung.com/) | — | Practical Spring Boot tutorials |

### REST API Design

| Resource | Author | Description |
|----------|--------|-------------|
| [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html) | — | Problem Details for HTTP APIs |
| ["REST API Design Rulebook"](https://restfulapi.net/) | Mark Massink | Practical REST design guide |
| ["Stop Writing Throwaway APIs"](https://www.ibm.com/developerworks/library/ws-stopthrowaway/) | Jim Webber | API design best practices |
| ["Building Microservices"](https://www.oreilly.com/library/view/building-microservices/9781491950357/) | Sam Newman | API design in distributed systems |

### Testing

| Resource | Author | Description |
|----------|--------|-------------|
| ["Agile Java"](https://www.informit.com/store/agile-java-t-hunt-d-goodall-9780132435418) | T. Hunt and D. T. Goodall | Unit testing with Mockito |
| ["xUnit Test Patterns"](https://xunitpatterns.com/) | Steve Freeman & Nat Pryce | Comprehensive catalog of testing patterns |
| ["Working Effectively with Legacy Code"](https://www.oreilly.com/library/view/working-effectively-with/9780321360017/) | Michael Feathers | Testing strategies for untested codebases |
| [AssertJ Documentation](https://assertj.github.io/doc/) | — | Fluent assertions guide |
| [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/) | — | Modern Java testing |

---

## Summary: Key Takeaways

1. **Hexagonal Architecture** (Cockburn, Martin, Fowler) keeps your domain pure and framework-independent
2. **SOLID Principles** (Robert C. Martin) guide every design decision in this codebase
3. **DDD patterns** (Evans, Vernon) — aggregates, value objects, repositories, events — model complex business logic clearly
4. **Design Patterns** (Gang of Four) — Factory, Observer — underpin the tactical DDD implementation
5. **CQRS** (Greg Young, Udi Dahan, Bertrand Meyer) separates reads from writes for clearer code and better scalability
6. **Repository Pattern** (Martin Fowler) isolates persistence from domain logic
7. **Result type** makes error handling explicit and composable
8. **Java 21+ features** (records, sealed types, pattern matching, virtual threads) make code more expressive and efficient
9. **Dependency Injection** (Martin Fowler) makes dependencies explicit and testable
10. **Separation of domain and persistence models** prevents framework leakage into business logic
11. **Testing strategies** vary by layer: pure unit tests for domain, mocking for infrastructure
12. **RFC 9457 Problem Details** standardizes API error responses
13. **i18n support** makes your application accessible to a global audience

---

> **Tip for your own projects**: Start with the domain model. Identify your aggregates, value objects, and business rules first. Then build the application layer, infrastructure, and interfaces around it. This "inside-out" approach ensures your architecture serves your business logic, not the other way around.
