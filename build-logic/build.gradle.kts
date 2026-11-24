plugins {
	`kotlin-dsl`
	kotlin("plugin.serialization") version embeddedKotlinVersion
}

gradlePlugin {
	plugins {
		register("modPlatform") {
			id = "mod-platform"
			implementationClass = "ModPlatformPlugin"
		}
		register("modRoot") {
			id = "mod-root"
			implementationClass = "ModRootPlugin"
		}
	}
}

repositories {
	mavenCentral()
	gradlePluginPortal()
	exclusiveContent {
		forRepositories(
			maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" },
			maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" },
		)
		filter { includeGroupAndSubgroups("dev.kikugie") }
	}
}

dependencies {
	implementation(libs.kikugie.postprocess)
	implementation(libs.kikugie.stonecutter)
	implementation(libs.kikugie.loomx)
	implementation(libs.mod.publish.plugin)
	implementation(libs.foojay.resolver)
	implementation(libs.fletching.table)
	implementation(libs.vanniktech.maven.publish)
	implementation(libs.serialization.json)
	implementation(libs.serialization.toml)
}
