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

| Chapter | Content | Lab task |
|---------|---------|----------|
| [1. Problem definition and scope](docs/01-problem-definition-and-scope.md) | Problem statement, stakeholders, objectives, constraints, 15-task MVP | 2 |
| [2. Agile plan and DevOps workflow](docs/02-agile-plan-and-devops-workflow.md) | User stories, backlog, Kanban, sprints, Definition of Done, lifecycle diagram | 3 |
| [3. SRS, architecture and setup](docs/03-srs-architecture-and-setup.md) | Requirements, use cases, architecture, data model, routes, local setup | 4 |
| [4. Git workflow](docs/04-git-workflow.md) | Repository setup, branch policy, PR flow, conflict and tags | 5, 6, 7 |
| [5. Backlog status](docs/05-backlog-status.md) | Updated backlog after the MVP baseline | 7 |
| [6. Jenkins CI/CD](docs/06-jenkins-ci-cd.md) | Jenkins install, freestyle job, pipeline, Selenium gate, Docker deployment | 8, 9, 11, 13 |
| [7. Selenium test plan](docs/07-selenium-test-plan.md) | Journeys, test data, run and report instructions | 10, 11 |
| [8. Docker](docs/08-docker.md) | Dockerfile, container lifecycle | 12 |
| [9. Ansible](docs/09-ansible-configuration-management.md) | Configuration spec, playbooks, idempotency, rollback | 14, 15 |

Also: [merge conflict evidence](docs/evidence/merge-conflict.md), [defect demonstration](docs/defect-demo/README.md).

## Branching and commits

See [CONTRIBUTING.md](CONTRIBUTING.md): `main` is always releasable, `develop` is the integration
branch, work happens on `feature/*`, `bugfix/*`, `ci/*`, `ops/*`, merged through pull requests.
