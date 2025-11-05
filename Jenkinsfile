pipeline {
    agent any

    environment {
        DOCKER_CREDS = credentials('dockerhub-user')  // Jenkins credential ID
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build with Gradle') {
            steps {
                sh './gradlew bootJar'
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    DOCKER_IMAGE = "${DOCKER_CREDS_USR}/springboot-hello:${BUILD_NUMBER}"
                }
                sh "echo $DOCKER_CREDS_PSW | docker login -u $DOCKER_CREDS_USR --password-stdin"
                sh "docker build -t $DOCKER_IMAGE ."
                sh "docker push $DOCKER_IMAGE"
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    DOCKER_IMAGE = "${DOCKER_CREDS_USR}/springboot-hello:${BUILD_NUMBER}"
                }
                sh "kubectl set image deployment/springboot-hello springboot-hello=$DOCKER_IMAGE --record || true"
                sh "kubectl rollout status deployment/springboot-hello || kubectl apply -f k8s/"
            }
        }
    }

    post {
        success {
            echo '✅ Deployment successful!'
        }
        failure {
            echo '❌ Deployment failed.'
        }
    }
}
