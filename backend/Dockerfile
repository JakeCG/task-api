FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy the jar file
COPY build/libs/task-management-backend.jar app.jar

# Create non-root user
RUN addgroup -g 1000 spring && \
    adduser -u 1000 -G spring -s /bin/sh -D spring

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
