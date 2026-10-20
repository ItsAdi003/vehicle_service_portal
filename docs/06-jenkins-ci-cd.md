# 6. Jenkins: continuous integration and pipeline as code (Tasks 8, 9, 11, 13)

Ports used on one machine: **Jenkins 8080**, **Tomcat 8081**, **Docker container 9090**, local `mvn spring-boot:run` 8080 (stop Jenkins first or use `-Dserver.port=8082`).

## 6.1 Install and configure Jenkins (Task 8)

1. Install **JDK 17** and **Git** first.
2. Install Jenkins (LTS):
   - *Windows:* download the LTS `.msi` from jenkins.io, run it, accept port **8080**, run as local system or your user.
   - *Linux:* follow the Debian/Ubuntu or RedHat package instructions on jenkins.io.
   - *Docker (any OS):* `docker run -d --name jenkins -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts-jdk17`
3. Open <http://localhost:8080>, enter the initial admin password (from the console / `secrets/initialAdminPassword`), choose **Install suggested plugins**, create the admin user.
4. **Manage Jenkins -> Plugins -> Available**: make sure these are installed: *Git*, *Pipeline*, *JUnit*, *Timestamper*, *Credentials Binding*, and (for Task 13) *Docker Pipeline*.
5. **Manage Jenkins -> Tools**:
   - JDK installations -> Add JDK -> name **`JDK-17`** (tick *Install automatically* or give `JAVA_HOME`)
   - Maven installations -> Add Maven -> name **`Maven-3.9`**, version 3.9.x (install automatically)
   - Git installations -> default is fine.

The names `JDK-17` and `Maven-3.9` are referenced by the `Jenkinsfile`.

## 6.2 Freestyle CI job with polling and archived artefact (Task 8)

1. **New Item** -> name `vsbp-ci-build` -> **Freestyle project**.
2. **Source Code Management -> Git**: Repository URL `https://github.com/<you>/vehicle-service-portal.git` (add credentials if private), branch `*/develop`.
3. **Build Triggers -> Poll SCM**: schedule `H/2 * * * *` (poll every ~2 minutes). *Alternative:* GitHub hook trigger with a webhook if Jenkins is internet-reachable.
4. **General**: choose JDK `JDK-17` (if the dropdown is shown).
5. **Build Steps -> Invoke top-level Maven targets**: Maven Version `Maven-3.9`, goals `clean package -DskipTests`.
6. **Post-build Actions -> Archive the artifacts**: files `target/*.war`. Add *Publish JUnit test result report* with `target/surefire-reports/*.xml` if you run `clean package` without `-DskipTests`.
7. **Save**, click **Build Now**, then push a small commit and wait for the poll to start a build automatically.

Evidence for Task 8: job configuration page, *Console Output* ending in `BUILD SUCCESS` and `Finished: SUCCESS`, the *Polling Log* / build triggered by *"Started by an SCM change"*, build page showing the archived `vehicle-service-portal.war`.

## 6.3 Pipeline as code and deployment to Tomcat (Task 9)

### Prepare Tomcat 10.1
1. Unzip **Apache Tomcat 10.1.x**, e.g. to `C:\tomcat` or `/opt/tomcat`.
2. In `conf/server.xml` change `<Connector port="8080" ...>` to **8081**.
3. Make sure the account that runs Jenkins can write to `<tomcat>/webapps`.
4. Start Tomcat (`bin/startup.sh` / `bin\startup.bat`) and check <http://localhost:8081>.

### Create the pipeline job
1. **New Item** -> `vsbp-pipeline` -> **Pipeline**.
2. **Build Triggers -> Poll SCM**: `H/2 * * * *` (the `Jenkinsfile` also declares it).
3. **Pipeline -> Definition: Pipeline script from SCM** -> SCM Git -> repository URL -> branch `*/develop` -> Script Path `Jenkinsfile`.
4. Save and **Build Now** once (this registers the parameters), then use **Build with Parameters**.

### Stages in the `Jenkinsfile`
| Stage | What it does |
|-------|--------------|
| Checkout | `checkout scm`, prints the commit |
| Build | `mvn clean compile` |
| Unit Tests | `mvn test`, publishes JUnit results |
| Package | `mvn clean package -DskipTests -Dapp.environment=<DEPLOY_ENV>`, archives the WAR |
| Deploy to Tomcat | Copies the WAR to `<TOMCAT_HOME>/webapps/vsbp-<DEPLOY_ENV>.war` |
| Verify Tomcat Deployment | Retries `curl --fail <TOMCAT_URL>/vsbp-<env>/actuator/health` until it answers |

### Parameters (configuration evidence)
| Parameter | Default | Effect |
|-----------|---------|--------|
| `DEPLOY_ENV` | `dev` | Baked into the WAR as `app.environment` (shown in the page footer), selects the context path `/vsbp-<env>` and the H2 database file name |
| `DEPLOY_TO_TOMCAT` | true | Skip the deploy stages when false |
| `TOMCAT_HOME` | `/opt/tomcat` | **Set to your Tomcat folder**, e.g. `C:/tomcat` |
| `TOMCAT_URL` | `http://localhost:8081` | Used by the health check |

Run once with `DEPLOY_ENV=dev` and once with `staging`: two applications appear side by side at
`http://localhost:8081/vsbp-dev/` and `http://localhost:8081/vsbp-staging/`, each showing its own environment name
in the footer. That is the proof of the parameterised environment setting.

Evidence for Task 9: the `Jenkinsfile`, *Stage View* with all stages green, console log of the deploy stage, browser
screenshot of `http://localhost:8081/vsbp-dev/` with the footer visible, *Build with Parameters* screenshot.

## 6.4 Troubleshooting
| Symptom | Cause / fix |
|---------|-------------|
| `Tool type "maven" does not have an install of "Maven-3.9"` | Tool name in Manage Jenkins -> Tools differs from the Jenkinsfile |
| `checkout scm` fails: *scm* undefined | Job is not "Pipeline script from SCM" |
| Deploy copies but Tomcat shows 404 / app does not start | Tomcat 9 used (needs 10.1), or Java < 17 on Tomcat |
| Health check retries then fails | Wrong `TOMCAT_URL` / port, or the Tomcat log shows a startup error |
| `Access is denied` on copy (Windows) | Jenkins service account has no write permission on `webapps` |

## 6.5 Continuous testing: Selenium in Jenkins (Task 11)

### Requirements on the Jenkins machine
Google Chrome must be installed on the machine (agent) that runs the build. The tests run headless
(`-Dselenium.headless=true`). If Chrome is installed in a non-standard place, add `-Dchrome.binary=<path>` to the Maven
command in the *Selenium UI Tests* stage.

### The stage
```groovy
stage('Selenium UI Tests') {
  steps { script { run 'mvn -B -ntp -Dselenium.headless=true test-compile failsafe:integration-test failsafe:verify' } }
  post { always {
      junit allowEmptyResults: true, testResults: 'target/failsafe-reports/*.xml'
      archiveArtifacts artifacts: 'target/screenshots/*', allowEmptyArchive: true } }
}
```
* `failsafe:integration-test` runs the `*IT.java` classes and always writes the reports.
* `failsafe:verify` fails the build when any test failed. A failed stage means Jenkins never reaches **Deploy to Tomcat**.
* `post { always { ... } }` publishes the *Test Result* trend and the failure screenshots even for a red build.

### Evidence checklist
1. Green run: Stage View with *Selenium UI Tests* green; *Test Result* page (10 Selenium tests + unit tests).
2. Introduce the defect with `docs/defect-demo/cancel-seat-defect.patch` (see `docs/defect-demo/README.md`): red run,
   deploy stages skipped, screenshot archived.
3. Fix commit and green re-run.
