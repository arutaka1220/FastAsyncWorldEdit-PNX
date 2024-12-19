import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
}

project.description = "PNX"
applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    mavenLocal()
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    flatDir { dir(File("src/main/resources")) }
}

val localImplementation = configurations.create("localImplementation") {
    description = "Dependencies used locally, but provided by the runtime Bukkit implementation"
    isCanBeConsumed = false
    isCanBeResolved = false
}

dependencies {
    compileOnly("com.github.PowerNukkitX:PowerNukkitX:snapshot") {
        exclude("junit", "junit")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    // Modules
    api(projects.worldeditCore)
    api(projects.worldeditLibs.pnx)
    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Minecraft expectations
    implementation(libs.fastutil)
    // Platform expectations
    // Logging
    compileOnly("org.apache.logging.log4j:log4j-api")

    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("com.google.code.gson:gson:2.11.0")

    // Third party
    implementation("dev.notmyfault.serverlib:ServerLib")
    implementation("com.intellectualsites.paster:Paster") { isTransitive = false }
    api(libs.lz4Java) { isTransitive = false }
    api(libs.sparsebitset) { isTransitive = false }
    api(libs.parallelgzip) { isTransitive = false }
    compileOnly("net.kyori:adventure-api")
    compileOnlyApi("org.checkerframework:checker-qual")

    // Tests
    testImplementation(libs.mockito)
    testImplementation("net.kyori:adventure-api")
    testImplementation("org.checkerframework:checker-qual")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Class-Path" to CLASSPATH,
                "WorldEdit-Version" to project.version)
    }
}

tasks.javadoc {
    enabled = false
}

addJarManifest(WorldEditKind.Plugin, includeClasspath = true)

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("${rootProject.name}-PNX-${project.version}.${archiveExtension.getOrElse("jar")}")
    dependencies {
        relocate("org.antlr.v4", "com.sk89q.worldedit.antlr4")
        include(dependency(":worldedit-core"))
        include(dependency("org.antlr:antlr4-runtime"))
        relocate("it.unimi.dsi.fastutil", "com.sk89q.worldedit.pnx.fastutil") {
            include(dependency("it.unimi.dsi:fastutil"))
        }
        relocate("org.lz4", "com.fastasyncworldedit.core.lz4") {
            include(dependency("org.lz4:lz4-java:1.8.0"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
