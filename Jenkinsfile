pipeline {
    agent any
    environment {
        DOCKER_USER = credentials('dockerhub-user')
        DOCKER_IMAGE = "$DOCKER_USER/springboot-hello:${env.BUILD_NUMBER}"
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
                sh "docker build -t $DOCKER_IMAGE ."
                sh "docker push $DOCKER_IMAGE"
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl set image deployment/springboot-hello springboot-hello=$DOCKER_IMAGE --record || kubectl apply -f k8s/'
            }
        }
    }
    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}
