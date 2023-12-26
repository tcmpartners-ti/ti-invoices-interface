val springbootVersion = "3.2.0"
val camelVersion = "4.0.0"
val lombokVersion = "1.18.28"
val h2Version = "2.2.220"
val mapstructVersion = "1.5.5.Final"
val jdbcVersion = "12.2.0.jre11"
val junitVersion = "5.10.1"

plugins {
    `java-library`
    `maven-publish`
    id("org.springframework.boot") version "3.2.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web:$springbootVersion")
    api("org.springframework.boot:spring-boot-starter-validation:$springbootVersion")
    api("org.apache.camel.springboot:camel-spring-boot-starter:$camelVersion")
    api("org.apache.camel:camel-activemq:$camelVersion")
    api("org.apache.camel:camel-jackson:$camelVersion")
    api("org.apache.camel:camel-jaxb:$camelVersion")
    api("org.apache.camel:camel-bindy:$camelVersion")
    api("com.opencsv:opencsv:5.9")
    api("commons-codec:commons-codec:1.16.0") // For crypto stuff
    api("org.codehaus.woodstox:woodstox-core-asl:4.4.1")
    api("org.mapstruct:mapstruct:$mapstructVersion")
    api("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.0")
    // Retries
    api("org.springframework.retry:spring-retry:2.0.3")
    api("org.springframework:spring-aspects:6.0.11")

    api("org.springframework.boot:spring-boot-starter-data-jpa:$springbootVersion")
    api("org.projectlombok:lombok:$lombokVersion")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    api("org.springframework.boot:spring-boot-starter-data-redis:3.2.0")

    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    compileOnly("org.springframework.boot:spring-boot-devtools:$springbootVersion")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:$jdbcVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springbootVersion")
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.apache.camel:camel-test-junit5:$camelVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
}

group = "com.tcmp"
version = "0.0.1"
description = "InvoicesInterface"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }

    systemProperty("spring.profiles.active", "test")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
