plugins {
	id("dev.architectury.loom")
//	id("checkstyle")
}

repositories {
	maven(url = "https://maven.puffish.net/")
	maven(url = "https://maven.ladysnake.org/releases")
	maven(url = "https://cursemaven.com")
}

base.archivesName.set("${project.properties["archives_base_name"]}")
version = "${project.properties["mod_version"]}-${project.properties["minecraft_version"]}-forge"
group = "${project.properties["maven_group"]}"

evaluationDependsOn(":Common")

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
	minecraft("com.mojang:minecraft:${project.properties["minecraft_version"]}")
	mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"]}:v2")

	forge("net.minecraftforge:forge:${project.properties["minecraft_version"]}-${project.properties["forge_version"]}")

	modImplementation("io.github.edwinmindcraft:calio-forge:${project.properties["calio_version"]}")
	modImplementation("io.github.edwinmindcraft:apoli-forge:${project.properties["apoli_version"]}")
	modImplementation("io.github.edwinmindcraft:origins-forge:${project.properties["origins_version"]}")
	modImplementation("curse.maven:caelus-308989:${project.properties["caelus_version"]}")
	modImplementation("net.puffish:skillsmod:${project.properties["skills_version"]}:forge")

	implementation(project(path = ":Common", configuration = "namedElements"))
}

tasks.check {
	dependsOn(project(":Common").tasks.check)
}

tasks.processResources {
	from(project(":Common").sourceSets.main.get().resources)

	inputs.property("version", project.properties["mod_version"])
	filesMatching("META-INF/mods.toml") {
		expand(mapOf("version" to project.properties["mod_version"]))
	}
}

tasks.jar {
	from(project(":Common").sourceSets.main.get().output.classesDirs)
}