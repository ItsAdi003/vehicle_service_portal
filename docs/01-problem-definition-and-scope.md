# 1. Problem Definition and Scope (Task 2)

## 1.1 Problem statement

Independent workshops and small service centres still take most vehicle-service appointments by phone call,
walk-in or chat message, and record them in a paper register or a shared spreadsheet. Nobody can see
the real availability of service bays at the moment a customer asks, so the same time slot can be promised
twice, cancellations leave bays empty because nobody knows the slot is free again, and customers keep
calling to ask "is my vehicle ready?".

The **Vehicle Service Booking Portal** gives customers a single web page where they can see which slots are
open *right now*, request an appointment, cancel it, and follow its status. It gives the workshop a single
list of all bookings to confirm and complete. The engineering goal of this project is to deliver the portal
through a **complete CI/CD pipeline**: Git -> Jenkins -> automated tests (JUnit and Selenium) ->
Tomcat / Docker deployment -> Ansible-provisioned server, with rollback.

## 1.2 Why it is needed in real time

| Situation | What goes wrong today | What the portal changes |
|-----------|----------------------|-------------------------|
| Two customers ask for the 11:00 slot within minutes | Receptionist promises both; one is turned away at the gate | Available seats are computed from live bookings; a full slot cannot be booked |
| A customer cancels in the morning | Slot stays marked busy in the register; bay idle | Cancelling releases the seat immediately for the next customer |
| Customer wants to know progress | Phone call to the workshop | Status page: Requested -> Confirmed -> Completed |
| Workshop wants to know the day's load | Manually count register entries | One admin list of all bookings with status |

> The pain points above are the problem scenario assumed for this lab project. They are design inputs, not
> statistics from a survey.

## 1.3 Target users

| User | Description | Main needs |
|------|-------------|-----------|
| **Customer** | Two- or four-wheeler owner who wants a service appointment | See open slots, book quickly, cancel, know the status |
| **Service admin** | Receptionist / service advisor of the workshop | See every booking, confirm it, mark it completed |

## 1.4 Stakeholder list

| Stakeholder | Role / interest | Influence | Engagement |
|-------------|-----------------|-----------|-----------|
| Customers (vehicle owners) | Use the portal; want speed and certainty | Medium | Acceptance criteria written as user stories |
| Workshop owner / manager | Wants fewer empty bays and fewer phone calls | High | Approves MVP scope, reviews sprint demo |
| Service advisors (admin users) | Operate the booking list daily | High | Admin stories reviewed with them |
| Student developer (project owner) | Builds, tests, deploys, documents | High | Owns backlog and pipeline |
| Course instructor / evaluator | Judges the DevOps deliverables | High | Reviews evidence for each lab task |
| Reviewer / peer | Reviews pull requests | Medium | PR review before merge to `develop` |
| Infrastructure / server owner | Provides Tomcat host, Docker, Jenkins | Medium | Ansible playbook documents prerequisites |

## 1.5 Objectives

| ID | Objective (SMART) |
|----|-------------------|
| O1 | A customer can register, find a slot and submit a booking request in **at most 6 page interactions** by the end of Sprint 2 |
| O2 | The system never allows more bookings than a slot's capacity (**0 overbooked slots** in tests) |
| O3 | All 5 critical user journeys are covered by automated Selenium tests that pass in Jenkins by the end of Sprint 3 |
| O4 | A commit to `develop` produces a tested WAR and a deployed container **without manual steps**, in under 10 minutes on the lab machine |
| O5 | A failing test **always stops deployment** (demonstrated with one deliberate defect) |
| O6 | A clean node can be provisioned and the app deployed with one Ansible command; a second run reports **0 changes** (idempotent) |
| O7 | The previous release can be restored in under 2 minutes (rollback demonstration) |

## 1.6 Constraints

| Type | Constraint |
|------|-----------|
| Technology | Java 17, Maven, Tomcat 10.1 (Jakarta EE 10) or Docker; free tools only (Jenkins, Docker Hub free tier, Ansible) |
| Time | Single semester lab; MVP frozen at 15 tasks across 3 sprints |
| Team | One developer plus a peer reviewer |
| Infrastructure | A laptop / one VM; Jenkins, Tomcat and the app share one machine, so ports must be separated (Jenkins 8080, Tomcat 8081, container 9090) |
| Security | Session login with BCrypt password hashes. No CSRF protection, no HTTPS, no rate limiting in MVP (documented limitation) |
| Data | Demo data only; no real customer data; H2 file database |
| Scope | No payments, no SMS/e-mail notification, no multi-branch workshops |

## 1.7 Measurable success criteria

| # | Criterion | Target | How it is measured |
|---|-----------|--------|--------------------|
| S1 | MVP user stories accepted | 100 % of the 10 Must stories | Acceptance criteria checked in demo |
| S2 | Unit tests | >= 15 tests, all passing | `mvn test` report |
| S3 | UI tests | 5 journeys, all passing | `mvn verify` Failsafe report, Jenkins test report |
| S4 | Build reproducibility | 3 consecutive green Jenkins builds from a clean workspace | Jenkins build history |
| S5 | Pipeline speed | Commit to running container < 10 min | Jenkins stage view |
| S6 | Deployment correctness | `/actuator/health` returns `UP` after every deployment | Health-check stage |
| S7 | Gate effectiveness | Failed Selenium test => no deployment stage executed | Failed-build evidence |
| S8 | Idempotency | 2nd Ansible run: `changed=0` | Ansible play recap |
| S9 | Recovery | Rollback to previous release restores `UP` health | Rollback playbook log |

## 1.8 Approved MVP scope (frozen) - 15 tasks

**In scope:** registration, login/logout, slot availability view, booking request with validation,
my-bookings list, admin confirmation and completion, cancellation with seat release, status tracking,
health endpoint, automated tests, CI/CD, containerisation, configuration management.

**Out of scope (post-MVP backlog):** e-mail/SMS notifications, rescheduling, payments and invoices, vehicle
service history, multiple branches, password reset, role management UI, HTTPS/CSRF hardening.

| Task | Title | Sprint |
|------|-------|--------|
| T-01 | Repository, build and application skeleton | 1 |
| T-02 | User registration with validation | 1 |
| T-03 | Login / logout with session and BCrypt | 1 |
| T-04 | Service-slot data model and automatic seeding | 1 |
| T-05 | Slot availability view with remaining seats | 1 |
| T-06 | Booking request form | 2 |
| T-07 | Booking rules (capacity, past slots) | 2 |
| T-08 | My bookings list | 2 |
| T-09 | Admin: list, confirm, complete bookings | 2 |
| T-10 | Cancel booking and release seat | 2 |
| T-11 | Status tracking page with timeline | 2 |
| T-12 | Health endpoint and per-environment configuration | 3 |
| T-13 | Unit tests (domain rules, services, web) | 3 |
| T-14 | Selenium UI tests for critical journeys | 3 |
| T-15 | Jenkins pipeline, Docker image and Ansible deployment | 3 |

**Change control:** after the freeze, a new request is written as a story in the backlog and only enters a
sprint if an item of equal size leaves it.

## 1.9 Assumptions and risks

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Jenkins and Tomcat both default to port 8080 | Deployment fails | Tomcat moved to 8081 (documented) |
| Tomcat 9 used by mistake | Spring Boot 3 WAR does not start (needs `jakarta.*`) | Document Tomcat 10.1 requirement |
| Selenium browser/driver mismatch on Jenkins host | UI stage fails | Selenium Manager resolves driver; Chrome installed on agent |
| H2 file locked by two deployments | App fails to start | One DB file per environment name |
| Lab machine offline during demo | No evidence | Capture screenshots after every task |
