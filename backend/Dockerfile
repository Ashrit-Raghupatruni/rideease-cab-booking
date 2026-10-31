# Build stage using Maven and Eclipse Temurin JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage using lightweight alpine JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy packaged fat JAR
COPY --from=build /app/target/rideease-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose multi-port isolation connectors:
# 8080: Passenger Web Portal & API
# 8081: Driver Partner Terminal & Telemetry
# 8082: Administrator Command Center
EXPOSE 8080 8081 8082

# JVM configuration for container environments
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
