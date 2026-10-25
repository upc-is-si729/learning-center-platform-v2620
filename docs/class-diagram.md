# Class Diagrams — Learning Center Platform

This document describes the structural class architecture of the **ACME Learning Center Platform** across its bounded contexts (`iam`, `learning`, `profiles`, and `shared`), reflecting DDD Tactical Patterns, Domain Aggregate Encapsulation, Identity References, Concurrency with Project Loom, and RFC 9457 REST Error Handling.

```puml
@startuml
skinparam classAttributeIconSize 0
skinparam packageStyle rectangle
left to right direction

' ==========================================
' BOUNDED CONTEXT: IAM
' ==========================================
package "com.acme.center.platform.iam" {
  package "interfaces.rest" {
    class AuthenticationController <<controller>> {
      -userCommandService: UserCommandService
      +signIn(signInResource: SignInResource): ResponseEntity<?>
      +signUp(signUpResource: SignUpResource): ResponseEntity<?>
    }

    class UsersController <<controller>> {
      -userQueryService: UserQueryService
      +getAllUsers(): ResponseEntity<List<UserResource>>
      +getUserById(userId: Long): ResponseEntity<UserResource>
    }

    class RolesController <<controller>> {
      -roleQueryService: RoleQueryService
      +getAllRoles(): ResponseEntity<List<RoleResource>>
    }
  }

  package "interfaces.acl" {
    class IamContextFacade <<facade>> {
      -userCommandService: UserCommandService
      -userQueryService: UserQueryService
      +createUser(username: String, password: String): Long
      +createUser(username: String, password: String, roleNames: List<String>): Long
      +fetchUserIdByUsername(username: String): Long
      +fetchUsernameByUserId(userId: Long): String
    }
  }

  package "application.commandservices" {
    interface UserCommandService <<service>> {
      +handle(command: SignInCommand): Result<ImmutablePair<User,String>, ApplicationError>
      +handle(command: SignUpCommand): Result<User, ApplicationError>
    }

    interface RoleCommandService <<service>> {
      +handle(command: SeedRolesCommand): void
    }
  }

  package "application.queryservices" {
    interface UserQueryService <<service>> {
      +handle(query: GetAllUsersQuery): List<User>
      +handle(query: GetUserByIdQuery): Optional<User>
      +handle(query: GetUserByUsernameQuery): Optional<User>
    }

    interface RoleQueryService <<service>> {
      +handle(query: GetAllRolesQuery): List<Role>
      +handle(query: GetRoleByNameQuery): Optional<Role>
    }
  }

  package "domain.model.aggregates" {
    class User <<aggregate root>> {
      -id: Long
      -username: String
      -password: String
      -roles: Set<Role>
      +User(username: String, password: String)
      +User(username: String, password: String, roles: List<Role>)
      +reconstitute(id: Long, username: String, password: String, roles: Set<Role>): User
      +addRole(role: Role): User
      +addRoles(roles: List<Role>): User
      +getId(): Long
      +getUsername(): String
      +getPassword(): String
      +getRoles(): Set<Role>
    }
  }

  package "domain.model.entities" {
    class Role <<entity>> {
      -id: Long
      -name: Roles
      +Role(name: Roles)
      +reconstitute(id: Long, name: Roles): Role
      +getId(): Long
      +getName(): Roles
      +getStringName(): String
      +getDefaultRole(): Role
      +toRoleFromName(name: String): Role
      +validateRoleSet(roles: List<Role>): List<Role>
    }
  }

  package "domain.repositories" {
    interface UserRepository <<repository port>> {
      +save(user: User): User
      +findById(id: Long): Optional<User>
      +existsByUsername(username: String): boolean
      +findByUsername(username: String): Optional<User>
      +findAll(): List<User>
    }

    interface RoleRepository <<repository port>> {
      +save(role: Role): Role
      +findByName(name: Roles): Optional<Role>
      +existsByName(name: Roles): boolean
      +findAll(): List<Role>
    }
  }
}

' ==========================================
' BOUNDED CONTEXT: PROFILES
' ==========================================
package "com.acme.center.platform.profiles" {
  package "interfaces.rest" {
    class ProfilesController <<controller>> {
      -profileCommandService: ProfileCommandService
      -profileQueryService: ProfileQueryService
      +createProfile(resource: CreateProfileResource): ResponseEntity<?>
      +getProfileById(profileId: Long): ResponseEntity<ProfileResource>
      +getAllProfiles(): ResponseEntity<List<ProfileResource>>
    }
  }

  package "interfaces.acl" {
    class ProfilesContextFacade <<facade>> {
      -profileCommandService: ProfileCommandService
      -profileQueryService: ProfileQueryService
      +createProfile(firstName: String, lastName: String, email: String, street: String, number: String, city: String, postalCode: String, country: String): Long
      +fetchProfileIdByEmail(email: String): Long
    }
  }

  package "domain.model.aggregates" {
    class Profile <<aggregate root>> {
      -id: Long
      -name: PersonName
      -email: EmailAddress
      -address: StreetAddress
      +Profile(command: CreateProfileCommand)
      +Profile(firstName: String, lastName: String, email: String, street: String, number: String, city: String, postalCode: String, country: String)
      +reconstitute(id: Long, name: PersonName, email: EmailAddress, address: StreetAddress): Profile
      +onCreated(): void
      +updateName(firstName: String, lastName: String): void
      +updateEmail(email: String): void
      +updateAddress(street: String, number: String, city: String, postalCode: String, country: String): void
      +getId(): Long
      +getFullName(): String
      +getEmailAddress(): String
      +getStreetAddress(): String
    }
  }

  package "domain.model.valueobjects" {
    class PersonName <<value object>> {
      -firstName: String
      -lastName: String
      +fullName(): String
    }
    class EmailAddress <<value object>> {
      -address: String
    }
    class StreetAddress <<value object>> {
      -street: String
      -number: String
      -city: String
      -postalCode: String
      -country: String
      +streetAddress(): String
    }
  }

  package "domain.repositories" {
    interface ProfileRepository <<repository port>> {
      +save(profile: Profile): Profile
      +findById(id: Long): Optional<Profile>
      +findByEmail(email: EmailAddress): Optional<Profile>
      +existsByEmail(email: EmailAddress): boolean
      +findAll(): List<Profile>
    }
  }
}

' ==========================================
' BOUNDED CONTEXT: LEARNING
' ==========================================
package "com.acme.center.platform.learning" {
  package "domain.model.aggregates" {
    class Course <<aggregate root>> {
      -id: Long
      -title: String
      -description: String
      -learningPath: LearningPath
      +Course(title: String, description: String)
      +Course(command: CreateCourseCommand)
      +reconstitute(id: Long, title: String, description: String, learningPath: LearningPath): Course
      +updateInformation(title: String, description: String): Course
      +addTutorialToLearningPath(tutorialId: TutorialId): void
      +addTutorialToLearningPath(tutorialId: TutorialId, nextTutorialId: TutorialId): void
      +getId(): Long
      +getTitle(): String
      +getDescription(): String
      +getLearningPath(): LearningPath
    }

    class Enrollment <<aggregate root>> {
      -id: Long
      -acmeStudentRecordId: AcmeStudentRecordId
      -courseId: CourseId
      -progressRecord: ProgressRecord
      -status: EnrollmentStatus
      +Enrollment(studentRecordId: AcmeStudentRecordId, courseId: CourseId)
      +reconstitute(id: Long, studentRecordId: AcmeStudentRecordId, courseId: CourseId, status: EnrollmentStatus, progressRecord: ProgressRecord): Enrollment
      +confirm(learningPath: LearningPath): void
      +reject(): void
      +cancel(): void
      +completeTutorial(tutorialId: TutorialId, learningPath: LearningPath): void
      +isConfirmed(): boolean
      +isRejected(): boolean
      +isCancelled(): boolean
      +calculateDaysElapsed(): long
      +getId(): Long
      +getAcmeStudentRecordId(): AcmeStudentRecordId
      +getCourseId(): CourseId
      +getStatus(): EnrollmentStatus
      +getProgressRecord(): ProgressRecord
    }

    class Student <<aggregate root>> {
      -id: Long
      -acmeStudentRecordId: AcmeStudentRecordId
      -profileId: ProfileId
      -performanceMetricSet: StudentPerformanceMetricSet
      +Student(profileId: ProfileId)
      +Student(profileId: Long)
      +reconstitute(id: Long, studentRecordId: AcmeStudentRecordId, profileId: ProfileId, metricSet: StudentPerformanceMetricSet): Student
      +updateMetricsOnCourseCompleted(): void
      +updateMetricsOnTutorialCompleted(): void
      +getId(): Long
      +getAcmeStudentRecordId(): AcmeStudentRecordId
      +getProfileId(): ProfileId
      +getPerformanceMetricSet(): StudentPerformanceMetricSet
    }
  }

  package "domain.model.entities" {
    class LearningPathItem <<entity>> {
      -id: Long
      -course: Course
      -tutorialId: TutorialId
      -nextItem: LearningPathItem
      +reconstitute(id: Long, course: Course, tutorialId: TutorialId, nextItem: LearningPathItem): LearningPathItem
      +updateNextItem(nextItem: LearningPathItem): void
    }

    class ProgressRecordItem <<entity>> {
      -id: Long
      -enrollment: Enrollment
      -tutorialId: TutorialId
      -status: ProgressStatus
      -startedAt: Date
      -completedAt: Date
      +reconstitute(id: Long, enrollment: Enrollment, tutorialId: TutorialId, status: ProgressStatus, startedAt: Date, completedAt: Date): ProgressRecordItem
      +start(): void
      +complete(): void
      +calculateDaysElapsed(): long
    }
  }

  package "domain.model.valueobjects" {
    class CourseId <<value object>> {
      -courseId: Long
    }
    class TutorialId <<value object>> {
      -tutorialId: Long
    }
    class AcmeStudentRecordId <<value object>> {
      -studentRecordId: String
    }
    class ProfileId <<value object>> {
      -profileId: Long
    }
    class StudentPerformanceMetricSet <<value object>> {
      -totalCompletedCourses: Integer
      -totalTutorials: Integer
      +incrementTotalCompletedCourses(): StudentPerformanceMetricSet
      +incrementTotalTutorials(): StudentPerformanceMetricSet
    }
    class LearningPath <<value object>> {
      -learningPathItems: List<LearningPathItem>
      +addItem(course: Course, tutorialId: TutorialId): void
      +addItem(course: Course, tutorialId: TutorialId, nextTutorialId: TutorialId): void
      +getLastItemInLearningPath(): LearningPathItem
      +getFirstTutorialInLearningPath(): TutorialId
      +getPreviousItem(tutorialId: TutorialId): Optional<LearningPathItem>
      +isSequenceConsistent(): boolean
    }
    class ProgressRecord <<value object>> {
      -progressRecordItems: List<ProgressRecordItem>
      +getLatestProgressRecordItem(): ProgressRecordItem
      +getInitialProgressRecordItem(): ProgressRecordItem
      +calculateProgressMilestones(): List<ProgressMilestone>
      +calculateDaysElapsedForEnrollment(enrollment: Enrollment): long
    }
    enum EnrollmentStatus {
      REQUESTED
      CONFIRMED
      REJECTED
      CANCELLED
    }
    enum ProgressStatus {
      NOT_STARTED
      STARTED
      COMPLETED
    }
  }

  package "domain.repositories" {
    interface CourseRepository <<repository port>> {
      +save(course: Course): Course
      +findById(id: Long): Optional<Course>
      +existsByTitle(title: String): boolean
      +findAll(): List<Course>
    }

    interface EnrollmentRepository <<repository port>> {
      +save(enrollment: Enrollment): Enrollment
      +findById(id: Long): Optional<Enrollment>
      +findByAcmeStudentRecordIdAndCourseId(studentRecordId: AcmeStudentRecordId, courseId: CourseId): Optional<Enrollment>
      +findAllByCourseId(courseId: CourseId): List<Enrollment>
      +findAllByAcmeStudentRecordId(studentRecordId: AcmeStudentRecordId): List<Enrollment>
      +findAll(): List<Enrollment>
    }

    interface StudentRepository <<repository port>> {
      +save(student: Student): Student
      +findById(id: Long): Optional<Student>
      +findByAcmeStudentRecordId(studentRecordId: AcmeStudentRecordId): Optional<Student>
      +findByProfileId(profileId: ProfileId): Optional<Student>
      +existsByProfileId(profileId: ProfileId): boolean
      +findAll(): List<Student>
    }
  }
}

' ==========================================
' BOUNDED CONTEXT: SHARED KERNEL
' ==========================================
package "com.acme.center.platform.shared" {
  package "domain.model.aggregates" {
    abstract class AbstractDomainAggregateRoot<T> {
      -domainEvents: List<Object>
      #registerDomainEvent(event: Object): void
      +domainEvents(): List<Object>
      +clearDomainEvents(): void
    }
  }

  package "application.result" {
    interface Result<T, E> <<sealed>>
    class Success<T, E> implements Result
    class Failure<T, E> implements Result
    record ApplicationError(code: String, message: String, details: String)
  }

  package "infrastructure.events" {
    class DomainEventPublisher <<component>> {
      -applicationEventPublisher: ApplicationEventPublisher
      +publishAndClearEvents(aggregate: AbstractDomainAggregateRoot<?>): void
    }
  }

  package "infrastructure.concurrency.loom.configuration" {
    class VirtualThreadsConfiguration <<configuration>> {
      +applicationTaskExecutor(): TaskExecutor
    }
  }

  package "interfaces.rest.transform" {
    class ProblemDetailFromApplicationErrorAssembler <<assembler>> {
      +toProblemDetailFromApplicationError(error: ApplicationError): ProblemDetail
      +toResponseFromApplicationError(error: ApplicationError): ResponseEntity<ProblemDetail>
      +toStatusFromErrorCode(errorCode: String): HttpStatusCode
    }
    class ResponseEntityAssembler <<assembler>> {
      +toResponseEntityFromResult(result: Result, assembler: Function, status: HttpStatusCode): ResponseEntity<?>
    }
  }
}

' ==========================================
' RELATIONSHIPS & INHERITANCE
' ==========================================
AbstractDomainAggregateRoot <|-- User
AbstractDomainAggregateRoot <|-- Profile
AbstractDomainAggregateRoot <|-- Course
AbstractDomainAggregateRoot <|-- Enrollment
AbstractDomainAggregateRoot <|-- Student

Enrollment *-- CourseId : references by identity
Enrollment *-- AcmeStudentRecordId : references by identity
Enrollment *-- ProgressRecord
Student *-- ProfileId : references by identity
Student *-- AcmeStudentRecordId
Student *-- StudentPerformanceMetricSet
Course *-- LearningPath
LearningPath *-- LearningPathItem
ProgressRecord *-- ProgressRecordItem
User *-- Role

@enduml
```

## Architectural Highlights

1. **Pure Domain Aggregates & Invariant Encapsulation**: Aggregates (`Course`, `Enrollment`, `Student`, `User`, `Profile`) eliminate public `@Setter`s and rehydrate state via `reconstitute(...)` factories.
2. **References by Identity**: `Enrollment` references `Course` via `CourseId` and `Student` via `AcmeStudentRecordId`, preserving strict aggregate isolation boundaries.
3. **Event Dispatching**: All repository adapters leverage `DomainEventPublisher` to publish domain events accumulated on `AbstractDomainAggregateRoot`.
4. **RFC 9457 Problem Details**: `ProblemDetailFromApplicationErrorAssembler` standardizes all error responses with URI types under `/api/v1/errors/...`.
5. **Project Loom Concurrency**: `VirtualThreadsConfiguration` enables lightweight virtual threads for asynchronous task execution and web request handling.
6. **Sequenced Collections & Stream Gatherers**: `LearningPath` and `ProgressRecord` utilize `SequencedCollection` and `java.util.stream.Gatherers` for sequence integrity and timeline scans.
