FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/discovery.jar discovery-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "discovery-0.0.1-SNAPSHOT.jar"]