# Backlog status

## After MVP baseline (tag `v1.0.0`)

| Task | Title | Story | Status | Delivered in |
|------|-------|-------|--------|--------------|
| T-01 | Repository, build and application skeleton | - | Done | `main` initial commits |
| T-02 | User registration with validation | US-01 | Done | `feature/user-registration-login` |
| T-03 | Login / logout with session and BCrypt | US-02 | Done | `feature/user-registration-login` |
| T-04 | Service-slot data model and automatic seeding | US-03 | Done | `feature/slot-availability` |
| T-05 | Slot availability view with remaining seats | US-03 | Done | `feature/slot-availability` |
| T-06 | Booking request form | US-04 | Done | `feature/booking-workflow` |
| T-07 | Booking rules (capacity, past slots) | US-04 | Done | `feature/booking-workflow` |
| T-08 | My bookings list | US-05 | Done | `feature/booking-workflow` |
| T-09 | Admin: list, confirm, complete bookings | US-06/07/10 | Done | `feature/booking-workflow` |
| T-10 | Cancel booking and release seat | US-08 | Done | `feature/booking-workflow` |
| T-11 | Status tracking page with timeline | US-09 | Done | `feature/booking-workflow` |
| T-12 | Health endpoint and per-environment configuration | US-11 | Done | skeleton + `app.environment` |
| T-13 | Unit tests | - | Done (extended later) | each feature branch |
| T-14 | Selenium UI tests | - | To Do | `test/selenium-suite` |
| T-15 | Jenkins pipeline, Docker image and Ansible deployment | - | To Do | `ci/*`, `ops/*` |

Also delivered: `feature/ui-navigation` (navigation styling), which caused the documented merge conflict.

## After CI/CD and provisioning (tag `v1.2.0`)
All 15 MVP tasks Done. Remaining items are the post-MVP backlog (e-mail/SMS notification, reschedule, service
history, CSRF and HTTPS, MySQL/PostgreSQL option) as listed in
[02-agile-plan-and-devops-workflow.md](02-agile-plan-and-devops-workflow.md#22-product-backlog).
