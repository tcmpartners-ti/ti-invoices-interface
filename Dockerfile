# Builder Stage
FROM gradle:8.5.0-jdk17-alpine AS build

WORKDIR /app

COPY src/ src/
COPY build.gradle.kts settings.gradle.kts ./
RUN  echo hello

RUN gradle --no-daemon assemble

# Runtime Stage
FROM amazoncorretto:17.0.9-alpine3.18

RUN addgroup -S invoices-group && adduser -S invoices -G invoices-group

WORKDIR /app

COPY --from=build /app/build/libs/invoices-0.0.1.jar invoices.jar

EXPOSE 80

USER invoices
CMD ["java", "-jar", "/app/invoices.jar"]
