# Setup & Run Spring Boot DevOps Project on macOS

## Prerequisites
- Java 17+: `brew install openjdk@17`
- Maven: `brew install maven`
- Docker Desktop: [Download from docker.com]
- kubectl: `brew install kubectl`
- Helm: `brew install helm`
- Jenkins: `brew install jenkins-lts` or use Jenkins server

## Steps
1. **Build Spring Boot App**
   ```sh
   mvn clean package
   ```
2. **Build Docker Image**
   ```sh
   docker build -t springboot-hello .
   ```
3. **Run Locally**
   ```sh
   docker run -p 8080:8080 springboot-hello
   # Test: curl http://localhost:8080/hello
   ```
4. **Kubernetes Deployment**
   ```sh
   kubectl apply -f k8s/
   kubectl get pods
   kubectl get svc
   # Access via NodePort: http://localhost:30080/hello
   ```
5. **Helm Deployment**
   ```sh
   helm install springboot-hello ./helm/springboot-hello
   helm list
   ```
6. **Jenkins Pipeline**
   - Configure Jenkins with Docker and kubectl access
   - Add DockerHub credentials as 'dockerhub-user'
   - Use provided Jenkinsfile for pipeline

## Notes
- All config files are provided in the project.
- For troubleshooting, check logs with `kubectl logs <pod>` and Jenkins build output.
