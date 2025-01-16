FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/shopapp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]