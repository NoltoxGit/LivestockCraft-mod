package fr.noltox.livestockcraft.domestication;

import java.util.List;

/**
 * Tunable domestication values.
 *
 * @param stageThresholds     rate at which each skin stage starts; exactly {@link DomesticationMath#STAGE_COUNT}
 *                            values, the first is 0, strictly increasing multiples of {@link DomesticationMath#STEP}
 * @param stepsGainedPerBirth steps a child gains over its parents' average rate
 */
public record DomesticationConfig(List<Integer> stageThresholds, int stepsGainedPerBirth) {

	public static final int MAX_STEPS_GAINED_PER_BIRTH = 20;

	public static final DomesticationConfig DEFAULT = new DomesticationConfig(List.of(0, 20, 40, 60, 80), 1);

	public DomesticationConfig {
		stageThresholds = List.copyOf(stageThresholds);
		validateThresholds(stageThresholds);
		if (stepsGainedPerBirth < 0 || stepsGainedPerBirth > MAX_STEPS_GAINED_PER_BIRTH) {
			throw new IllegalArgumentException(
				"stepsGainedPerBirth must be between 0 and " + MAX_STEPS_GAINED_PER_BIRTH + ", got " + stepsGainedPerBirth);
		}
	}

	private static void validateThresholds(List<Integer> thresholds) {
		if (thresholds.size() != DomesticationMath.STAGE_COUNT) {
			throw new IllegalArgumentException(
				"stageThresholds must have exactly " + DomesticationMath.STAGE_COUNT + " values, got " + thresholds);
		}
		if (thresholds.getFirst() != DomesticationMath.MIN_RATE) {
			throw new IllegalArgumentException("stageThresholds must start at 0, got " + thresholds);
		}
		int previous = -1;
		for (int threshold : thresholds) {
			if (threshold <= previous || threshold > DomesticationMath.MAX_RATE || threshold % DomesticationMath.STEP != 0) {
				throw new IllegalArgumentException(
					"stageThresholds must be strictly increasing multiples of " + DomesticationMath.STEP
						+ " up to " + DomesticationMath.MAX_RATE + ", got " + thresholds);
			}
			previous = threshold;
		}
	}
}
