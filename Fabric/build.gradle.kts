plugins {
    id("dev.architectury.loom")
    id("checkstyle")
}

repositories {
    maven(url = "https://maven.puffish.net/")
    maven(url = "https://maven.terraformersmc.com/")
    maven(url = "https://maven.shedaniel.me/")
    maven(url = "https://maven.ladysnake.org/releases")
    maven(url = "https://maven.jamieswhiteshirt.com/libs-release")
    maven(url = "https://jitpack.io")
}

base.archivesName.set("${project.properties["archives_base_name"]}")
version = "${project.properties["mod_version"]}-${project.properties["minecraft_version"]}-fabric"
group = "${project.properties["maven_group"]}"

evaluationDependsOn(":Common")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"]}:v2")

    modImplementation("com.github.apace100:origins-fabric:${project.properties["origins_version"]}")
    modImplementation("com.github.apace100:apoli:${project.properties["apoli_version"]}")
    modImplementation("net.puffish:skillsmod:${project.properties["skills_version"]}:fabric")
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_api_version"]}")

    implementation(project(path = ":Common", configuration = "namedElements"))
}

tasks.check {
    dependsOn(project(":Common").tasks.check)
}

tasks.processResources {
    from(project(":Common").sourceSets.main.get().resources)

    inputs.property("version", project.properties["mod_version"])
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.properties["mod_version"]))
    }
}

tasks.jar {
    from(project(":Common").sourceSets.main.get().output.classesDirs)
}