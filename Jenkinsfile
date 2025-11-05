pipeline {
    agent any

    environment {
        PATH = "/opt/homebrew/bin:/usr/local/bin:$PATH"
    }

    parameters {
        string(name: 'IMAGE_TAG', defaultValue: '', description: 'Optional image tag to deploy (defaults to BUILD_NUMBER).')
        booleanParam(name: 'ENABLE_GIT_TAG', defaultValue: false, description: 'Create and push git tag v$BUILD_NUMBER')
        booleanParam(name: 'USE_TUNNEL', defaultValue: false, description: 'Try to run `minikube tunnel` to provision external IPs (requires sudo NOPASSWD).')
    }

    stages {
        stage('Checkout') { steps { checkout scm } }

        stage('Build with Gradle') { steps { sh './gradlew --no-daemon clean bootJar' } }

        stage('Verify Tools') {
            steps {
                sh '''
                    export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"
                    set -e
                    docker --version
                    kubectl version --client
                    kubectl config current-context || true
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
                        def imageTag = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : env.BUILD_NUMBER
                        env.DOCKER_IMAGE = "${DOCKER_USER}/springboot-hello:${imageTag}"
                        sh '''
                            set -e
                            export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker build -t "$DOCKER_IMAGE" .
                            docker push "$DOCKER_IMAGE"
                        '''
                    }
                }
            }
        }

        stage('Tag Source in Git') {
            when { expression { return params.ENABLE_GIT_TAG } }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    withCredentials([usernamePassword(
                        credentialsId: 'github-pat',
                        usernameVariable: 'GITHUB_USER',
                        passwordVariable: 'GITHUB_TOKEN'
                    )]) {
                        sh '''
                            set -e
                            git config user.name "$GITHUB_USER"
                            git config user.email "$GITHUB_USER@users.noreply.github.com"
                            git tag -a "v$BUILD_NUMBER" -m "CI tag build $BUILD_NUMBER"
                            git push "https://$GITHUB_USER:$GITHUB_TOKEN@github.com/hari-uu/project.git" "v$BUILD_NUMBER"
                        '''
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh '''
                                                set -e
                        export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"
                                                kubectl version --client
                                                # Apply core resources first (avoid ingress webhook failures breaking the build)
                                                kubectl apply -f k8s/deployment.yaml
                                                                                                kubectl apply -f k8s/service.yaml
                                                # Update image and wait for rollout
                                                kubectl set image deployment/springboot-hello springboot-hello=$DOCKER_IMAGE --record
                                                kubectl rollout status deployment/springboot-hello --timeout=90s
                                                # Apply ingress if controller is present; do not fail the build if webhook isn't ready
                                                if [ -f k8s/ingress.yaml ]; then
                                                    if kubectl get ns ingress-nginx >/dev/null 2>&1; then
                                                        kubectl -n ingress-nginx rollout status deploy/ingress-nginx-controller --timeout=60s || true
                                                    fi
                                                    kubectl apply -f k8s/ingress.yaml || echo "Ingress apply skipped (controller not ready)."
                                                fi
                                                # Quick visibility
                                                kubectl get deploy/springboot-hello
                                                kubectl get pods -l app=springboot-hello -o wide || true
                                                kubectl get svc springboot-hello-service || true
                                                kubectl get ingress springboot-hello-ingress || true

                                                                                                # Attempt to discover External IPs
                                                                                                echo "Waiting for Service external IP (LoadBalancer) ..."
                                                                                                for i in $(seq 1 30); do
                                                                                                    EXTIP=$(kubectl get svc springboot-hello-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
                                                                                                    [ -n "$EXTIP" ] && break || sleep 2
                                                                                                done
                                                                                                if [ -z "$EXTIP" ] && [ "${USE_TUNNEL}" = "true" ]; then
                                                                                                    echo "No external IP yet; attempting to start minikube tunnel (may require sudo NOPASSWD)..."
                                                                                                    if command -v sudo >/dev/null 2>&1; then
                                                                                                        sudo -n nohup minikube tunnel >/tmp/minikube-tunnel.log 2>&1 &
                                                                                                    else
                                                                                                        nohup minikube tunnel >/tmp/minikube-tunnel.log 2>&1 &
                                                                                                    fi
                                                                                                    sleep 5
                                                                                                    for i in $(seq 1 30); do
                                                                                                        EXTIP=$(kubectl get svc springboot-hello-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
                                                                                                        [ -n "$EXTIP" ] && break || sleep 2
                                                                                                    done
                                                                                                fi
                                                                                                [ -n "$EXTIP" ] && echo "Service External IP: $EXTIP" || echo "Service External IP not assigned. If needed, run: minikube tunnel"

                                                                                                # Ingress controller external IP (if LoadBalancer)
                                                                                                INGCIP=$(kubectl -n ingress-nginx get svc ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
                                                                                                if [ -n "$INGCIP" ]; then
                                                                                                    echo "Ingress Controller External IP: $INGCIP"
                                                                                                    echo "Ingress Host: http://hello-project.com/hello (map DNS/hosts to $INGCIP)"
                                                                                                else
                                                                                                    echo "Ingress Controller External IP not assigned. Use: minikube tunnel, then map hello-project.com to the tunnel IP."
                                                                                                fi
                                                
                                                                                                echo "Direct service test (if EXTIP present): curl http://$EXTIP:8080/hello"
                                                                                                echo "Local fallback: kubectl port-forward svc/springboot-hello-service 8080:8080 && curl http://localhost:8080/hello"
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
