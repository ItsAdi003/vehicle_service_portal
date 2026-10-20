// Vehicle Service Booking Portal - declarative CI/CD pipeline.
// Works on Linux (sh) and Windows (bat) Jenkins agents.
// Create the job as "Pipeline script from SCM" so that `checkout scm` knows the repository.

def run(String cmd) {
  if (isUnix()) { sh cmd } else { bat cmd }
}

def runStatus(String cmd) {
  return isUnix() ? sh(script: cmd, returnStatus: true) : bat(script: cmd, returnStatus: true)
}

pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '15'))
    timeout(time: 30, unit: 'MINUTES')
  }

  // Poll GitHub every ~2 minutes; replace by a GitHub webhook when Jenkins is reachable from the internet.
  triggers { pollSCM('H/2 * * * *') }

  // Names must match Manage Jenkins -> Tools (Maven installation "Maven-3.9", JDK installation "JDK-17").
  tools {
    maven 'Maven-3.9'
    jdk 'JDK-17'
  }

  parameters {
    choice(name: 'DEPLOY_ENV', choices: ['dev', 'staging', 'prod'],
           description: 'Target environment. Sets app.environment inside the WAR and the Tomcat context path /vsbp-<env>')
    booleanParam(name: 'DEPLOY_TO_TOMCAT', defaultValue: true,
                 description: 'Copy the WAR into Tomcat webapps and verify /actuator/health')
    string(name: 'TOMCAT_HOME', defaultValue: '/opt/tomcat',
           description: 'Tomcat 10.1 directory on the Jenkins machine (Windows example: C:/tomcat)')
    string(name: 'TOMCAT_URL', defaultValue: 'http://localhost:8081',
           description: 'Base URL of Tomcat (Jenkins itself keeps port 8080)')
  }

  environment {
    APP_NAME  = 'vehicle-service-portal'
    IMAGE_TAG = "1.0.${env.BUILD_NUMBER}"
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
        script { run 'git log -1 --oneline' }
      }
    }

    stage('Build') {
      steps {
        script { run 'mvn -B -ntp clean compile' }
      }
    }

    stage('Unit Tests') {
      steps {
        script { run 'mvn -B -ntp test' }
      }
      post {
        always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
      }
    }

    stage('Package') {
      steps {
        // clean makes sure the filtered application.properties carries the chosen environment name
        script { run "mvn -B -ntp clean package -DskipTests -Dapp.environment=${params.DEPLOY_ENV}" }
      }
      post {
        success { archiveArtifacts artifacts: 'target/*.war', fingerprint: true }
      }
    }

    stage('Selenium UI Tests') {
      steps {
        // A failing test fails this stage, so the deploy stages below never run.
        script { run 'mvn -B -ntp -Dselenium.headless=true test-compile failsafe:integration-test failsafe:verify' }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'target/failsafe-reports/*.xml'
          archiveArtifacts artifacts: 'target/screenshots/*', allowEmptyArchive: true
        }
      }
    }

    stage('Deploy to Tomcat') {
      when { expression { params.DEPLOY_TO_TOMCAT } }
      steps {
        script {
          def target = "${params.TOMCAT_HOME}/webapps/vsbp-${params.DEPLOY_ENV}.war"
          if (isUnix()) {
            sh "cp target/vehicle-service-portal.war '${target}'"
          } else {
            def winTarget = target.replace('/', '\\')
            bat "copy /Y target\\vehicle-service-portal.war \"${winTarget}\""
          }
          echo "Deployed to ${params.TOMCAT_URL}/vsbp-${params.DEPLOY_ENV}/"
        }
      }
    }

    stage('Verify Tomcat Deployment') {
      when { expression { params.DEPLOY_TO_TOMCAT } }
      steps {
        script {
          def healthUrl = "${params.TOMCAT_URL}/vsbp-${params.DEPLOY_ENV}/actuator/health"
          retry(12) {            // Tomcat needs a few seconds to unpack and start the application
            sleep time: 5, unit: 'SECONDS'
            run "curl --fail --silent --show-error ${healthUrl}"
          }
        }
      }
    }

  }

  post {
    success { echo "Build #${env.BUILD_NUMBER} succeeded." }
    failure { echo "Build #${env.BUILD_NUMBER} failed - nothing was deployed after the failing stage." }
  }
}
