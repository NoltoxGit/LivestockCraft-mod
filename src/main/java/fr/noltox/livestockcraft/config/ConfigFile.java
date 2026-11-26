package fr.noltox.livestockcraft.config;

import fr.noltox.livestockcraft.domestication.DomesticationConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads and writes {@code livestockcraft.toml}.
 * <p>
 * A missing file is created with defaults and comments. Missing or invalid values fall back to their default,
 * are reported as warnings, and the file is rewritten with the corrected values. A file that cannot be parsed
 * is never overwritten: defaults are used until it is fixed.
 */
public final class ConfigFile {

	public static final String FILE_NAME = "livestockcraft.toml";

	static final String KEY_STAGE_THRESHOLDS = "domestication.stageThresholds";
	static final String KEY_STEPS_GAINED_PER_BIRTH = "domestication.stepsGainedPerBirth";
	static final String KEY_MILK_COOLDOWN_SECONDS = "milking.cooldownSeconds";
	static final String KEY_SPAWN_WEIGHT_MULTIPLIER = "spawning.bovineSpawnWeightMultiplier";
	static final String KEY_REDUCE_VANILLA_COW_WEIGHT = "spawning.reduceVanillaCowWeight";

	private static final Set<String> KNOWN_KEYS = Set.of(
		KEY_STAGE_THRESHOLDS, KEY_STEPS_GAINED_PER_BIRTH, KEY_MILK_COOLDOWN_SECONDS,
		KEY_SPAWN_WEIGHT_MULTIPLIER, KEY_REDUCE_VANILLA_COW_WEIGHT
	);

	private ConfigFile() {
	}

	/**
	 * @param config   the values in effect
	 * @param warnings human-readable problems found in the file, to be logged
	 * @param written  whether the file was created or rewritten
	 */
	public record Result(LivestockCraftConfig config, List<String> warnings, boolean written) {
	}

	/** Parsed file content, before anything is written. */
	record Read(LivestockCraftConfig config, List<String> warnings, boolean needsRewrite) {
	}

	public static Result load(Path configDir) throws IOException {
		Path file = configDir.resolve(FILE_NAME);
		if (Files.notExists(file)) {
			write(file, LivestockCraftConfig.DEFAULT);
			return new Result(LivestockCraftConfig.DEFAULT, List.of(), true);
		}
		Read read = read(Files.readString(file, StandardCharsets.UTF_8));
		if (read.needsRewrite()) {
			write(file, read.config());
		}
		return new Result(read.config(), read.warnings(), read.needsRewrite());
	}

	static Read read(String text) {
		Map<String, Object> values;
		try {
			values = SimpleToml.parse(text);
		} catch (SimpleToml.ParseException e) {
			return new Read(LivestockCraftConfig.DEFAULT,
				List.of(FILE_NAME + " could not be parsed (" + e.getMessage() + "); using defaults, the file is left untouched"),
				false);
		}
		Values reader = new Values(values);
		LivestockCraftConfig defaults = LivestockCraftConfig.DEFAULT;
		DomesticationConfig domestication = new DomesticationConfig(
			reader.thresholds(KEY_STAGE_THRESHOLDS, defaults.domestication().stageThresholds()),
			reader.integer(KEY_STEPS_GAINED_PER_BIRTH, defaults.domestication().stepsGainedPerBirth(),
				0, DomesticationConfig.MAX_STEPS_GAINED_PER_BIRTH)
		);
		LivestockCraftConfig config = new LivestockCraftConfig(
			domestication,
			reader.integer(KEY_MILK_COOLDOWN_SECONDS, defaults.milkCooldownSeconds(),
				0, LivestockCraftConfig.MAX_MILK_COOLDOWN_SECONDS),
			reader.decimal(KEY_SPAWN_WEIGHT_MULTIPLIER, defaults.bovineSpawnWeightMultiplier(),
				0, LivestockCraftConfig.MAX_SPAWN_WEIGHT_MULTIPLIER),
			reader.bool(KEY_REDUCE_VANILLA_COW_WEIGHT, defaults.reduceVanillaCowWeight())
		);
		reader.warnUnknownKeys();
		return new Read(config, List.copyOf(reader.warnings), reader.needsRewrite);
	}

	static String render(LivestockCraftConfig config) {
		DomesticationConfig domestication = config.domestication();
		return """
			# LivestockCraft configuration.
			# Changes apply after a restart. Invalid values are reset to their default.

			[domestication]
			# Domestication rate (percent) at which each of the 5 skin stages starts.
			# Exactly 5 values: the first is 0, then strictly increasing multiples of 5, up to 100.
			# Default: [0, 20, 40, 60, 80]. Example of quarter bands: [0, 25, 50, 75, 100].
			stageThresholds = %s
			# Number of 5%% steps a newborn gains over the average rate of its parents. Range: 0 to %d.
			# Default: 1.
			stepsGainedPerBirth = %d

			[milking]
			# Seconds before the same bovine can be milked again. Range: 0 to %d.
			# Default: 60.
			cooldownSeconds = %d

			[spawning]
			# Multiplier on the bovine spawn weight. At 1.0 it equals the vanilla cow weight. Range: 0.0 to %s.
			# Default: 1.0.
			bovineSpawnWeightMultiplier = %s
			# When true, halves the vanilla cow spawn weight in bovine biomes, so cattle are not more common overall.
			# Default: false.
			reduceVanillaCowWeight = %s
			""".formatted(
			domestication.stageThresholds().toString(),
			DomesticationConfig.MAX_STEPS_GAINED_PER_BIRTH,
			domestication.stepsGainedPerBirth(),
			LivestockCraftConfig.MAX_MILK_COOLDOWN_SECONDS,
			config.milkCooldownSeconds(),
			LivestockCraftConfig.MAX_SPAWN_WEIGHT_MULTIPLIER,
			config.bovineSpawnWeightMultiplier(),
			config.reduceVanillaCowWeight()
		);
	}

	private static void write(Path file, LivestockCraftConfig config) throws IOException {
		Files.createDirectories(file.getParent());
		Path temp = file.resolveSibling(FILE_NAME + ".tmp");
		Files.writeString(temp, render(config), StandardCharsets.UTF_8);
		Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
	}

	/** Typed access to parsed values; records a warning and falls back to the default on any problem. */
	private static final class Values {
		private final Map<String, Object> values;
		private final Set<String> used = new HashSet<>();
		private final List<String> warnings = new ArrayList<>();
		private boolean needsRewrite;

		Values(Map<String, Object> values) {
			this.values = values;
		}

		private Object raw(String key) {
			used.add(key);
			Object value = values.get(key);
			if (value == null) {
				needsRewrite = true;
				warnings.add(key + " is missing; using the default");
			}
			return value;
		}

		private void invalid(String key, Object value, String expected, Object fallback) {
			needsRewrite = true;
			warnings.add(key + " = " + value + " is invalid (expected " + expected + "); using " + fallback);
		}

		int integer(String key, int fallback, int min, int max) {
			Object value = raw(key);
			if (value == null) {
				return fallback;
			}
			if (value instanceof Long number && number >= min && number <= max) {
				return number.intValue();
			}
			invalid(key, value, "an integer from " + min + " to " + max, fallback);
			return fallback;
		}

		double decimal(String key, double fallback, double min, double max) {
			Object value = raw(key);
			if (value == null) {
				return fallback;
			}
			if (value instanceof Number number) {
				double decimal = number.doubleValue();
				if (decimal >= min && decimal <= max) {
					return decimal;
				}
			}
			invalid(key, value, "a number from " + min + " to " + max, fallback);
			return fallback;
		}

		boolean bool(String key, boolean fallback) {
			Object value = raw(key);
			if (value == null) {
				return fallback;
			}
			if (value instanceof Boolean bool) {
				return bool;
			}
			invalid(key, value, "true or false", fallback);
			return fallback;
		}

		List<Integer> thresholds(String key, List<Integer> fallback) {
			Object value = raw(key);
			if (value == null) {
				return fallback;
			}
			String expected = "5 increasing multiples of 5 from 0 to 100, starting at 0";
			if (!(value instanceof List<?> list)) {
				invalid(key, value, expected, fallback);
				return fallback;
			}
			List<Integer> thresholds = new ArrayList<>();
			for (Object item : list) {
				if (!(item instanceof Long number) || number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
					invalid(key, value, expected, fallback);
					return fallback;
				}
				thresholds.add(number.intValue());
			}
			try {
				return new DomesticationConfig(thresholds, DomesticationConfig.DEFAULT.stepsGainedPerBirth()).stageThresholds();
			} catch (IllegalArgumentException e) {
				invalid(key, value, expected, fallback);
				return fallback;
			}
		}

		void warnUnknownKeys() {
			for (String key : values.keySet()) {
				if (!used.contains(key) && !KNOWN_KEYS.contains(key)) {
					warnings.add(key + " is not a known option and is ignored");
				}
			}
		}
	}
}
