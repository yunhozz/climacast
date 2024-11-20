FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
WORKDIR /app/config-server
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/config-server/build/libs/config-server-0.0.1-SNAPSHOT.jar config-server.jar
ENTRYPOINT ["java", "-jar", "config-server.jar"]