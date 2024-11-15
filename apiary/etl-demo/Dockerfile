# Use an official OpenJDK runtime as the base image
FROM openjdk:11-jre-slim

# Set working directory inside the container
WORKDIR /app

# Copy the Spring Boot jar file into the container
COPY target/website-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port that Spring Boot listens to
EXPOSE 8081

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]