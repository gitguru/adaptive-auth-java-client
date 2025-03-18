# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.apidynamics.test.client-demo' is invalid and this project uses 'com.apidynamics.test.client_demo' instead.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.2/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.2/gradle-plugin/packaging-oci-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.2/reference/web/servlet.html)
* [Thymeleaf](https://docs.spring.io/spring-boot/3.4.2/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.4.2/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

## Docker
### Creating the image
```
docker build -t client-demo .
```

### Running the development container
```
docker run -it -p 8081:8081 --name client-demo-dev --rm --volume $(pwd):/usr/src/app client-demo:latest bash
```

## Building gradle app
```
./gradlew build
```

## Running built app
```
java -jar build/libs/client-demo-0.0.1-SNAPSHOT.jar 
```

## Running app as a Spring Boot application
```
./gradlew bootRun
```

## Login to AWS ECR
```
aws --profile {PROFILE} ecr get-login-password --regionn {REGION} | docker login --username AWS --password-stdin {ECR_URI}
```

# Build and push (demo-server) docker image to ECR
```
export DOCKER_DEFAULT_PLATFORM=linux/amd64
docker build --pull --no-cache --platform="linux/amd64" --provenance=false -t client-demo:latest -f Dockerfile.k8s .
docker tag client-demo:latest 071215964715.dkr.ecr.us-west-2.amazonaws.com/shs-common-backend:java_client_demo_011
docker push 071215964715.dkr.ecr.us-west-2.amazonaws.com/shs-common-backend:java_client_demo_011
```
