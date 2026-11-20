# Dockerfile Multi-stage para Frontend Ejercicio5Cliente en Java 21 LTS
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /app/target/*.jar app.jar

ENV PORT=9080
ENV API_BACKEND_URL=http://localhost:8081

EXPOSE 9080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
