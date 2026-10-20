# Vehicle Service Booking Portal

CI/CD pipeline project (DevOps lab) built around a small web application where customers
book, cancel and track vehicle service appointments and a service-centre admin confirms them.

| Item | Choice |
|------|--------|
| Language / framework | Java 17, Spring Boot 3.3 (Spring MVC + Thymeleaf + Spring Data JPA) |
| Build tool | Maven 3.9 (packages a **WAR**) |
| Database | H2 (file mode) - zero setup; switchable through `DB_URL` |
| Deployment targets | Tomcat 10.1 (WAR) and Docker (`java -jar`) |
| Tests | JUnit 5 + Mockito (unit), Selenium WebDriver (UI, run by Failsafe) |
| CI/CD | Jenkins declarative pipeline (`Jenkinsfile`) |
| Config management | Ansible (`ansible/`) |

## Quick start (local)

Prerequisites: JDK 17+, Maven 3.9+ (Chrome only for the Selenium tests).

```bash
git clone <your-repo-url> vehicle-service-portal
cd vehicle-service-portal
mvn spring-boot:run                 # http://localhost:8080
```

Demo accounts (created on first start - change them for any real deployment):

| Role | Email | Password |
|------|-------|----------|
| Admin (service centre) | `admin@vsbp.local` | `Admin@123` |
| Customer | register on `/register` | your choice (min 8 chars) |

Health check: <http://localhost:8080/actuator/health>

## Common commands

| Goal | Command |
|------|---------|
| Compile | `mvn clean compile` |
| Unit tests | `mvn test` |
| Package WAR | `mvn clean package -DskipTests` -> `target/vehicle-service-portal.war` |
| Package for an environment | `mvn clean package -DskipTests -Dapp.environment=staging` |
| Unit + Selenium tests | `mvn clean verify` |
| Selenium with visible browser | `mvn verify -Dselenium.headless=false` |
| Run packaged WAR | `java -jar target/vehicle-service-portal.war` |
| Docker image | `docker build -t vehicle-service-portal:1.0.0 .` |

## Repository layout

```
.
|-- Jenkinsfile                 CI/CD pipeline as code
|-- Dockerfile                  multi-stage image build
|-- ansible/                    inventory, playbooks, role for the target node
|-- docs/                       problem statement, agile plan, SRS, test plan, guides
|-- src/main/java/com/vsbp/     application code (config, domain, repository, service, web)
|-- src/main/resources/         templates (Thymeleaf), CSS, application.properties
`-- src/test/java/com/vsbp/     unit tests and Selenium UI tests
```

## Documentation

All lab deliverables live in [`docs/`](docs/):

1. [Problem definition and scope](docs/01-problem-definition-and-scope.md)
2. [Agile plan and DevOps workflow](docs/02-agile-plan-and-devops-workflow.md)
3. [SRS, architecture and local setup](docs/03-srs-architecture-and-setup.md)
4. [Git workflow and branch policy](docs/04-git-workflow.md)

## Branching and commits

See [CONTRIBUTING.md](CONTRIBUTING.md): `main` is always releasable, `develop` is the integration
branch, work happens on `feature/*`, `bugfix/*`, `ci/*`, `ops/*`, merged through pull requests.
