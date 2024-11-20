FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
WORKDIR /app/eureka-server
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/eureka-server/build/libs/eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]