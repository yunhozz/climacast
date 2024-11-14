FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
WORKDIR /app/api-gateway
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]