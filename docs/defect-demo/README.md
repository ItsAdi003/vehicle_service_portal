# Deliberate defect demonstration (Task 11)

Goal: show that a failing Selenium test **stops deployment**, then fix the defect and show the pipeline going green.

## The defect
`cancel-seat-defect.patch` changes one word in `SlotService.availableSeats`: the seat count ignores `COMPLETED`
bookings instead of `CANCELLED` ones. The application still compiles and every unit test still passes, but
**cancelling a booking no longer frees its seat**. Only the browser journey `CancelBookingIT` notices
(seats left after cancelling do not return to the original number).

## Steps (work on `develop`, the branch Jenkins polls)

```bash
git checkout develop
git pull origin develop

# 1. introduce the defect
git apply docs/defect-demo/cancel-seat-defect.patch
git commit -am "test: deliberately break seat release on cancel (demo defect)"
git push origin develop
```
Jenkins starts automatically. Expected: stages Build, Unit Tests, Package are green, **Selenium UI Tests is red**,
the stages *Deploy to Tomcat* and *Verify Tomcat Deployment* (and the Docker stages, once added) are **skipped** and the
application on Tomcat keeps running the previous version. Capture: Stage View, Test Result page showing
`CancelBookingIT.cancellingReleasesTheSeat` failed, the archived screenshot in *Build Artifacts*, console error
`expected: 3 but was: 2`-style assertion.

```bash
# 2. correct the defect with a visible fix commit
git revert --no-edit HEAD
git commit --amend -m "fix: restore seat release on cancel (excludes CANCELLED bookings from the seat count)"
git push origin develop
```
Jenkins builds again. Expected: all stages green and the deploy stages run. Capture: the new green Stage View,
*Test Result* with 0 failures, and `git log --oneline -3` showing *defect commit -> fix commit*.

## Why not simply break a unit test?
A unit test failure would stop the pipeline earlier, at *Unit Tests*, and would not demonstrate the Selenium gate.
