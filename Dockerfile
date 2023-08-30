# Use a Maven image for building
FROM maven:3.8.4-openjdk-17-slim AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests


FROM openjdk:17-slim

WORKDIR /app

COPY --from=build /app/target/invoices-0.0.1.jar /app/invoices.jar

ENV APPLICATION_ENV=prod

CMD ["java", "-jar", "/app/invoices.jar"]
