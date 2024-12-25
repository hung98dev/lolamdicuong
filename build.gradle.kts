plugins {
    id("java")
    id("application")
}

group = "com.game"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Thêm Netty Library
    implementation("io.netty:netty-all:4.1.116.Final")

    // JDBC Driver cho PostgreSQL
    implementation("org.postgresql:postgresql:42.7.4")

    // slf4j Để ghi log
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    //
    implementation("com.google.guava:guava:33.4.0-jre")

    //
    implementation("com.zaxxer:HikariCP:6.2.1")

    //
    implementation("io.undertow:undertow-core:2.3.18.Final")
}

application {
    mainClass = "com.game"
}

tasks.test {
    useJUnitPlatform()
}