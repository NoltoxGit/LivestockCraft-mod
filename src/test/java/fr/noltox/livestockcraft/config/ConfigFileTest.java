package fr.noltox.livestockcraft.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.noltox.livestockcraft.domestication.DomesticationConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigFileTest {

	private static final LivestockCraftConfig CUSTOM = new LivestockCraftConfig(
		new DomesticationConfig(List.of(0, 25, 50, 75, 100), 3), 120, 0.5, true);

	@TempDir
	Path dir;

	@Test
	void missingFileIsCreatedWithDefaultsAndComments() throws IOException {
		ConfigFile.Result result = ConfigFile.load(dir);
		assertEquals(LivestockCraftConfig.DEFAULT, result.config());
		assertTrue(result.written());
		assertTrue(result.warnings().isEmpty());
		String text = Files.readString(dir.resolve(ConfigFile.FILE_NAME));
		assertTrue(text.contains("# Domestication rate (percent)"));
		assertTrue(text.contains("stageThresholds = [0, 20, 40, 60, 80]"));
		assertTrue(text.contains("cooldownSeconds = 60"));
	}

	@Test
	void generatedFileIsReadBackUnchanged() throws IOException {
		ConfigFile.load(dir);
		String first = Files.readString(dir.resolve(ConfigFile.FILE_NAME));
		ConfigFile.Result second = ConfigFile.load(dir);
		assertEquals(LivestockCraftConfig.DEFAULT, second.config());
		assertFalse(second.written());
		assertTrue(second.warnings().isEmpty());
		assertEquals(first, Files.readString(dir.resolve(ConfigFile.FILE_NAME)));
	}

	@Test
	void customValuesRoundTrip() {
		ConfigFile.Read read = ConfigFile.read(ConfigFile.render(CUSTOM));
		assertEquals(CUSTOM, read.config());
		assertFalse(read.needsRewrite());
		assertTrue(read.warnings().isEmpty());
	}

	@Test
	void userEditsAreRead() throws IOException {
		Path file = dir.resolve(ConfigFile.FILE_NAME);
		ConfigFile.load(dir);
		String edited = Files.readString(file)
			.replace("stageThresholds = [0, 20, 40, 60, 80]", "stageThresholds = [0, 25, 50, 75, 100] # quarters")
			.replace("cooldownSeconds = 60", "cooldownSeconds = 30")
			.replace("bovineSpawnWeightMultiplier = 1.0", "bovineSpawnWeightMultiplier = 2");
		Files.writeString(file, edited, StandardCharsets.UTF_8);

		ConfigFile.Result result = ConfigFile.load(dir);
		assertEquals(List.of(0, 25, 50, 75, 100), result.config().domestication().stageThresholds());
		assertEquals(30, result.config().milkCooldownSeconds());
		assertEquals(2.0, result.config().bovineSpawnWeightMultiplier());
		assertFalse(result.written());
		assertEquals(edited, Files.readString(file));
	}

	@Test
	void invalidValuesFallBackAndAreRewritten() throws IOException {
		Path file = dir.resolve(ConfigFile.FILE_NAME);
		Files.writeString(file, """
			[domestication]
			stageThresholds = [0, 30, 20, 60, 80]
			stepsGainedPerBirth = 2
			[milking]
			cooldownSeconds = -5
			[spawning]
			bovineSpawnWeightMultiplier = 1.5
			reduceVanillaCowWeight = 1
			""");

		ConfigFile.Result result = ConfigFile.load(dir);
		LivestockCraftConfig config = result.config();
		assertEquals(DomesticationConfig.DEFAULT.stageThresholds(), config.domestication().stageThresholds());
		assertEquals(2, config.domestication().stepsGainedPerBirth());
		assertEquals(60, config.milkCooldownSeconds());
		assertEquals(1.5, config.bovineSpawnWeightMultiplier());
		assertFalse(config.reduceVanillaCowWeight());
		assertEquals(3, result.warnings().size());
		assertTrue(result.written());
		assertEquals(config, ConfigFile.read(Files.readString(file)).config());
	}

	@Test
	void missingKeysAreAddedAndUnknownKeysReported() {
		ConfigFile.Read read = ConfigFile.read("""
			[milking]
			cooldownSeconds = 10
			legacyOption = true
			""");
		assertEquals(10, read.config().milkCooldownSeconds());
		assertEquals(LivestockCraftConfig.DEFAULT.domestication(), read.config().domestication());
		assertTrue(read.needsRewrite());
		assertEquals(5, read.warnings().size());
		assertTrue(read.warnings().getLast().contains("milking.legacyOption"));
	}

	@Test
	void unparsableFileIsLeftUntouched() throws IOException {
		Path file = dir.resolve(ConfigFile.FILE_NAME);
		String broken = "[milking\ncooldownSeconds = 10\n";
		Files.writeString(file, broken);

		ConfigFile.Result result = ConfigFile.load(dir);
		assertEquals(LivestockCraftConfig.DEFAULT, result.config());
		assertFalse(result.written());
		assertEquals(1, result.warnings().size());
		assertEquals(broken, Files.readString(file));
	}

	@Test
	void parserHandlesTheSupportedSubset() throws SimpleToml.ParseException {
		Map<String, Object> values = SimpleToml.parse("""
			# comment
			top = -3
			[section]
			flag = true   # trailing comment
			ratio = 2.5e1
			list = [1, 2, 3,]
			empty = []
			""");
		assertEquals(-3L, values.get("top"));
		assertEquals(true, values.get("section.flag"));
		assertEquals(25.0, values.get("section.ratio"));
		assertEquals(List.of(1L, 2L, 3L), values.get("section.list"));
		assertEquals(List.of(), values.get("section.empty"));
	}

	@Test
	void parserRejectsUnsupportedSyntax() {
		assertThrows(SimpleToml.ParseException.class, () -> SimpleToml.parse("a = 1\na = 2"));
		assertThrows(SimpleToml.ParseException.class, () -> SimpleToml.parse("a = \"text\""));
		assertThrows(SimpleToml.ParseException.class, () -> SimpleToml.parse("a = [1,\n2]"));
		assertThrows(SimpleToml.ParseException.class, () -> SimpleToml.parse("a ="));
		assertThrows(SimpleToml.ParseException.class, () -> SimpleToml.parse("[[array]]"));
		assertThrows(SimpleToml.ParseException.class, () -> SimpleToml.parse("just text"));
		assertThrows(SimpleToml.ParseException.class, () -> SimpleToml.parse("a = 99999999999999999999"));
	}
}
