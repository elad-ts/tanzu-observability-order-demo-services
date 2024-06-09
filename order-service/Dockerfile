# Use Maven with Eclipse Temurin 17 (Java 17) for building the application
FROM docker.io/maven:3-eclipse-temurin-17 AS build

# Set the working directory and copy the application files
WORKDIR /usr/src/mymaven
COPY . .

# Build the application
RUN mvn clean package

# Use Eclipse Temurin 17 JRE for running the application
FROM docker.io/eclipse-temurin:17-jre

# Install unzip and bash
RUN apt-get update && apt-get install -y unzip bash && rm -rf /var/lib/apt/lists/*

# Copy the built application from the build stage
COPY --from=build /usr/src/mymaven/target /target

# Set the working directory
WORKDIR /target

# Command to run the application
CMD ["java", "-jar", "order-service-1.0-SNAPSHOT.jar"]
