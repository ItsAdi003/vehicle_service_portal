# 7. Selenium test design and execution (Tasks 10, 11)

## 7.1 Test plan

### Objective
Prove automatically that the critical user journeys of the Vehicle Service Booking Portal work in a real browser, and
make a failing journey **stop the pipeline** before anything is deployed.

### Test levels
| Level | Tool | Naming | Maven phase | Count (approx.) |
|-------|------|--------|-------------|-----------------|
| Unit (domain rules, services) | JUnit 5 + Mockito | `*Test.java` | `test` (Surefire) | 34 |
| Web slice (controllers, validation, security) | Spring MockMvc | `*WebTest.java` | `test` (Surefire) | 21 |
| **UI journeys** | **Selenium WebDriver 4 + headless Chrome** | `*IT.java` in `com.vsbp.ui` | `verify` (Failsafe) | 10 test methods in 5 classes |

### Environment
The UI tests start the real application on a random port with an in-memory H2 database (`BaseSeleniumIT`), so
`mvn verify` is self-contained. Slot capacity is raised to 100 so tests never run out of seats. To test a deployed instance
instead pass `-Dapp.baseUrl=http://localhost:8081/vsbp-dev`.

Prerequisite: Google Chrome installed. Selenium Manager downloads the matching driver automatically; a `chromedriver` on the
`PATH` is used if present.

### Critical user journeys (5)
| ID | Class | Journey | Key assertions |
|----|-------|---------|----------------|
| J1 | `RegistrationLoginIT` | Register -> log in -> log out; weak password and duplicate e-mail; wrong password | Success flash after registration; user name in header; "Password must be 8 to 72 characters"; "already registered"; "Invalid email or password"; guest redirected to `/login` after logout |
| J2 | `SlotAvailabilityIT` | Logged-in customer opens the slot list; guest is blocked | At least 28 slot rows; every seats cell is a number or "Full"; guest lands on login |
| J3 | `BookingRequestIT` | Pick a slot -> fill form -> submit; submit empty form | Flash "requested"; vehicle number upper-cased `KA01AB1234`; status `REQUESTED`; three field errors |
| J4 | `ConfirmAndTrackIT` | Customer books -> admin confirms and completes -> customer tracks | Statuses `REQUESTED` -> `CONFIRMED` -> `COMPLETED`; timeline entries present; customer opening `/admin/bookings` gets **403** |
| J5 | `CancelBookingIT` | Customer books and cancels | Seats left decrease by 1 after booking and return to the original value after cancelling; status `CANCELLED`; Cancel button disappears |

### Test data
| Data | Value | Source |
|------|-------|--------|
| Customer e-mail | `ui-<8 random hex>@example.com` | generated per test (no collisions, tests are independent) |
| Customer password | `Secret123` | constant |
| Admin account | `admin@vsbp.local` / `Admin@123` | seeded on start-up |
| Vehicles | `KA01AB1234`, `MH12AB9999`, `TN09XY4321` | constants in the tests |
| Slots | 7 days x 4 slots, capacity 100 | seeded on start-up |

### Design rules used
- Locators are stable ids (`login-btn`, `confirm-btn-<id>`, `seats-left` classes) added to the templates for testability.
- Every navigation is followed by an explicit wait (`waitForPath`) and status checks use `ExpectedConditions.textToBe`:
  no `Thread.sleep`.
- Each test starts with a fresh browser and a fresh customer.

## 7.2 Running locally through Maven

```bash
mvn clean verify                                   # unit tests + Selenium journeys
mvn clean verify -Dselenium.headless=false         # watch the browser
mvn -Dit.test=CancelBookingIT verify               # one journey
mvn test-compile failsafe:integration-test failsafe:verify   # only the UI tests (what Jenkins runs)
mvn surefire-report:failsafe-report-only           # HTML report
```

Reports and evidence:

| What | Where |
|------|-------|
| Console summary | `Tests run: ..., Failures: 0` at the end of `mvn verify` |
| Unit test reports | `target/surefire-reports/` |
| Selenium reports | `target/failsafe-reports/` (`*.txt`, `TEST-*.xml`) |
| HTML report | `target/site/failsafe-report.html` |
| Failure screenshots | `target/screenshots/<Class>_<method>_<timestamp>.png` (+ `.html` page source) |

## 7.3 Failure screenshot mechanism
`ScreenshotOnFailureExtension` is a JUnit 5 `TestExecutionExceptionHandler` registered in `BaseSeleniumIT`. When any
test method throws it saves a PNG and the page HTML under `target/screenshots/` **while the browser is still open**
and then re-throws the original failure. (A `TestWatcher` would be too late because `@AfterEach` has already closed the
browser.) `ScreenshotOnFailureExtensionTest` proves the mechanism with a mocked driver.

To see it work, make an assertion fail on purpose (for example change `"Invalid email or password"` to a different text in
`RegistrationLoginIT`), run `mvn verify` and open the new PNG in `target/screenshots/`.

## 7.4 Exit criteria
All 5 journeys pass; no skipped tests; screenshots produced for every failure; the suite finishes in under 3 minutes.
