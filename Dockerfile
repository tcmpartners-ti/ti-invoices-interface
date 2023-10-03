FROM gradle:8.3.0-jdk17 AS build

WORKDIR /app

COPY src/ src/
COPY build.gradle.kts settings.gradle.kts ./

RUN gradle --no-daemon assemble

FROM amazoncorretto:17.0.8-alpine3.18

RUN addgroup -S invoices-group && adduser -S invoices -G invoices-group

WORKDIR /app

COPY --from=build /app/build/libs/invoices-0.0.1.jar invoices.jar

EXPOSE 80

USER invoices
CMD ["java", "-jar", "/app/invoices.jar"]
