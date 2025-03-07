# Use official Java 17 image
FROM maven:3.9.6-eclipse-temurin-17

# Set working directory inside container
WORKDIR /app

# Copy pom.xml and download dependencies (cache layer optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy all source files
COPY . .

# Build the Spring Boot JAR file
RUN mvn clean package -DskipTests

# Expose port 8080 for API
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
