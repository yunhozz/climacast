FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
WORKDIR /app/ai-service
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/ai-service/build/libs/ai-service-0.0.1-SNAPSHOT.jar ai-service.jar
ENTRYPOINT ["java", "-jar", "ai-service.jar"]