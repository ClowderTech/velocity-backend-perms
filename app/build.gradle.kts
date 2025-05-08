plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.clowdertech"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url  = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "velocity-snapshots"
        url  = uri("https://nexus.velocitypowered.com/repository/velocity-snapshots/")
    }
    maven {
        name = "mojang-libs"
        url = uri("https://libraries.minecraft.net")
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
    compileOnly("com.mojang:brigadier:1.0.18")
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    // Configure the shadowJar task:
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("velocity-backend-perms")
        archiveVersion.set("")
        archiveClassifier.set("")
        mergeServiceFiles()      // merges META‑INF/services if needed
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = "com.clowdertech.velocitybackendperms.Main"
        }
    }
}

tasks.named("build") {
    dependsOn("shadowJar")
}
