# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy pom.xml first so Docker can cache dependencies separately
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the final JAR from the build stage
COPY --from=build /app/target/complaint-app.jar app.jar

# Expose the port Render injects via $PORT
EXPOSE 10000

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
