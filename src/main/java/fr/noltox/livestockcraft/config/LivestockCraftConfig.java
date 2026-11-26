package fr.noltox.livestockcraft.config;

import fr.noltox.livestockcraft.domestication.DomesticationConfig;

/**
 * All values read from {@code livestockcraft.toml}.
 *
 * @param domestication               stage thresholds and rate inheritance
 * @param milkCooldownSeconds         seconds before the same bovine can be milked again
 * @param bovineSpawnWeightMultiplier multiplier on the bovine spawn weight, which equals the vanilla cow weight at 1.0
 * @param reduceVanillaCowWeight      halve the vanilla cow weight in bovine biomes so the total cattle share is unchanged
 */
public record LivestockCraftConfig(
	DomesticationConfig domestication,
	int milkCooldownSeconds,
	double bovineSpawnWeightMultiplier,
	boolean reduceVanillaCowWeight
) {

	public static final int MAX_MILK_COOLDOWN_SECONDS = 86_400;
	public static final double MAX_SPAWN_WEIGHT_MULTIPLIER = 10.0;

	public static final LivestockCraftConfig DEFAULT = new LivestockCraftConfig(DomesticationConfig.DEFAULT, 60, 1.0, false);

	public LivestockCraftConfig {
		if (milkCooldownSeconds < 0 || milkCooldownSeconds > MAX_MILK_COOLDOWN_SECONDS) {
			throw new IllegalArgumentException("milkCooldownSeconds out of range: " + milkCooldownSeconds);
		}
		if (!(bovineSpawnWeightMultiplier >= 0 && bovineSpawnWeightMultiplier <= MAX_SPAWN_WEIGHT_MULTIPLIER)) {
			throw new IllegalArgumentException("bovineSpawnWeightMultiplier out of range: " + bovineSpawnWeightMultiplier);
		}
	}
}
