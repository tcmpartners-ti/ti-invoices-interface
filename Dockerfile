# Builder Stage
FROM gradle:8.8.0-jdk17-alpine AS build

WORKDIR /app

COPY src/ src/
COPY build.gradle.kts settings.gradle.kts ./

RUN --mount=type=cache,target=$GRADLE_USER_HOME/caches \
    gradle --no-daemon assemble

# Runtime Stage
FROM amazoncorretto:17.0.10-alpine3.16

ARG COMMIT
ENV COMMIT=${COMMIT}

RUN addgroup -S invoices-group && adduser -S invoices -G invoices-group

WORKDIR /app
COPY --from=build /app/build/libs/invoices-0.0.1.jar invoices.jar

EXPOSE 80

RUN mkdir -p \
    /app/tmp/full-output \
    /app/tmp/summary \
    /app/tmp/reports
RUN chown -R invoices:invoices-group /app/tmp

USER invoices
CMD ["java", "-jar", "/app/invoices.jar"]
