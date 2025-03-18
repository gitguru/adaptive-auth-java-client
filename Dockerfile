FROM gradle:jdk21

WORKDIR /usr/src/app

COPY * .
COPY gradle ./gradle
COPY src ./src

EXPOSE 8081

RUN ./gradlew build --stacktrace

# ENTRYPOING ["gradlew" "build"]
