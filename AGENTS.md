# Repository Guidelines

## Project Structure & Module Organization
This repository is a Java 17 Maven backend for a blog system. `blog-server/` contains the Spring Boot application, controllers, services, MyBatis mappers, XML mapper files, and runtime configuration under `src/main/resources/`. `blog-pojo/` holds shared entities, DTOs, and VOs in `com.blog.*`. `blog-common/` contains shared constants, context, JSON, result wrappers, properties, and utility classes. Tests currently live in `blog-server/src/test/java/`.

## Build, Test, and Development Commands
Use Maven from the repository root for shared modules, then run the app from `blog-server/`.

- `mvn clean install`  
  Builds and installs `blog-common` and `blog-pojo` into the local Maven repository.
- `mvn -f blog-server/pom.xml test`  
  Runs the Spring Boot test suite for the server module.
- `mvn -f blog-server/pom.xml spring-boot:run`  
  Starts the API locally using `blog-server/src/main/resources/application.yml`.
- `mvn -f blog-server/pom.xml package`  
  Produces the server artifact after shared modules are installed.

## Coding Style & Naming Conventions
Follow the existing Java style: 4-space indentation, one top-level class per file, and package names in lowercase. Keep controllers in `controller`, business logic in `service` and `service.impl`, persistence interfaces in `mapper`, and shared models in `blog-pojo`. Use `PascalCase` for classes, `camelCase` for fields and methods, and suffix transfer models consistently: `*DTO`, `*VO`, `*Properties`, `*Util`. Lombok is already used for boilerplate reduction.

## Testing Guidelines
JUnit 5 and `spring-boot-starter-test` are configured. Add tests under `blog-server/src/test/java` and mirror the production package structure. Name test classes `*Tests` and focus on controller integration, service behavior, and mapper queries that change filtering or status logic. Run `mvn -f blog-server/pom.xml test` before opening a PR.

## Commit & Pull Request Guidelines
The visible Git history is minimal (`完成版`), so there is no reliable house style to preserve. Use short, imperative commit messages with an optional module prefix, for example `blog-server: fix article status toggle`. PRs should include a concise summary, affected modules, setup or config changes, linked issues, and example requests or screenshots when API behavior changes.

## Security & Configuration Tips
Review `application.yml` and `application-dev.yml` before committing. Do not commit real database credentials, JWT secrets, or OSS keys. Keep environment-specific values external when possible, prefer untracked `application-local.yml` or environment variables for local secrets, and document any new required properties in the PR. Local config may live in the current working directory or under `blog-server/`, but it must stay untracked.
