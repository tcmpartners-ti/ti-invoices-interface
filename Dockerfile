# Use the official Gradle image as the build environment
FROM gradle:8.3.0-jdk17 AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src/ src/

RUN gradle build --no-daemon

FROM openjdk:17-slim

WORKDIR /app

COPY --from=build /app/build/libs/invoices-0.0.1.jar invoices.jar
ENV APPLICATION_ENV=prod

CMD ["java", "-jar", "/app/invoices.jar"]
