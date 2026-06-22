# --- Step 1: Build the Application jar ---
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Build the jar file skipping test cycles for deployment speed
RUN mvn clean package -DskipTests

# --- Step 2: Run the Application jar ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render assigns a dynamic port via the PORT environment variable. 
# Spring Boot automatically picks up this variable.
EXPOSE 8082

ENTRYPOINT ["java", "-Dserver.port=${PORT:-8082}", "-jar", "app.jar"]
