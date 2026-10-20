# Merge conflict evidence (Task 7)

## Scenario
Two branches were created from the same `develop` commit and both changed the navigation block of
`src/main/resources/templates/fragments/layout.html`:

| Branch | Change |
|--------|--------|
| `feature/ui-navigation` | Added `class="nav-link"` and ids to the existing links (restyling) |
| `feature/booking-workflow` | Inserted two new links, *My bookings* and *Admin bookings* |

`feature/ui-navigation` was merged first (clean). Merging `feature/booking-workflow` then failed:

```
$ git merge --no-ff feature/booking-workflow
Auto-merging src/main/resources/templates/fragments/layout.html
CONFLICT (content): Merge conflict in src/main/resources/templates/fragments/layout.html
Automatic merge failed; fix conflicts and then commit the result.
```

## The conflicted file (as Git wrote it)

```html
<<<<<<< HEAD
      <a class="nav-link" th:href="@{/}" id="nav-home">Home</a>
      <a class="nav-link" th:if="${me != null}" th:href="@{/slots}" id="nav-slots">Available slots</a>
      <a class="nav-link" th:if="${me == null}" th:href="@{/login}" id="nav-login">Login</a>
      <a class="nav-link" th:if="${me == null}" th:href="@{/register}" id="nav-register">Register</a>
=======
      <a th:href="@{/}">Home</a>
      <a th:if="${me != null}" th:href="@{/slots}" id="nav-slots">Available slots</a>
      <a th:if="${me != null and !me.admin}" th:href="@{/bookings}" id="nav-bookings">My bookings</a>
      <a th:if="${me != null and me.admin}" th:href="@{/admin/bookings}" id="nav-admin">Admin bookings</a>
      <a th:if="${me == null}" th:href="@{/login}" id="nav-login">Login</a>
      <a th:if="${me == null}" th:href="@{/register}" id="nav-register">Register</a>
>>>>>>> feature/booking-workflow
```

## Resolution
Both intentions are wanted: keep the new links from `feature/booking-workflow` **and** the `nav-link` styling from
`feature/ui-navigation`. The markers were removed and the two new links also received `class="nav-link"`:

```html
      <a class="nav-link" th:href="@{/}" id="nav-home">Home</a>
      <a class="nav-link" th:if="${me != null}" th:href="@{/slots}" id="nav-slots">Available slots</a>
      <a class="nav-link" th:if="${me != null and !me.admin}" th:href="@{/bookings}" id="nav-bookings">My bookings</a>
      <a class="nav-link" th:if="${me != null and me.admin}" th:href="@{/admin/bookings}" id="nav-admin">Admin bookings</a>
      <a class="nav-link" th:if="${me == null}" th:href="@{/login}" id="nav-login">Login</a>
      <a class="nav-link" th:if="${me == null}" th:href="@{/register}" id="nav-register">Register</a>
```

Then:

```bash
git add src/main/resources/templates/fragments/layout.html
git commit        # completes the merge (message: "Merge branch 'feature/booking-workflow' into develop")
```
