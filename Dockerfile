# Dockerfile for learning-center-platform
# Summary:
# This Dockerfile builds and run the learning-center-platform application using Maven and OpenJDK 27.
# Description:
# This Dockerfile is designed to build a Spring Boot application using Maven and run it in a lightweight
# OpenJDK 27 environment. It uses a multi-stage build to keep the final image size small by separating the build
# and runtime environments. It sets the active Spring profile to 'prod' for production use and exposes port 8080,
# which is the default port for Spring Boot applications.
# Version: 1.0
# Maintainer: Open-source Development Team

# Step 1: Build the application using Maven

# Use a Maven image with Temurin JDK 27 for builds
FROM maven:3.10.0-eclipse-temurin-27 AS build
# Set the working directory inside the container
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
# Copy the Maven project files into the container
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

# Step 2: Create a runtime image
# Copy the Spring Boot JAR file into the container
FROM eclipse-temurin:27-jre AS runtime
ENV SPRING_PROFILES_ACTIVE=prod
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Step 3: Configure and run the application
# Expose the port your Spring Boot application listens on (default is 8080)
EXPOSE 8080
# Define the command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Note: The application will run with the 'prod' profile as set in the runtime stage.
# This Dockerfile is designed to be used in a CI/CD pipeline or for local development.
# It is necessary to define the following environment variables in the hosting provider for the application to
# run correctly in the Production environment:
# - DATABASE_NAME: The name of the database to connect to.
# - DATABASE_USER: The username for the database connection.
# - DATABASE_PASSWORD: The password for the database connection.
# - DATABASE_URL: The database host name or address.
# - DATABASE_PORT: The port of the database to connect to.
# - JWT_SECRET: Secret used to sign JWT tokens.
# - PORT: The port on which the application will run (default is 8080).
# - SPRING_PROFILES_ACTIVE: The active Spring profile (Must be 'prod' to use the runtime configuration).