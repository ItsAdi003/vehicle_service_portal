# 4. Git and GitHub Workflow (Tasks 5, 6, 7)

## 4.1 Create the GitHub repository and push this project (Task 5)

1. On GitHub: **New repository** -> name `vehicle-service-portal` -> *no* README/.gitignore/licence (they already exist) -> Create.
2. In the project folder:
   ```bash
   git remote add origin https://github.com/<your-username>/vehicle-service-portal.git
   git push -u origin main develop --tags
   git push origin --all
   ```
3. Create the labels and issues (needs the GitHub CLI, `gh auth login` first):
   ```bash
   bash scripts/create-github-issues.sh <your-username>/vehicle-service-portal
   ```
   It creates labels and the 15 task issues T-01 ... T-15 with their acceptance criteria.
4. **Branch protection** (Settings -> Branches -> Add rule), for `main` and `develop`:
   - Require a pull request before merging, 1 approval
   - Require status checks to pass (add the Jenkins check once the webhook exists)
   - Do not allow force pushes
5. Optional: GitHub Projects -> new Board -> add the 15 issues -> columns To Do / In Progress / In Review / Done.

Deliverable evidence for Task 5: repository URL, README rendered on GitHub, `git log --oneline` output,
Issues tab with 15 issues, Settings -> Branches screenshot, `CONTRIBUTING.md` (branch policy).

## 4.2 Branch policy summary
See [CONTRIBUTING.md](../CONTRIBUTING.md): `main` (releases, tagged) <- `develop` (integration) <- `feature/*`,
`bugfix/*`, `ci/*`, `ops/*`, `test/*`.

## 4.3 Feature branch and pull request (Task 6)

Feature 1 = **user registration and login** (branch `feature/user-registration-login`).

Commands used (all in the repository history - see `git log --graph --oneline --all`):

```bash
git checkout develop
git pull origin develop
git checkout -b feature/user-registration-login
# ... edit code ...
git add src/main/java/com/vsbp/domain src/main/java/com/vsbp/repository
git commit -m "feat: add AppUser entity, role and repository"
git add -p                               # stage selectively
git commit -m "feat: register customers with validation and BCrypt hashing"
git push -u origin feature/user-registration-login
git log --oneline --graph --decorate
git pull origin develop                  # bring in changes before the PR
```

Then on GitHub: **Compare & pull request** -> base `develop` <- compare `feature/user-registration-login`.
Use the PR template. Ask the reviewer to leave 2-3 line comments (examples below), address them with a
new commit on the same branch, and click **Merge pull request** (merge commit).

Example review comments (use real ones from your reviewer):
- `UserService.register`: "normalise the e-mail to lower case before the duplicate check" -> fixed in the next commit.
- `RegistrationForm`: "add a max length to full name" -> fixed.
- `AuthController`: "regenerate the session id after login (session fixation)" -> already done, explained in reply.

Merge evidence: PR page showing *Merged*, `git log --graph` showing the merge commit on `develop`.

### Replaying the first pull request on GitHub

In this repository `feature/user-registration-login` is already merged into `develop` locally, and GitHub
will not open a pull request for a branch with nothing left to compare. To get a genuine pull request with
review comments, publish `develop` as it was *before* that merge, push the feature branch, and do the merge
on GitHub:

```bash
git remote add origin https://github.com/<your-username>/vehicle-service-portal.git
git push origin 5ba2357:refs/heads/main          # initial commits only
git push origin 5ba2357:refs/heads/develop       # develop before the first feature merge
git push origin feature/user-registration-login  # 5 commits waiting for review
```

1. On GitHub open **Compare & pull request** for `feature/user-registration-login` into `develop`, fill in the template,
   ask the reviewer for line comments, answer them and merge with **Create a merge commit**.
2. Bring your local `develop` in line and publish the rest of the history:
   ```bash
   git checkout develop
   git fetch origin
   git merge --no-edit origin/develop     # adds GitHub's merge commit; the files are identical
   git push origin develop main --tags
   git push origin --all
   ```

`5ba2357` is the commit *"docs: add problem definition, MVP scope, agile plan and diagram sources"*
(`git log --first-parent --oneline develop` lists it just below the first feature merge). The procedure was
rehearsed against a scratch remote: after step 2 the tree of `develop` is byte-identical to the original.

## 4.4 Second feature branches, conflict and tag (Task 7)

Branches merged into `develop`: `feature/slot-availability`, `feature/ui-navigation`,
`feature/booking-workflow`. The last two both edit the navigation block of `fragments/layout.html`, which
produces a genuine merge conflict.

```bash
git checkout develop
git merge --no-ff feature/ui-navigation          # clean
git merge --no-ff feature/booking-workflow       # CONFLICT in layout.html
git status                                       # shows both modified
#   edit the file, remove <<<<<<< ======= >>>>>>> markers, keep both sets of links
git add src/main/resources/templates/fragments/layout.html
git commit                                       # completes the merge
git tag -a v1.0.0 -m "MVP release"
git push origin develop --tags
```

Conflict as it appeared and how it was resolved: see the section *Merge conflict evidence* in the report and
`docs/evidence/merge-conflict.md` in the repository.

Tags: `v1.0.0` MVP baseline; `v1.1.0` CI/CD and tests; `v1.2.0` Ansible provisioning.

## 4.5 Everyday command cheat-sheet
| Task | Command |
|------|---------|
| Status | `git status` |
| Stage / commit | `git add <files>`; `git commit -m "type: summary"` |
| History | `git log --oneline --graph --decorate --all` |
| Who changed a line | `git blame <file>` |
| Fetch + merge | `git pull origin develop` |
| Publish a branch | `git push -u origin <branch>` |
| Undo a pushed commit safely | `git revert <sha>` |
| List tags | `git tag -n` |
