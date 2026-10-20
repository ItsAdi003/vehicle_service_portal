#!/usr/bin/env bash
# Creates labels and the 15 MVP task issues (T-01 .. T-15) in a GitHub repository.
# Usage:  gh auth login   (once)   then   bash scripts/create-github-issues.sh <owner>/<repo>
set -euo pipefail
REPO="${1:?usage: create-github-issues.sh <owner>/<repo>}"

for l in "enhancement:a2eeef" "task:fbca04" "bug:d73a4a" "sprint-1:0e8a16" "sprint-2:1d76db" "sprint-3:5319e7"; do
  gh label create "${l%%:*}" --color "${l##*:}" --repo "$REPO" --force >/dev/null
done

issue() { # id | title | sprint | labels | acceptance criteria
  gh issue create --repo "$REPO" --title "[$1] $2" --label "$3" --label "sprint-$4" \
    --body "$(printf '**Sprint %s**\n\n### Acceptance criteria\n%s\n\n### Definition of done\nSee docs/02-agile-plan-and-devops-workflow.md#definition-of-done' "$4" "$5")"
}

issue T-01 "Repository, build and application skeleton" task 1 "- [ ] Maven WAR builds\n- [ ] README, .gitignore, templates, branch policy committed"
issue T-02 "User registration with validation" enhancement 1 "- [ ] Valid form creates account\n- [ ] Duplicate e-mail rejected\n- [ ] Invalid input shows field errors"
issue T-03 "Login / logout with session and BCrypt" enhancement 1 "- [ ] Valid login shows user name\n- [ ] Wrong password rejected\n- [ ] Protected pages redirect to /login"
issue T-04 "Service-slot data model and automatic seeding" enhancement 1 "- [ ] Slots for the next 7 days exist after start-up\n- [ ] Seeding is idempotent"
issue T-05 "Slot availability view with remaining seats" enhancement 1 "- [ ] Table of upcoming slots with remaining seats\n- [ ] Full slots show Full and no Book button"
issue T-06 "Booking request form" enhancement 2 "- [ ] Form with vehicle number, model, service type, notes\n- [ ] Created booking has status REQUESTED"
issue T-07 "Booking rules (capacity, past slots)" enhancement 2 "- [ ] Full slot rejected\n- [ ] Past slot rejected"
issue T-08 "My bookings list" enhancement 2 "- [ ] Customer sees only own bookings, newest first"
issue T-09 "Admin: list, confirm, complete bookings" enhancement 2 "- [ ] Admin sees all bookings\n- [ ] Confirm and Complete change status\n- [ ] Non-admin gets 403"
issue T-10 "Cancel booking and release seat" enhancement 2 "- [ ] Cancel sets CANCELLED\n- [ ] Remaining seats increase by one"
issue T-11 "Status tracking page with timeline" enhancement 2 "- [ ] Details page shows status and time-stamped timeline"
issue T-12 "Health endpoint and per-environment configuration" task 3 "- [ ] /actuator/health returns UP\n- [ ] Environment name shown in footer"
issue T-13 "Unit tests (domain rules, services, web)" task 3 "- [ ] At least 15 unit tests pass in mvn test"
issue T-14 "Selenium UI tests for critical journeys" task 3 "- [ ] 5 journeys pass\n- [ ] Screenshot saved on failure"
issue T-15 "Jenkins pipeline, Docker image and Ansible deployment" task 3 "- [ ] Commit-to-container pipeline green\n- [ ] Ansible run idempotent\n- [ ] Rollback demonstrated"
echo "Done: 15 issues created in $REPO"
