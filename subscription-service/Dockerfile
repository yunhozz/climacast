FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
WORKDIR /app/subscription-service
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/subscription-service/build/libs/subscription-service-0.0.1-SNAPSHOT.jar subscription-service.jar
ENTRYPOINT ["java", "-jar", "subscription-service.jar"]