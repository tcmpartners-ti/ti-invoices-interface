plugins {
  `java-library`
  `maven-publish`
  id("org.springframework.boot") version "3.1.3"
}

repositories {
  mavenLocal()
  maven {
    url = uri("https://repo.maven.apache.org/maven2/")
  }
}

dependencies {
  api("org.springframework.boot:spring-boot-starter-web:3.1.3")
  api("org.springframework.boot:spring-boot-starter-validation:3.1.3")
  api("org.apache.camel.springboot:camel-spring-boot-starter:4.0.0")
  api("org.apache.camel:camel-activemq:4.0.0")
  api("org.apache.camel:camel-jackson:4.0.0")
  api("org.apache.camel:camel-jaxb:4.0.0")
  api("org.apache.camel:camel-bindy:4.0.0")
  api("org.codehaus.woodstox:woodstox-core-asl:4.4.1")
  api("org.mapstruct:mapstruct:1.5.5.Final")
  api("org.springframework.cloud:spring-cloud-starter-openfeign:4.0.3")
  api("org.springframework.boot:spring-boot-starter-data-jpa:3.1.2")
  api("org.projectlombok:lombok:1.18.28")
  api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")

  annotationProcessor("org.projectlombok:lombok:1.18.28")
  annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

  compileOnly("org.projectlombok:lombok:1.18.28")
  runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:11.2.3.jre17")
  runtimeOnly("org.springframework.boot:spring-boot-devtools:3.1.2")
  testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.2")
  testImplementation("com.h2database:h2:2.2.220")
  testImplementation("org.apache.camel:camel-test-junit5:4.0.0")
  testImplementation("junit:junit:4.13.2")
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

tasks.withType<JavaCompile> {
  options.encoding = "UTF-8"
  options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc> {
  options.encoding = "UTF-8"
}
