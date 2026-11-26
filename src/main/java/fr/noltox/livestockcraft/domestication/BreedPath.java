package fr.noltox.livestockcraft.domestication;

import java.util.List;
import java.util.regex.Pattern;

/**
 * A breed path: the skins an animal goes through, one per stage, from the wild form to the final breed.
 *
 * @param id               path id, lowercase ({@code [a-z0-9_]+}), e.g. {@code black_angus}
 * @param family           family this path belongs to
 * @param skinIds          one skin id per stage, {@link DomesticationMath#STAGE_COUNT} entries
 * @param yieldMultipliers one drop multiplier per stage, {@link DomesticationMath#STAGE_COUNT} entries, not negative
 */
public record BreedPath(String id, Family family, List<String> skinIds, List<Double> yieldMultipliers) {

	private static final Pattern ID = Pattern.compile("[a-z0-9_]+");

	public BreedPath {
		if (!ID.matcher(id).matches()) {
			throw new IllegalArgumentException("Invalid path id: " + id);
		}
		skinIds = List.copyOf(skinIds);
		yieldMultipliers = List.copyOf(yieldMultipliers);
		if (skinIds.size() != DomesticationMath.STAGE_COUNT) {
			throw new IllegalArgumentException("Path " + id + " needs " + DomesticationMath.STAGE_COUNT + " skins, got " + skinIds);
		}
		for (String skinId : skinIds) {
			if (!ID.matcher(skinId).matches()) {
				throw new IllegalArgumentException("Path " + id + " has an invalid skin id: " + skinId);
			}
		}
		if (yieldMultipliers.size() != DomesticationMath.STAGE_COUNT) {
			throw new IllegalArgumentException(
				"Path " + id + " needs " + DomesticationMath.STAGE_COUNT + " yield multipliers, got " + yieldMultipliers);
		}
		for (double multiplier : yieldMultipliers) {
			if (!(multiplier >= 0)) {
				throw new IllegalArgumentException("Path " + id + " has a negative or NaN yield multiplier: " + yieldMultipliers);
			}
		}
	}

	public String skinId(int stage) {
		return skinIds.get(stage);
	}

	public double yieldMultiplier(int stage) {
		return yieldMultipliers.get(stage);
	}
}
