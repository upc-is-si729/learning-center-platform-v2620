# Learning Center Platform — REST API Technical Stories

## Overview
This document contains API-focused technical stories intended for frontend or mobile developers integrating with the Learning Center Platform REST API (Java, Spring Boot).

Common conventions
- Base path: `/api/v1`
- All request and response bodies use `Content-Type: application/json`
- All protected endpoints require a JWT Bearer token in the `Authorization` header (see TS-ARCH001)
- Error responses follow a standard schema (see TS-ARCH002)

---

### TS-ARCH001 — Authenticate API Requests with a JWT Bearer Token
As a frontend developer, I want to include the JWT token in my API requests so that I can access protected endpoints after signing in.

Note: Only `POST /api/v1/tokens` (sign-in) and `POST /api/v1/users` (sign-up) are publicly accessible without a token. All other endpoints require authentication.

Acceptance criteria:
- Scenario: Authenticated request succeeds
  - Given the client received a token via `POST /api/v1/tokens`
  - When the client sends any request to a protected endpoint with the header `Authorization: Bearer <token>`
  - Then the API processes the request normally and responds with the expected resource.
- Scenario: Missing token
  - Given the client sends a request to a protected endpoint without an `Authorization` header
  - When the API detects no token
  - Then the API responds `401 Unauthorized` with no further body.
- Scenario: Expired or invalid token
  - Given the client sends a request with a malformed or expired `Authorization: Bearer <token>` header
  - When the API fails to validate the token
  - Then the API responds `401 Unauthorized` with no further body.

---

### TS-ARCH002 — Handle Standardized API Error Responses (RFC 9457 Problem Details)
As a frontend developer, I want all API error responses to follow the RFC 9457 Problem Details standard so that I can implement a single, reusable error-handling layer in my application.

Acceptance criteria:
- Scenario: Any error response
  - Given any API request results in an error (4xx or 5xx)
  - Then the API returns a JSON body with `Content-Type: application/problem+json` (or `application/json`) containing:
    - `type` (URI, required) — problem type identifier URI formatted as `/api/v1/errors/{error-slug}` (e.g., `/api/v1/errors/not-found`, `/api/v1/errors/validation-error`)
    - `title` (String, required) — short, human-readable summary of the problem type
    - `status` (Integer, required) — HTTP status code
    - `detail` (String, required) — human-readable explanation specific to this occurrence (localized per `Accept-Language`)
    - `code` (String, required) — machine-readable domain error code (e.g., `VALIDATION_ERROR`, `COURSE_NOT_FOUND`)
    - `details` (String, optional) — additional context or original error details
    - `invalidParams` (Array of Objects, optional) — list of validation errors for `400 Bad Request` with `field` and `message` properties

Error code to HTTP status and problem type URI mapping:

| `code` value                             | HTTP Status                 | Problem `type` URI                     | Title                       |
|------------------------------------------|-----------------------------|----------------------------------------|-----------------------------|
| `VALIDATION_ERROR`                       | `400 Bad Request`           | `/api/v1/errors/validation-error`      | Validation Error            |
| `*_NOT_FOUND` (e.g., `COURSE_NOT_FOUND`) | `404 Not Found`             | `/api/v1/errors/not-found`             | Resource Not Found          |
| `*_CONFLICT` (e.g., `USER_CONFLICT`)     | `409 Conflict`              | `/api/v1/errors/conflict`              | Resource Conflict           |
| `BUSINESS_RULE_VIOLATION`                | `422 Unprocessable Entity`  | `/api/v1/errors/business-rule-violation` | Business Rule Violation   |
| `UNEXPECTED_ERROR`                       | `500 Internal Server Error` | `/api/v1/errors/unexpected-error`      | Internal Server Error       |

Example error response body:
```json
{
  "type": "/api/v1/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Request validation failed",
  "code": "VALIDATION_ERROR",
  "invalidParams": [
    {
      "field": "firstName",
      "message": "is required"
    }
  ]
}
```

---

### TS-ARCH003 — Request Localized Error Messages
As a frontend developer, I want to receive error messages in my users' language so that I can display them directly without additional translation.

Supported locales: `en` (English, default), `es` (Spanish)

Acceptance criteria:
- Scenario: Default locale (English)
  - Given the client sends a request without an `Accept-Language` header (or with `Accept-Language: en`)
  - When the API returns an error response
  - Then the `detail` field in the problem detail is in English (e.g., `"Resource not found"`)
- Scenario: Spanish locale
  - Given the client sends a request with the header `Accept-Language: es`
  - When the API returns an error response
  - Then the `detail` field in the problem detail is in Spanish (e.g., `"Recurso no encontrado"`)
- Scenario: Unsupported locale falls back to English
  - Given the client sends a request with an unsupported `Accept-Language` value (e.g., `fr`)
  - When the API returns an error response
  - Then the `detail` field is returned in English.

Note: Only the `detail` field in error responses is localized. All resource data fields (course titles, names, etc.) are stored and returned as provided by the client.

---

### TS-C001 — Create a Course
As a frontend developer, I want to create a new course through the API so that I can add courses to the system as a feature in my application.

Acceptance criteria:
- Scenario: Successful create
  - Given a POST request to `/api/v1/courses` is received with a request body containing the create-course attributes: title, description
  - When the API validates and creates the course
  - Then the API responds with `201 Created` and returns the created course with attributes: id (Long), title (String), description (String).
- Scenario: Validation error
  - Given a POST request to `/api/v1/courses` is received with missing or invalid create-course attributes (e.g., empty title or description)
  - When the API validates the request and detects validation errors
  - Then the API responds with `400 Bad Request` and returns an error payload describing validation errors.

---

### TS-C002 — Get a Course by id
As a frontend developer, I want to fetch a course by its `{courseId}` through the API, so that I can show course information as a feature in my application.

Acceptance criteria:
- Scenario: Found
  - Given a GET request to `/api/v1/courses/{courseId}` is received
  - When the API finds the course
  - Then the API responds `200 OK` and returns the course with attributes: id (Long), title (String), description (String).
- Scenario: Not found
  - Given a GET request to `/api/v1/courses/{courseId}` is received for a non-existent `{courseId}`
  - When the API does not find the course
  - Then the API responds `404 Not Found` and returns an error payload.

---

### TS-C003 — Get all Courses
As a frontend developer, I want to list all courses through the API so that I can show them in a catalogue as a feature in my application.

Acceptance criteria:
- Scenario: Courses exist
  - Given a GET request to `/api/v1/courses` is received
  - When the API finds one or more courses
  - Then the API responds `200 OK` and returns a list where each item contains the course attributes: id, title, description.
- Scenario: No courses found
  - Given a GET request to `/api/v1/courses` is received and there are no courses in the system
  - When the API searches for courses and finds none
  - Then the API responds `404 Not Found` and returns an error payload.

---

### TS-LP001 — Add a Tutorial to a Course Learning Path
As a frontend developer, I want to add a tutorial to a course's learning path through the API so that instructors can curate course content as a feature in my application.

Acceptance criteria:
- Scenario: Successful add
  - Given a POST request to `/api/v1/courses/{courseId}/learning-path-items/{tutorialId}` is received
  - When the API successfully attaches the tutorial
  - Then the API responds `201 Created` and returns the learning-path item with attributes: learningPathItemId (Long), courseId (Long), tutorialId (Long).
- Scenario: Course or tutorial not found
  - Given a POST request to `/api/v1/courses/{courseId}/learning-path-items/{tutorialId}` is received where the referenced course or tutorial does not exist
  - When the API cannot find the course or tutorial
  - Then the API responds `404 Not Found` and returns an error payload.

---

### TS-S001 — Create a Student
As a frontend developer, I want to create a student record through the API so that users can enroll in courses as a feature in my application.

Acceptance criteria:
- Scenario: Successful create
  - Given a POST request to `/api/v1/students` is received with a request body containing the create-student attributes: firstName, lastName, email, street, number, city, postalCode, country
  - When the API validates and creates the student
  - Then the API responds `201 Created` and returns the created student with attributes: acmeStudentRecordId (String), profileId (Long), totalCompletedCourses (Integer), totalCompletedTutorials (Integer).
- Scenario: Validation error
  - Given a POST request to `/api/v1/students` is received with missing or invalid student attributes (e.g., blank firstName or invalid email)
  - When the API validates the request and detects validation errors
  - Then the API responds `400 Bad Request` and returns an error payload describing validation errors.

---

### TS-S002 — Get Student by Acme Student Record ID
As a frontend developer, I want to fetch a student by `{studentRecordId}` through the API so that I can show student profile pages as a feature in my application.

Note: `{studentRecordId}` is a UUID string generated by the platform.

Acceptance criteria:
- Scenario: Found
  - Given a GET request to `/api/v1/students/{studentRecordId}` is received
  - When the API finds the student
  - Then the API responds `200 OK` and returns the student with attributes: acmeStudentRecordId (String), profileId (Long), totalCompletedCourses (Integer), totalCompletedTutorials (Integer).
- Scenario: Not found
  - Given a GET request to `/api/v1/students/{studentRecordId}` is received for a non-existent `{studentRecordId}`
  - When the API does not find the student
  - Then the API responds `404 Not Found` and returns an error payload.

---

### TS-E001 — Request Enrollment
As a frontend developer, I want to request a new enrollment through the API so that students can request access to a course as a feature in my application.

Acceptance criteria:
- Scenario: Successful request
  - Given a POST request is received to `/api/v1/enrollments` with a request body containing the request-enrollment attributes: studentRecordId (UUID string), courseId
  - When the API validates and creates the enrollment
  - Then the API responds `201 Created` and returns the enrollment with attributes: enrollmentId (Long), studentRecordId (UUID String), courseId (Long), status (String).
- Scenario: Bad request
  - Given a POST request is received to `/api/v1/enrollments` with missing or invalid enrollment attributes
  - When the API validates the request and detects validation errors
  - Then the API responds `400 Bad Request` and returns an error payload describing validation errors.
- Scenario: Creation failure
  - Given a POST request is received to `/api/v1/enrollments` and the enrollment cannot be retrieved after creation
  - When the API attempts to return the newly created enrollment and does not find it
  - Then the API responds `404 Not Found` and returns an error payload.

---

### TS-E002 — Confirm / Reject / Cancel Enrollment
As a frontend developer, I want to confirm, reject or cancel an enrollment through the API so that I can support the enrollment lifecycle as a feature in my application.

Acceptance criteria:
- Confirm
  - Given a POST request to `/api/v1/enrollments/{enrollmentId}/confirmations` is received
  - When the API confirms the enrollment successfully
  - Then the API responds `200 OK` and returns a message with attribute: message (String).
  - Given POST `/api/v1/enrollments/{enrollmentId}/confirmations` is received with invalid data or for a non-existent enrollment
  - When the API validates the request and cannot perform the confirmation
  - Then the API responds `400 Bad Request` and returns an error payload.
- Reject
  - Given a POST request to `/api/v1/enrollments/{enrollmentId}/rejections` is received
  - When the API rejects the enrollment
  - Then the API responds `200 OK` and returns a message with attribute: message (String).
  - Given POST `/api/v1/enrollments/{enrollmentId}/rejections` is received with invalid data or for a non-existent enrollment
  - When the API validates the request and cannot perform the rejection
  - Then the API responds `400 Bad Request` and returns an error payload.
- Cancel
  - Given a POST request to `/api/v1/enrollments/{enrollmentId}/cancellations` is received
  - When the API cancels the enrollment
  - Then the API responds `200 OK` and returns a message with attribute: message (String).
  - Given POST `/api/v1/enrollments/{enrollmentId}/cancellations` is received with invalid data or for a non-existent enrollment
  - When the API validates the request and cannot perform the cancellation
  - Then the API responds `400 Bad Request` and returns an error payload.

---

### TS-P001 — Create a Profile
As a frontend developer, I want to create user profiles through the API so that learners have personal profiles as a feature in my application.

Acceptance criteria:
- Scenario: Successful create
  - Given POST is received to `/api/v1/profiles` with a request body containing the create-profile attributes: firstName, lastName, email, street, number, city, postalCode, country
  - When the API validates and creates the profile
  - Then the API responds `201 Created` and returns the created profile with attributes: id (Long), fullName (String), email (String), streetAddress (String).
- Scenario: Validation error
  - Given POST `/api/v1/profiles` with missing or invalid profile attributes
  - When the API validates the request and detects validation errors
  - Then the API responds `400 Bad Request` and returns an error payload describing validation errors.

---

### TS-P002 — Get Profiles
As a frontend developer, I want to retrieve profiles through the API so that I can list profiles as a feature in my application.

Acceptance criteria:
- Get by id
  - Given GET request to `/api/v1/profiles/{profileId}` is received
  - When the API finds the profile
  - Then the API responds `200 OK` and returns the profile with attributes: id (Long), fullName (String), email (String), streetAddress (String).
  - Given GET `/api/v1/profiles/{profileId}` is received for a non-existent id
  - When the API does not find the profile
  - Then the API responds `404 Not Found` and returns an error payload.
- Get all
  - Given GET `/api/v1/profiles` is received
  - When profiles exist
  - Then API responds `200 OK` with a list of items containing: id, fullName, email, streetAddress.
  - Given GET `/api/v1/profiles` is received and no profiles exist
  - When the API finds no profiles
  - Then the API responds with an empty list and responds `200 OK`.

---

### TS-U001 — List Users and Get User by id
As a frontend developer, I want to list users and retrieve a user by id through the API so that I can implement admin views as a feature in my application.

Acceptance criteria:
- List users
  - Given GET request is sent to `/api/v1/users`
  - When users exist
  - Then API responds `200 OK` with a list of items containing user attributes: id (Long), username (String), roles (List<String>).
- Get user by id
  - Given GET `/api/v1/users/{userId}` is received
  - When a user exists
  - Then API responds `200 OK` and returns the user with attributes: id (Long), username (String), roles (List<String>).
  - Given GET `/api/v1/users/{userId}` is received for a non-existent id
  - When the API does not find the user
  - Then API responds `404 Not Found` and returns an error payload.

---

### TS-IAM001 — Sign-up through the API
As a frontend developer, I want to create a new user account through the API so that users can register in my application as a feature in my application.

Acceptance criteria:
- Scenario: Successful sign-up
  - Given a POST `/api/v1/users` with request attributes: username (String), password (String), roles (List<String>)
  - When the API validates the request and creates the user
  - Then the API responds `201 Created` and returns the created user with attributes: id (Long), username (String), roles (List<String>).
- Scenario: Validation error
  - Given a POST `/api/v1/users` with missing or invalid attributes (e.g., blank username or password)
  - When the API validates the request and detects validation errors
  - Then the API responds `400 Bad Request` and returns an error payload describing validation issues.
- Scenario: Duplicate username or create failure
  - Given a POST `/api/v1/users` with a username that already exists or the creation fails
  - When the user creation cannot be completed
  - Then the API responds `409 Conflict` and returns an error payload explaining the failure.

---

### TS-IAM002 — Sign-in through the API
As a frontend developer, I want to sign in users through the API so that I can get authentication tokens for my application as a feature in my application.

Acceptance criteria:
- Scenario: Successful sign-in
  - Given a POST `/api/v1/tokens` with credentials attributes: username (String), password (String)
  - When the API validates credentials successfully
  - Then the API responds `200 OK` and returns the authenticated user with attributes: id (Long), username (String), token (String).
- Scenario: Invalid credentials or malformed request
  - Given a POST `/api/v1/tokens` with invalid credentials or malformed request
  - When the API fails to validate the credentials or request structure
  - Then the API responds `400 Bad Request` and returns an error payload describing the validation error.
- Scenario: User not found
  - Given a POST `/api/v1/tokens` with a non-existent username
  - When the API cannot find the user
  - Then the API responds `404 Not Found` and returns an error payload.

---



### TS-C004 — Update a Course
As a frontend developer, I want to update an existing course through the API so that I can modify course information (title, description) after creation as a feature in my application.

Acceptance criteria:
- Scenario: Successful update
  - Given a PUT request to `/api/v1/courses/{courseId}` is received with a request body containing update-course attributes: title, description
  - When the API validates and updates the course
  - Then the API responds with `200 OK` and returns the updated course with attributes: id (Long), title (String), description (String).
- Scenario: Course not found
  - Given a PUT request to `/api/v1/courses/{courseId}` is received for a non-existent `{courseId}`
  - When the API cannot find the course
  - Then the API responds `404 Not Found` and returns an error payload.
- Scenario: Validation error
  - Given a PUT request to `/api/v1/courses/{courseId}` is received with missing or invalid update-course attributes
  - When the API validates the request and detects validation errors
  - Then the API responds with `400 Bad Request` and returns an error payload describing validation errors.

---

### TS-C005 — Delete a Course
As a frontend developer, I want to delete a course through the API so that I can remove courses from the system as a feature in my application.

Acceptance criteria:
- Scenario: Successful delete
  - Given a DELETE request to `/api/v1/courses/{courseId}` is received
  - When the API successfully deletes the course
  - Then the API responds `204 No Content` with no response body.
- Scenario: Course not found
  - Given a DELETE request to `/api/v1/courses/{courseId}` is received for a non-existent `{courseId}`
  - When the API cannot find the course
  - Then the API responds `404 Not Found` and returns an error payload.

---

### TS-E003 — Get All Enrollments
As a frontend developer, I want to retrieve all enrollments in the system through the API so that I can view enrollment statistics or generate reports as a feature in my application.

Acceptance criteria:
- Scenario: Enrollments exist
  - Given a GET request to `/api/v1/enrollments` is received
  - When the API finds one or more enrollments
  - Then the API responds `200 OK` and returns a list where each item contains enrollment attributes: enrollmentId (Long), studentRecordId (UUID String), courseId (Long), status (String).
- Scenario: No enrollments found
  - Given a GET request to `/api/v1/enrollments` is received and there are no enrollments in the system
  - When the API searches for enrollments and finds none
  - Then the API responds with an empty list and responds `200 OK`.

---

### TS-S003 — Get Student's Enrollments
As a frontend developer, I want to retrieve all course enrollments for a specific student through the API so that I can display a student's course enrollment history as a feature in my application.

Acceptance criteria:
- Scenario: Student has enrollments
  - Given a GET request to `/api/v1/students/{studentRecordId}/enrollments` is received
  - When the API finds one or more enrollments for the student
  - Then the API responds `200 OK` and returns a list where each item contains enrollment attributes: enrollmentId (Long), studentRecordId (UUID String), courseId (Long), status (String).
- Scenario: Student not found
  - Given a GET request to `/api/v1/students/{studentRecordId}/enrollments` is received for a non-existent `{studentRecordId}`
  - When the API cannot find the student
  - Then the API responds `404 Not Found` and returns an error payload.
- Scenario: Student has no enrollments
  - Given a GET request to `/api/v1/students/{studentRecordId}/enrollments` is received for a student with no enrollments
  - When the API finds the student but has no enrollments
  - Then the API responds `200 OK` and returns an empty list.

---

### TS-U002 — Get Available Roles
As a frontend developer, I want to retrieve all available system roles through the API so that I can display role options when creating or updating user accounts as a feature in my application.

Acceptance criteria:
- Scenario: Roles exist
  - Given a GET request to `/api/v1/roles` is received
  - When the API finds one or more roles
  - Then the API responds `200 OK` and returns a list where each item contains role attributes: id (Long), name (String).
- Scenario: No roles found
  - Given a GET request to `/api/v1/roles` is received and there are no roles configured in the system
  - When the API searches for roles and finds none
  - Then the API responds with an empty list and responds `200 OK`.

---


