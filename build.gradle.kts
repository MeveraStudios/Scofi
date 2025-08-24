import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    java
    id("java-library")
    id("com.gradleup.shadow") version "8.3.9" // Note: 8.3.9 is not a valid version, latest is 8.1.1
    id("com.vanniktech.maven.publish") version "0.29.0" // Note: 0.33.0 is not a valid version, latest is 0.29.0
}

group = "studio.mevera"
version = "2.0.0"

// Configure Java 17 for compilation but target Java 8 for compatibility
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    // Generate sources and javadoc jars
    withSourcesJar()
    withJavadocJar()
}

// Compiler options - Compile with Java 17 but target Java 8 bytecode for compatibility
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"

    // Target Java 8 for maximum compatibility with older MC versions
    // Change to 17 if you only support modern MC versions (1.17+)
    options.release.set(8)

    options.compilerArgs.add("-parameters")
}

repositories {
    mavenCentral()
    mavenLocal()

    // Spigot/Bukkit repositories
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        name = "sonatype-central"
        url = uri("https://oss.sonatype.org/content/repositories/central")
    }

    // Additional repositories
    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }

    // Paper repository (for modern Paper API if needed)
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    gradlePluginPortal()
}

dependencies {
    // Keep 1.8.8 for maximum compatibility
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")

    // Lombok - Latest version
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // Annotations - Latest version
    compileOnly("org.jetbrains:annotations:24.1.0") // Note: 26.0.1 is not a valid version
    annotationProcessor("org.jetbrains:annotations:24.1.0")

    // Adventure API - Latest versions
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.17.0")
}

mavenPublishing {
    coordinates(group.toString(), "scofi", version.toString())

    pom {
        name.set("Scofi")
        description.set("A modern customizable scoreboard library for spigot development.")
        inceptionYear.set("2025")
        url.set("https://github.com/MeveraStudios/Scofi/")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://mit-license.org/")
            }
        }
        developers {
            developer {
                id.set("mqzn")
                name.set("Mqzn")
                url.set("https://github.com/Mqzn/")
            }
        }
        scm {
            url.set("https://github.com/MeveraStudios/Scofi/")
            connection.set("scm:git:git://github.com/MeveraStudios/Scofi.git")
            developerConnection.set("scm:git:ssh://git@github.com/MeveraStudios/Scofi.git")
        }
    }

    if (!gradle.startParameter.taskNames.any { it == "publishToMavenLocal" }) {
        publishToMavenCentral()
        signAllPublications()
    }
}


// Shadow JAR configuration
tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("Scofi")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())

    // Minimize JAR size (be careful with reflection)
    minimize {
        // Exclude classes that might be accessed via reflection
        exclude(dependency("org.spigotmc:.*"))
    }

    // Merge service files
    mergeServiceFiles()

    // Set the output directory (uncomment and modify as needed)
    // destinationDirectory.set(file("D:\\minecraft-dev\\servers\\paper-1.20\\plugins"))
}

// Build task configuration
tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

// Javadoc configuration for Java 17
tasks.withType<Javadoc> {
    if (JavaVersion.current().isJava9Compatible) {
        val options = options as StandardJavadocDocletOptions
        options.addBooleanOption("html5", true)
        options.addStringOption("Xdoclint:none", "-quiet")
    }

    // Set source compatibility for javadoc
    options.source = "8"

    // Exclude implementation packages from javadoc
    exclude("**/impl/**")
    exclude("**/internal/**")
}

// Clean task enhancement
tasks.named<Delete>("clean") {
    delete("$projectDir/out")
    delete("$projectDir/bin")
}

// Task to display Java version info
tasks.register("javaVersion") {
    doLast {
        println("====================")
        println("Java Configuration:")
        println("====================")
        println("Toolchain: Java ${java.toolchain.languageVersion.get()}")
        tasks.named<JavaCompile>("compileJava").get().options.release.get().let {
            println("Target: Java $it")
        }
        println("Current JVM: ${JavaVersion.current()}")
        println("Shadow Plugin: ${plugins.findPlugin("com.gradleup.shadow")?.javaClass?.protectionDomain?.codeSource?.location}")
        println("====================")
    }
}
