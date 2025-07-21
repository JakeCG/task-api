# Build stage
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew build.gradle ./

RUN chmod +x ./gradlew

RUN ./gradlew dependencies --no-daemon

COPY src/ src/

RUN ./gradlew build --no-daemon -x test -x integration

# Runtime stage
FROM amazoncorretto:21-alpine AS runtime

WORKDIR /app

COPY --from=builder /app/build/libs/task-management-backend.jar app.jar

RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
