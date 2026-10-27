# Build stage using Maven and Eclipse Temurin JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/rideease-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose primary port (8080) and secondary tomcat connector ports (8081, 8082)
EXPOSE 8080 8081 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
