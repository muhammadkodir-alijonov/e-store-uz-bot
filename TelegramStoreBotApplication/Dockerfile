# Use a base image with OpenJDK
FROM openjdk:17-jdk-slim as build

# Set the working directory inside the container
WORKDIR /app

# Install Maven in the build image
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy the Maven build files
COPY pom.xml .

# Download dependencies (this will be cached if unchanged)
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src /app/src

# Build the application (create the JAR file)
RUN mvn clean package -DskipTests

# Use a lightweight base image for the final container
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app will run on
EXPOSE 8080

# Run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
