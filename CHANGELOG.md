# Changelog

All notable changes to the **ACME Learning Center Platform** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) and [Conventional Commits](https://www.conventionalcommits.org/).

---

## [Unreleased]

### Added
- **RESTful IAM Endpoints**:
  - Adopted pure REST resource endpoints `POST /api/v1/users` for user creation (sign-up) and `POST /api/v1/tokens` for security token generation (sign-in).
  - Added `TokensController` and updated `UsersController` with dedicated unit test suites (`TokensControllerTest`, `UsersControllerTest`).
- **Project Loom Virtual Threads Concurrency**:
  - Enabled virtual thread request dispatching in embedded Tomcat via `spring.threads.virtual.enabled=true`.
  - Added `VirtualThreadsConfiguration` under `com.acme.center.platform.shared.infrastructure.concurrency.loom.configuration` providing `@EnableAsync` and a virtual-thread-backed `applicationTaskExecutor` bean.
- **RFC 9457 Problem Details Standard**:
  - Introduced `ProblemDetailFromApplicationErrorAssembler` in `shared.interfaces.rest.transform` with standardized problem type URIs under `/api/v1/errors/{error-slug}`.
  - Modernized `GlobalExceptionHandler` to produce `ResponseEntity<ProblemDetail>` with localized error details and structured `invalidParams` validation error mappings.
- **Aggregate References by Identity**:
  - Introduced `CourseId` value object record in `learning.domain.model.valueobjects` with validation invariants.
  - Added `CourseIdPersistenceConverter` for JPA column mapping in `EnrollmentPersistenceEntity`.
- **Standardized Domain Event Dispatcher**:
  - Introduced `DomainEventPublisher` component in `shared.infrastructure.events` to extract, publish via `ApplicationEventPublisher`, and clear domain events on `AbstractDomainAggregateRoot<T>`.
- **Modern Sequenced Collections & Stream Gatherers**:
  - Added `getPreviousItem(TutorialId)` and `isSequenceConsistent()` with `Gatherers.windowSliding(2)` in `LearningPath`.
  - Added `calculateProgressMilestones()` using `Gatherers.scan(...)` and sequenced accessors (`getLatestProgressRecordItem()`, `getInitialProgressRecordItem()`) in `ProgressRecord`.
- **Documentation & Architecture Standards**:
  - Added Architecture Decision Records in `docs/adrs.md` (ADR-001 through ADR-013).
  - Updated `docs/class-diagram.puml` with PlantUML comment conventions and structural alignment.
  - Added `CONTRIBUTING.md` defining architecture, testing, and conventional commit standards.
  - Updated `docs/user-stories.md` with RFC 9457 error specifications and RESTful IAM endpoints.

### Changed
- **RESTful IAM Transition**:
  - Replaced legacy RPC-style `AuthenticationController` with RESTful `TokensController` and `UsersController`.
  - Updated `WebSecurityConfiguration` filter chain to permit unauthenticated access to `POST /api/v1/users` and `POST /api/v1/tokens`.
- **Runtime & Build Standardization**:
  - Upgraded build definitions and Docker configurations to Java 27 and Maven `3.10.0-eclipse-temurin-27`.
  - Standardized architecture documentation and terminology on Modular Monolith with Layered Domain-Driven Design (Layered DDD) and REST Resources.
- **Domain Aggregate Invariant Encapsulation**:
  - Eliminated Lombok `@Setter` annotations across `Enrollment`, `Student`, `Course`, `User`, `Profile`, `LearningPathItem`, `ProgressRecordItem`, and `Role`.
  - Introduced static `reconstitute(...)` factories on aggregate roots for pure persistence rehydration.
  - Refactored `EnrollmentPersistenceAssembler`, `StudentPersistenceAssembler`, `CoursePersistenceAssembler`, `UserPersistenceAssembler`, and `ProfilePersistenceAssembler`.
- **Decoupled Aggregate References**:
  - Refactored `Enrollment` aggregate to reference `CourseId` instead of embedding the entire `Course` aggregate root.
  - Updated `EnrollmentCommandServiceImpl` to coordinate `LearningPath` loading from `CourseRepository` and pass it to domain methods (`confirm(learningPath)`, `completeTutorial(tutorialId, learningPath)`).
- **Standardized Repository Adapters**:
  - Connected `DomainEventPublisher` across `EnrollmentRepositoryImpl`, `StudentRepositoryImpl`, `CourseRepositoryImpl`, `UserRepositoryImpl`, and `ProfileRepositoryImpl`.
- **Cleaned Up Legacy Error Assemblers**:
  - Removed deprecated `ErrorResource` and `ErrorResponseAssembler` in favor of native Spring `ProblemDetail`.

### Fixed
- Fixed dropped domain events during aggregate persistence by ensuring all repository adapters publish pending events prior to clearing them.
- Fixed typo in `@EventListener` target in `TutorialCompletedEventHandler`.
