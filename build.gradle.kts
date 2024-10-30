import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    jacoco // Required by SonarCloud
    `jvm-test-suite`
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.sonarqube") version "4.4.1.3373"
}

repositories {
    mavenCentral()
}

sonar {
    properties {
        property("sonar.projectKey", "tcmpartners_ti-invoices-interface")
        property("sonar.organization", "tcmpartners")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.junit.reportPaths", "${project.projectDir}/test-results/test")
        property("sonar.junit.reportPaths", "**/*Test*/**")
        property("sonar.gradle.skipCompile", true)
    }
}

//<editor-fold desc="Dependencies Versions">
val springbootVersion = "3.3.2"
val camelVersion = "4.7.0"
val lombokVersion = "1.18.32"
val h2Version = "2.2.224"
val postgresVersion = "42.7.1"
val mapstructVersion = "1.6.0.RC1"
val jdbcVersion = "12.7.0.jre8-preview"
val openCsvVersion = "5.9"
val commonsCodecVersion = "1.17.0"
val openApiVersion = "2.3.0"
val azureStorageBlobVersion = "12.27.0"
val woodStoxVersion = "4.4.1"
val openFeignVersion = "4.1.3"
val retryVersion = "2.0.7"
val aspectsVersion = "6.1.10"
val lombokMapstructBindingVersion = "0.2.0"

val junitVersion = "5.11.0-M2"
val testContainersVersion = "1.19.3"
val awaitilityVersion = "4.2.0"
val restAssuredVersion = "5.4.0"
val mockServerClientVersion = "5.15.0"
//</editor-fold>

dependencies {
    // Spring Boot Dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springbootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:$springbootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springbootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springbootVersion")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:$openFeignVersion")
    implementation("org.springframework.retry:spring-retry:$retryVersion")
    implementation("org.springframework:spring-aspects:$aspectsVersion")

    // Apache Camel
    implementation("org.apache.camel.springboot:camel-spring-boot-starter:$camelVersion")
    implementation("org.apache.camel:camel-activemq:$camelVersion")
    implementation("org.apache.camel:camel-ftp:$camelVersion")
    implementation("org.apache.camel:camel-bindy:$camelVersion")
    implementation("org.apache.camel:camel-jackson:$camelVersion")
    implementation("org.apache.camel:camel-jaxb:$camelVersion")

    // Other libraries
    implementation("com.opencsv:opencsv:$openCsvVersion")
    implementation("com.azure:azure-storage-blob:$azureStorageBlobVersion")
    implementation("commons-codec:commons-codec:$commonsCodecVersion") // Cryptography
    implementation("org.codehaus.woodstox:woodstox-core-asl:$woodStoxVersion")
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    implementation("org.projectlombok:lombok:$lombokVersion")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$openApiVersion") // Documentation
    implementation("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

    // Annotation Processors
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Compile and Runtime Dependencies
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    compileOnly("org.springframework.boot:spring-boot-devtools:$springbootVersion")
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:$jdbcVersion")

    // Testing Dependencies
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.apache.camel:camel-test-junit5:$camelVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springbootVersion")
}

group = "com.tcmp"
version = "0.0.1"
description = "InvoicesInterface"
java.sourceCompatibility = JavaVersion.VERSION_17

sourceSets {
    create("intTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }

    systemProperty("spring.profiles.active", "test")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required = true
    }
}

tasks.withType<BootJar> {
    group = "boot"
    description = "This task is used to avoid duped dependencies in docker"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mainClass = "com.tcmp.tiapi.TIInvoicesAPIApplication"
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

// Integration Tests configuration
configurations {
    val intTestImplementation: Configuration by configurations.getting {
        extendsFrom(configurations.implementation.get())
    }

    val intTestRuntimeOnly: Configuration by configurations.getting
    configurations["intTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

    all {
        resolutionStrategy {
            force("com.google.guava:guava:32.0.0-jre")
            force("org.mozilla:rhino:1.7.12")
            force("io.netty:netty-common:4.1.110.Final")
            force("io.netty:netty-handler:4.1.110.Final")
            force("io.netty:netty-handler-proxy:4.1.110.Final")
            force("io.netty:netty-buffer:4.1.110.Final")
            force("io.netty:netty-codec:4.1.110.Final")
            force("io.netty:netty-codec-http:4.1.110.Final")
            force("io.netty:netty-codec-http2:4.1.110.Final")
            force("io.netty:netty-transport-native-unix-common:4.1.110.Final")
            force("io.netty:netty-transport-native-epoll:4.1.110.Final")
            force("io.netty:netty-transport-native-kqueue:4.1.110.Final")
            force("io.netty:netty-tcnative-boringssl-static:2.0.65.Final")
        }
    }

    dependencies {
        intTestImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        intTestImplementation("org.springframework.boot:spring-boot-starter-test:$springbootVersion")

        intTestImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
        intTestImplementation("org.postgresql:postgresql:$postgresVersion") // For test containers
        intTestImplementation("org.awaitility:awaitility:$awaitilityVersion")
        intTestImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
        intTestImplementation("org.testcontainers:testcontainers:$testContainersVersion")
        intTestImplementation("org.testcontainers:postgresql:$testContainersVersion")
        intTestImplementation("org.mock-server:mockserver-client-java:$mockServerClientVersion")
    }

    task<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["intTest"].output.classesDirs
        classpath = sourceSets["intTest"].runtimeClasspath

        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
