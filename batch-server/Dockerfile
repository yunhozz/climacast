FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
WORKDIR /app/batch-server
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/batch-server/build/libs/batch-server-0.0.1-SNAPSHOT.jar batch-server.jar
ENTRYPOINT ["java", "-jar", "batch-server.jar"]