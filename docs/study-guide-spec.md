# Study Guide Specification

## Using AI to Analyze and Learn from a Codebase

Use the following prompt to create a study guide specification for analyzing a Codebase:

---

```text
Given I am a Software Engineering Student and this Codebase is an example of a RESTful API, part of the Open-Source 
Application Development course I am enrolled in, so analyze the codebase and help me understand the approaches, 
patterns, Framework and language features applied, so I can apply them in my own projects. Based on this, create a 
Study Guide (docs/study-guide.md), including detailed explanations and references for each relevant topic, 
so I as an student can use it as a source for learning and developing my skills.
```

---

A more specific prompt can be created by adding the following information:

```text
Given I am a Software Engineering Student and this Codebase is an example of a RESTful API,
part of the Open-Source Application Development course I am enrolled in, analyze the codebase
and help me understand the approaches, patterns, frameworks, and language features applied,
so I can apply them in my own projects.

Based on this analysis, create a comprehensive Study Guide (docs/study-guide.md) that serves
as a detailed learning resource. The guide should include:

### Structure Requirements
- A table of contents with all sections and subsections
- 12 major sections covering: Project Overview, Architecture (including SOLID Principles),
  DDD Patterns, CQRS, Modern Java Features (Java 21+), Spring Boot Framework Patterns,
  Spring Security & JWT Authentication, REST API Design & Best Practices,
  Persistence with Spring Data JPA, Testing Strategies, Cross-Cutting Concerns,
  and Recommended Learning Resources
- End with a Summary section containing key takeaways
- Each pattern/feature section should follow this structure:
  1. Explanation of what it is
  2. Code example from the actual codebase
  3. **"Why this matters"** — practical relevance for real-world development (PRIMARY focus)
  4. **"Origin"** — concise author attribution with a link (SECONDARY, one sentence maximum)

### Priority: Practical Relevance Over Historical Attribution (CRITICAL)
The guide should prioritize **"why patterns matter for you as a developer"** over
**"who invented this pattern"**. Every section must explain:
- Concrete benefits (testability, maintainability, scalability, safety)
- Real-world scenarios where the pattern solves a problem
- What happens if you don't use the pattern
- How the codebase specifically applies it

Author attribution is required but should be **condensed to one sentence** with a clickable link,
placed AFTER the "Why this matters" section. For example:

**GOOD** (practical first, origin second):
---
**Why this matters**: Aggregates are the fundamental unit of consistency in your application.
Every business rule that must be enforced atomically lives inside an aggregate. Without
aggregates, you risk inconsistent state when multiple parts of your domain change simultaneously.

**Origin**: From **Eric Evans'** "Domain-Driven Design" (2003) ([book](https://...)).
---

**BAD** (origin first, lengthy historical narrative):
---
**Origin**: This concept comes from Eric Evans' "Domain-Driven Design" (2003). Evans introduced
aggregates to manage consistency boundaries in complex domains. Vaughn Vernon further clarified
the pattern in "Implementing Domain-Driven Design" (2013), emphasizing that aggregates are the
primary units of transaction and consistency.

**Why this matters**: Aggregates are the fundamental unit of consistency...
---

### Author Attribution Requirements
Throughout the entire guide, every architectural pattern, design pattern, and principle
must be attributed to its original author or originator. Attribution should be concise
(one sentence) and include a clickable link where available. Key authors to reference:
- Hexagonal Architecture → Alistair Cockburn (2005)
- Clean Architecture → Robert C. Martin
- Layered Architecture → Martin Fowler
- SOLID Principles → Robert C. Martin (Liskov Substitution → Barbara Liskov)
- DDD patterns → Eric Evans & Vaughn Vernon
- Repository Pattern → Martin Fowler (PoEAA, 2002)
- Design Patterns → Gang of Four (1994)
- CQRS → Greg Young (2009), Bertrand Meyer (CQS, 1988), Udi Dahan
- Dependency Injection → Martin Fowler (2004)
- REST → Roy Fielding (2000)
- JWT → RFC 7519 (2015)
- BCrypt → Niels Provos & David Mazieres (1999)
- Java features → OpenJDK JEPs with specific JEP numbers and links

### Online References (CRITICAL)
Every section must include clickable URLs to authoritative sources:
- **Java features**: Link to the specific JEP (e.g., [JEP 396](https://openjdk.org/jeps/396))
- **Standards**: Link to RFCs (e.g., [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html))
- **Books**: Link to publisher pages (O'Reilly, InformIT, etc.)
- **Frameworks**: Link to official documentation (Spring Boot, Spring Security, Spring Data JPA)
- **Tools**: Link to documentation (JUnit, Mockito, AssertJ)
- **Articles**: Link to original blog posts or articles (Martin Fowler's site, etc.)

Target: At least 70+ clickable URLs throughout the guide.

### SOLID Principles Section
Include a dedicated subsection within the Architecture section presenting SOLID
principles as a table mapping each principle to:
- The principle name and author
- How the codebase specifically applies it with concrete class examples

### Code Examples
- Extract real code snippets from the codebase to illustrate each concept
- Show both the pattern definition and how it's implemented in the project
- Include examples from multiple layers: domain (aggregates, value objects),
  application (services, commands), infrastructure (repositories, assemblers),
  and interfaces (controllers, transformers)
- Show the `Result<T, E>` sealed interface pattern with pattern matching
- Show virtual thread configuration
- Show domain event registration and handling

### Learning Resources Section
The resources section must be extensively organized with these subcategories:
- Architecture & Design Fundamentals (GoF, Martin Fowler, Robert C. Martin)
- Domain-Driven Design (Eric Evans, Vaughn Vernon)
- Architectural Styles (Cockburn, Martin, Palermo)
- CQRS & Event Sourcing (Greg Young, Udi Dahan)
- SOLID Principles (Robert C. Martin)
- Java Modern Features (JEPs, books)
- Spring Boot (official docs, books)
- REST API Design (RFCs, books)
- Testing (books, frameworks)

Each resource must:
- Be a clickable markdown link
- Include an Author column in its table
- Point to authoritative sources (O'Reilly, Martin Fowler's site, official docs)

### Summary Section
End with key takeaways (12-15 bullets) that:
- Attribute patterns to their authors in parentheses
- Reinforce the connection between theory and practice
- Cover all major topics: architecture, SOLID, DDD, CQRS, Java features,
  DI, persistence, testing, error handling, i18n

### Tone and Audience
- Write for a student who wants to understand **why patterns matter** first,
  then **who invented them** second
- Connect each pattern to real code in the project
- Provide enough context that a student can use this guide to study for exams
  and build their own projects using these patterns
- Use concrete benefits ("testability", "maintainability", "scalability")
  rather than abstract praise
- End with a practical tip for applying these patterns in their own projects

### Anti-Corruption Layer & Transformer Pattern
Explain how the project keeps JPA concerns out of the domain model through:
- Separate domain and persistence models
- Assemblers that map between layers
- Attribute converters for value objects
- The transformer pattern for REST resources → commands → domain

### Testing Strategies
Explain the two-tier testing approach:
- Domain tests: Pure unit tests without Spring or database (fast, no mocking)
- Infrastructure tests: Mocking-based tests for adapters (Mockito, AssertJ)
- Include the Arrange-Act-Assert pattern
- Reference testing tools with links to their documentation

```



