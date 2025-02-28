FROM openjdk:17-jdk-slim

COPY /target/website-*.jar /website.jar

# Expose the port the app will run on
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "/website.jar"]