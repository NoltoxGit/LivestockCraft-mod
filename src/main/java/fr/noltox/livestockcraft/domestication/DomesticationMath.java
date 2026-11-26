package fr.noltox.livestockcraft.domestication;

import java.util.List;
import java.util.OptionalInt;

/**
 * Domestication rate arithmetic: quantization, inheritance, stage resolution and lock capping.
 * <p>
 * Pure logic with no Minecraft dependency. A rate is an integer percentage in
 * {@code [MIN_RATE, MAX_RATE]}, always a multiple of {@link #STEP} once stored on an animal.
 */
public final class DomesticationMath {

	public static final int MIN_RATE = 0;
	public static final int MAX_RATE = 100;
	/** Size of one step, in percent. Rates move by whole steps only. */
	public static final int STEP = 5;
	/** Number of skin stages per breed path. */
	public static final int STAGE_COUNT = 5;

	private DomesticationMath() {
	}

	public static int clampRate(int rate) {
		return Math.clamp(rate, MIN_RATE, MAX_RATE);
	}

	/** Rounds {@code value} down to the nearest multiple of {@code step} (towards negative infinity). */
	public static int quantizeDown(int value, int step) {
		if (step <= 0) {
			throw new IllegalArgumentException("step must be positive: " + step);
		}
		return Math.floorDiv(value, step) * step;
	}

	/**
	 * Rate of a child born from two parents: the parents' average rounded down to a whole step,
	 * plus {@code stepsGainedPerBirth} steps, clamped to {@code [MIN_RATE, MAX_RATE]}.
	 */
	public static int inherit(int rateA, int rateB, int stepsGainedPerBirth) {
		if (stepsGainedPerBirth < 0) {
			throw new IllegalArgumentException("stepsGainedPerBirth must not be negative: " + stepsGainedPerBirth);
		}
		int average = Math.floorDiv(clampRate(rateA) + clampRate(rateB), 2);
		return clampRate(quantizeDown(average, STEP) + STEP * stepsGainedPerBirth);
	}

	/** Index of the stage reached at {@code rate}: the last threshold that is lower than or equal to it. */
	public static int stageOf(int rate, List<Integer> thresholds) {
		int clamped = clampRate(rate);
		int stage = 0;
		for (int i = 0; i < thresholds.size(); i++) {
			if (thresholds.get(i) <= clamped) {
				stage = i;
			}
		}
		return stage;
	}

	/**
	 * Highest rate an animal locked at {@code stage} may reach: one step below the next stage's
	 * threshold, or {@code MAX_RATE} for the last stage.
	 */
	public static int lockCap(int stage, List<Integer> thresholds) {
		if (stage < 0 || stage >= thresholds.size()) {
			throw new IllegalArgumentException("stage out of range: " + stage);
		}
		if (stage == thresholds.size() - 1) {
			return MAX_RATE;
		}
		return thresholds.get(stage + 1) - STEP;
	}

	/** Lock inherited by a child: present if either parent is locked, the lower stage when both are. */
	public static OptionalInt inheritLock(OptionalInt lockA, OptionalInt lockB) {
		if (lockA.isPresent() && lockB.isPresent()) {
			return OptionalInt.of(Math.min(lockA.getAsInt(), lockB.getAsInt()));
		}
		return lockA.isPresent() ? lockA : lockB;
	}

	/** Rate of a child, capped by its inherited lock if it has one (see {@link #inheritLock}). */
	public static int childRate(int rateA, int rateB, OptionalInt childLock, DomesticationConfig config) {
		int rate = inherit(rateA, rateB, config.stepsGainedPerBirth());
		if (childLock.isEmpty()) {
			return rate;
		}
		return Math.min(rate, lockCap(childLock.getAsInt(), config.stageThresholds()));
	}
}
