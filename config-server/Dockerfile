FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/config-server.jar config-server-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "config-server-0.0.1-SNAPSHOT.jar"]