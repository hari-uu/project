pipeline {
    agent any

    environment {
        // Optional: Set non-secret environment variables as needed
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
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-user',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    script {
                        def dockerImage = "${DOCKER_USER}/springboot-hello:${env.BUILD_NUMBER}"
                        sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                        sh "docker build -t $dockerImage ."
                        sh "docker push $dockerImage"
                        // Store image tag for next stage
                        env.DOCKER_IMAGE = dockerImage
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def image = env.DOCKER_IMAGE
                    sh "kubectl set image deployment/springboot-hello springboot-hello=${image} --record || true"
                    sh "kubectl rollout status deployment/springboot-hello || kubectl apply -f k8s/"
                }
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
