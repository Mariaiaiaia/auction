FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y netcat

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/UserService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

