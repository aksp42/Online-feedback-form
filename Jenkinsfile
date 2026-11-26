// Jenkins Pipeline for the Online Feedback Form project
//
// Before running, configure Maven in Jenkins once:
//   Manage Jenkins -> Tools -> Maven installations -> Add Maven
//   Name: Maven   (exactly this name)   -> tick "Install automatically" -> Save
//
// Works on Windows Jenkins (uses "bat") and Linux Jenkins (uses "sh").

pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                // Gets the code from the GitHub repository configured in the job
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B clean package'
                    } else {
                        bat 'mvn -B clean package'
                    }
                }
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/online-feedback-form.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            // Shows test results (graph + list) on the Jenkins build page
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
        success {
            echo 'BUILD SUCCESSFUL: target/online-feedback-form.jar created and all tests passed.'
        }
        failure {
            echo 'BUILD FAILED: check the Console Output above for the error.'
        }
    }
}
