pipeline {
    agent any

    environment {
        PATH = "/opt/homebrew/bin:/usr/local/bin:$PATH"
    }

    stages {
    stage('Checkout') { steps { checkout scm } }

    stage('Build with Gradle') { steps { sh './gradlew --no-daemon clean bootJar' } }

        stage('Verify Docker on Agent') {
            steps {
                sh '''
                    export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"
                    set -eux
                    echo "USER: $(id -un)"
                    echo "HOME: $HOME"
                    echo "PATH: $PATH"
                    which docker || true
                    command -v docker || true
                    docker version || true
                    docker info || true
                '''
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
                        env.DOCKER_IMAGE = "${DOCKER_USER}/springboot-hello:${env.BUILD_NUMBER}"
                        sh '''
                            set -eux
                            export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker build -t "$DOCKER_IMAGE" .
                            docker push "$DOCKER_IMAGE"
                        '''
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh '''
                        set -eux
                        export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"
                        kubectl version --client
                        kubectl apply -f k8s/
                        kubectl set image deployment/springboot-hello springboot-hello=$DOCKER_IMAGE --record
                        kubectl rollout status deployment/springboot-hello --timeout=90s
                    '''
                }
            }
        }
    }

    post {
        success { echo 'Deployment successful!' }
        failure { echo 'Deployment failed.' }
    }
}
