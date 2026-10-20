# Contributing and branch policy

## Branch model

| Branch | Purpose | Rules |
|--------|---------|-------|
| `main` | Always releasable, tagged releases only | No direct pushes. Updated by merging `develop` through a pull request. Every merge is tagged `vMAJOR.MINOR.PATCH`. |
| `develop` | Integration branch | No direct pushes. Receives feature branches through pull requests with at least one review and a green Jenkins build. |
| `feature/<short-name>` | New functionality | Branch from `develop`. Example: `feature/slot-availability` |
| `bugfix/<short-name>` | Defect fixes found before release | Branch from `develop` |
| `hotfix/<short-name>` | Urgent fix to a released version | Branch from `main`, merge to both `main` and `develop` |
| `ci/<short-name>` | Pipeline, Docker and build changes | Example: `ci/jenkins-pipeline` |
| `ops/<short-name>` | Infrastructure / configuration management | Example: `ops/ansible-playbook` |
| `test/<short-name>` | Test-only changes | Example: `test/selenium-suite` |

Branch names are lower-case, words separated by hyphens.

## Commit messages

Format: `<type>: <summary in imperative mood>` (max 72 characters), optional body explaining *why*.

Types: `feat`, `fix`, `docs`, `test`, `build`, `ci`, `ops`, `refactor`, `chore`.

Good: `feat: add slot availability page with remaining seats`
Bad: `changes`, `final`, `asdf`

## Pull request checklist

- [ ] Linked issue (`Closes #n`)
- [ ] `mvn clean verify` passes locally
- [ ] Acceptance criteria of the user story are met
- [ ] Docs updated if behaviour changed
- [ ] At least one reviewer approved

## Merge rules

- Feature -> `develop`: merge commit (`--no-ff`) so the feature stays visible in history.
- `develop` -> `main`: merge commit, then tag.
- Resolve conflicts on the feature branch by merging `develop` into it first, never by force-pushing shared branches.

## Definition of Done

See [docs/02-agile-plan-and-devops-workflow.md](docs/02-agile-plan-and-devops-workflow.md#definition-of-done).
