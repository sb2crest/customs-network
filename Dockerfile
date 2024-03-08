# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Specify a build argument for the JAR file name (default to fdapn-0.0.1-SNAPSHOT.jar)
ARG JAR_FILE=target/fdapn-0.0.1-SNAPSHOT.jar

# Copy the JAR file into the container
COPY ${JAR_FILE} fdapn_service.jar

# Expose the port your application will listen on
EXPOSE 8080

# Define the command to run your application when the container starts
CMD ["java", "-jar", "fdapn_service.jar"]