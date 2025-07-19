pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.5'
        jdk 'JDK-17'
    }
    
    environment {
        DOCKER_REGISTRY = 'localhost:5000'
        APP_NAME = 'football-standings'
        VERSION = "${BUILD_NUMBER}"
        SONAR_HOST_URL = 'http://sonarqube:9000'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    env.GIT_COMMIT = gitCommit.substring(0, 7)
                }
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building application...'
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/TEST-*.xml'
                    publishCoverage adapters: [
                        jacocoAdapter('target/site/jacoco/jacoco.xml')
                    ], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                }
            }
        }
        
        stage('Code Quality Analysis') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=football-standings \
                        -Dsonar.projectName="Football Standings Microservice" \
                        -Dsonar.host.url=${SONAR_HOST_URL}
                    '''
                }
            }
        }
        
        stage('Quality Gate') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging application...'
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                script {
                    def dockerImage = docker.build("${DOCKER_REGISTRY}/${APP_NAME}:${VERSION}")
                    dockerImage.push()
                    dockerImage.push('latest')
                }
            }
        }
        
        stage('Security Scan') {
            parallel {
                stage('Dependency Check') {
                    steps {
                        echo 'Running dependency security check...'
                        sh '''
                            mvn org.owasp:dependency-check-maven:check \
                            -DfailBuildOnCVSS=8 \
                            -Dformat=XML
                        '''
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'Dependency Check Report'
                            ])
                        }
                    }
                }
                
                stage('Container Scan') {
                    steps {
                        echo 'Scanning Docker image for vulnerabilities...'
                        sh """
                            docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                            -v \$(pwd):/tmp/.cache/ aquasec/trivy:latest image \
                            --exit-code 0 --severity HIGH,CRITICAL \
                            ${DOCKER_REGISTRY}/${APP_NAME}:${VERSION}
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging environment...'
                sh """
                    docker-compose -f docker-compose.staging.yml down
                    docker-compose -f docker-compose.staging.yml up -d
                """
            }
        }
        
        stage('Integration Tests') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Running integration tests...'
                sleep(time: 30, unit: 'SECONDS') // Wait for service to be ready
                sh 'mvn failsafe:integration-test failsafe:verify -Dtest.env=staging'
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                echo 'Deploying to production environment...'
                sh """
                    docker-compose -f docker-compose.prod.yml down
                    docker-compose -f docker-compose.prod.yml up -d
                """
            }
        }
        
        stage('Production Health Check') {
            when {
                branch 'main'
            }
            steps {
                echo 'Performing production health check...'
                script {
                    retry(5) {
                        sleep(10)
                        sh 'curl -f http://localhost:8080/api/actuator/health'
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
            slackSend(
                color: 'good',
                message: "✅ Pipeline succeeded for ${APP_NAME} - Build ${BUILD_NUMBER}"
            )
        }
        failure {
            echo 'Pipeline failed!'
            slackSend(
                color: 'danger',
                message: "❌ Pipeline failed for ${APP_NAME} - Build ${BUILD_NUMBER}"
            )
        }
    }
}