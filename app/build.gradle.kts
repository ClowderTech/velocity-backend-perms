plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.clowdertech"

version = "1.1.7"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "velocity-snapshots"
        url = uri("https://nexus.velocitypowered.com/repository/velocity-snapshots/")
    }
    maven {
        name = "mojang-libs"
        url = uri("https://libraries.minecraft.net")
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.1.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:4.1.0-SNAPSHOT")
    compileOnly("net.luckperms:api:5.5")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    compileOnly("com.mojang:brigadier:1.0.18")
    implementation("org.reflections:reflections:0.10.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<Test> { useJUnitPlatform() }

tasks {
    // Configure the shadowJar task:
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("velocity-backend-perms")
        archiveVersion.set("")
        archiveClassifier.set("")
        mergeServiceFiles() // merges META‑INF/services if needed
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes["Main-Class"] = "com.clowdertech.velocitybackendperms.Main" }
    }
}

tasks.named("build") { dependsOn("shadowJar") }
