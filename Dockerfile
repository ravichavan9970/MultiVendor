# Multi-stage Docker build for MultiVendor Fullstack Spring Boot Application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENV PORT=8081
ENTRYPOINT ["java", "-jar", "app.jar"]
