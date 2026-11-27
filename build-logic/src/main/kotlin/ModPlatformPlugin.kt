import dev.kikugie.fletching_table.extension.FletchingTableExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.expand
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.model.IdeaModel
import javax.inject.Inject

abstract class ModPlatformPlugin @Inject constructor() : Plugin<Project> {
	override fun apply(project: Project) = with(project) {
		val inferredLoader = Loader.of(project.buildFile.name.substringAfter('.').replace(".gradle.kts", ""))

		val extension = extensions.create("platform", ModPlatformExtension::class.java).apply {
			loader.convention(inferredLoader.id)
		}

		when (inferredLoader) {
			is Loader.Fabric -> {
				extension.jarTask.convention(providers.provider {
					extensions.getByType<dev.kikugie.loomx.LoomCompatProjectExtension>().modJar.name
				})
				extension.sourcesJarTask.convention(providers.provider {
					extensions.getByType<dev.kikugie.loomx.LoomCompatProjectExtension>().modSourcesJar.name
				})
			}

			else -> {
				extension.jarTask.convention("jar")
				extension.sourcesJarTask.convention("sourcesJar")
			}
		}

		listOf(
			"org.jetbrains.kotlin.jvm",
			"com.google.devtools.ksp",
			"dev.kikugie.fletching-table",
			"me.modmuss50.mod-publish-plugin"
		).forEach {
			apply(
				plugin = it
			)
		}

		val (version, loader) = name.split('-', limit = 2)
		project.sc.properties.tags(version, loader)

		val ctx = Context(
			project = this,
			extension = extension,
			loader = Loader.of(extension.loader.get()),
			stonecutter = project.sc
		)
		configureProject(ctx)
	}

	private fun Project.configureProject(ctx: Context) {
		version = ctx.fullVersion

		listOf("java", "idea").forEach { apply(plugin = it) }
		ctx.extension.requiredJava.set(ctx.javaVersion)

		if (ctx.loader.isFabricLike) {
			ctx.extension.dependencies {
				required("java") { fabricLikeVersionRange = ">=${ctx.javaVersion.majorVersion}" }
			}
		}

		configureFletchingTable(ctx)
		registerGenerateManifestTask(ctx)
		configureJarTask(ctx)
		configureIdea()
		configureProcessResources(ctx)
		configureJava(ctx)
		configureTests(ctx)
		registerBuildAndCollectTask(ctx)

		configureModPublishing(ctx)

		if (envTrue("PUB_MAVEN_ENABLE")) {
			apply(plugin = "com.vanniktech.maven.publish")
			configureMavenPublishing(ctx)
		}
	}

	private fun Project.configureJava(ctx: Context) {
		extensions.configure<JavaPluginExtension>("java") {
			withSourcesJar()
			// Javadoc is slow and only required by Maven Central; skip it otherwise.
			if (envTrue("PUB_MAVEN_ENABLE")) withJavadocJar()
			sourceCompatibility = ctx.javaVersion
			targetCompatibility = ctx.javaVersion
			toolchain {
				languageVersion.set(JavaLanguageVersion.of(ctx.javaVersion.majorVersion))
			}
		}
	}

	/**
	 * JUnit for the shared `src/test` source set: pure, loader-agnostic logic only.
	 * The tests are identical for every loader, so they only run on Fabric
	 * (they still compile everywhere).
	 */
	private fun Project.configureTests(ctx: Context) {
		val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
		fun lib(alias: String) = libs.findLibrary(alias).get()
		dependencies.apply {
			add("testImplementation", platform(lib("junit-bom")))
			add("testImplementation", lib("junit-jupiter"))
			add("testRuntimeOnly", lib("junit-platform-launcher"))
		}
		val runTests = ctx.loader is Loader.Fabric
		tasks.withType<Test>().configureEach {
			useJUnitPlatform()
			enabled = runTests
		}
	}

	private fun Project.registerGenerateManifestTask(ctx: Context) {
		abstract class GenerateModManifestTask : DefaultTask() {
			@get:Input
			abstract val content: Property<String>

			/** Path of the manifest inside [outputDir], e.g. `META-INF/neoforge.mods.toml`. */
			@get:Input
			abstract val manifestPath: Property<String>

			@get:OutputDirectory
			abstract val outputDir: DirectoryProperty

			@TaskAction
			fun generate() {
				val file = outputDir.file(manifestPath).get().asFile
				file.parentFile.mkdirs()
				file.writeText(content.get())
			}
		}
		val generateTask = tasks.register<GenerateModManifestTask>("generateModManifest") {
			content.set(ctx.loader.generateManifest(ctx))
			manifestPath.set(ctx.loader.modManifestPath)
			outputDir.set(layout.buildDirectory.dir("generated/modManifest"))
		}

		// Registering the task (not its directory) as a source dir makes every consumer of the
		// main resources (processResources, sourcesJar...) depend on it without explicit dependsOn.
		the<JavaPluginExtension>().sourceSets.named("main") { resources.srcDir(generateTask) }
	}

	private fun Project.configureProcessResources(ctx: Context) {
		val javaVersionValue = "JAVA_${ctx.javaVersion.majorVersion}"
		val excluded = ctx.loader.excludedResources
		tasks.named<ProcessResources>("processResources") {
			dependsOn(tasks.named("stonecutterGenerate"), "kspKotlin")
			filesMatching("*.mixins.json") {
				expand("java" to javaVersionValue)
			}
			exclude(excluded)
			// Placeholders that keep empty asset folders in git; never ship them.
			exclude("**/.gitkeep")
		}
	}

	private fun Project.configureJarTask(ctx: Context) {
		tasks.withType<Jar>().configureEach {
			archiveBaseName.set(ctx.modId)
		}
	}

	private fun Project.configureIdea() {
		extensions.configure<IdeaModel>("idea") {
			module {
				// Sources only: Javadoc jars slow down the IDE sync and add nothing over sources.
				isDownloadSources = true
			}
		}
	}

	private fun Project.configureFletchingTable(ctx: Context) {
		extensions.configure<FletchingTableExtension> {
			mixins.create("main") { mixin("default", "${ctx.modId}.mixins.json") }
			j52j.register("main") { extension("json", "**/*.json5") }
		}
	}

	private fun Project.registerBuildAndCollectTask(ctx: Context) {
		tasks.register<Copy>("buildAndCollect") {
			from(
				tasks.named(ctx.extension.jarTask.get()),
				tasks.named(ctx.extension.sourcesJarTask.get()),
			)
			if (envTrue("PUB_MAVEN_ENABLE")) from(tasks.named("javadocJar"))
			into(rootProject.layout.buildDirectory.file("libs/${ctx.basicVersion}"))
			dependsOn("build")
			group = "build"
		}
	}
}
