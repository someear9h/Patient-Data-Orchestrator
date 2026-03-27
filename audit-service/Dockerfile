# ----------------- Builder Stage -----------------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and Protobuf schemas
COPY src ./src

# Build the jar (this will also compile the Protobuf files inside the Linux container)
RUN mvn clean package -DskipTests

# ----------------- Runner Stage ------------------
FROM eclipse-temurin:21-jdk AS runner

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/audit-service-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the port we defined in application.yml
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]