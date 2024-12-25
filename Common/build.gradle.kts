plugins {
    id("dev.architectury.loom")
    id("checkstyle")
}

repositories {
    maven(url = "https://maven.puffish.net/")
}

base.archivesName.set("${project.properties["archives_base_name"]}")
version = "${project.properties["mod_version"]}-${project.properties["minecraft_version"]}-common"
group = "${project.properties["maven_group"]}"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"]}:v2")

    modImplementation("net.puffish:skillsmod:${project.properties["skills_version"]}")
}

tasks.jar {
    from(project.rootDir.resolve("LICENSE.txt"))

    manifest {
        attributes["Fabric-Loom-Remap"] = "true"
    }
}