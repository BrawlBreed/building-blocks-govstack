FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/gateway.jar gateway-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "gateway-0.0.1-SNAPSHOT.jar"]