pluginManagement {
	repositories {
		mavenLocal()
		mavenCentral()
		gradlePluginPortal()
		// Single-purpose repositories are restricted to their own groups so Gradle
		// never queries them (or the others) for artifacts they cannot serve.
		exclusiveContent {
			forRepositories(
				maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" },
				maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" },
			)
			filter {
				includeGroupAndSubgroups("dev.kikugie")
				includeGroupAndSubgroups("org.quiltmc") // proxied for resource-postprocessor
			}
		}
		exclusiveContent {
			forRepository { maven("https://maven.parchmentmc.org") { name = "ParchmentMC" } }
			filter { includeGroupAndSubgroups("org.parchmentmc") }
		}
		maven("https://maven.neoforged.net/releases/") {
			name = "NeoForged"
			content { includeGroupAndSubgroups("net.neoforged") }
		}
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
	}
	includeBuild("build-logic")
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	// Keep in sync with the `stonecutter` version in gradle/libs.versions.toml!
	id("dev.kikugie.stonecutter") version "0.10"
	id("dev.kikugie.loom-back-compat") version "0.4.3" // Keep in sync with `loomx` in gradle/libs.versions.toml!
}

stonecutter {
	create(rootProject) {
		fun match(version: String, vararg loaders: String) =
			loaders.forEach { version("$version-$it", version).buildscript = "build.$it.gradle.kts" }

		match("1.21.1", "fabric", "neoforge")

		vcsVersion = "1.21.1-fabric"
	}
}

