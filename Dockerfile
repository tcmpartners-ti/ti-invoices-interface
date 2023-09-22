FROM gradle:8.3.0-jdk17 AS build

WORKDIR /app

COPY src/ src/
COPY build.gradle.kts settings.gradle.kts ./

RUN gradle build --no-daemon -x test

FROM amazoncorretto:17.0.8-alpine3.18

WORKDIR /app

COPY --from=build /app/build/libs/invoices-0.0.1.jar invoices.jar
ENV APPLICATION_ENV=dev
ENV PORT=80

CMD ["java", "-jar", "/app/invoices.jar"]
