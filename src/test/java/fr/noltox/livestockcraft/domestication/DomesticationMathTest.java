package fr.noltox.livestockcraft.domestication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.OptionalInt;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DomesticationMathTest {

	private static final List<Integer> TEXT_THRESHOLDS = List.of(0, 20, 40, 60, 80);
	private static final List<Integer> DIAGRAM_THRESHOLDS = List.of(0, 25, 50, 75, 100);

	@Nested
	class QuantizeDown {
		@ParameterizedTest
		@CsvSource({"0, 0", "4, 0", "5, 5", "12, 10", "99, 95", "100, 100", "-1, -5"})
		void roundsDownToWholeSteps(int value, int expected) {
			assertEquals(expected, DomesticationMath.quantizeDown(value, 5));
		}

		@Test
		void rejectsNonPositiveStep() {
			assertThrows(IllegalArgumentException.class, () -> DomesticationMath.quantizeDown(10, 0));
		}
	}

	@Nested
	class Inherit {
		@ParameterizedTest(name = "{0}% + {1}% -> {2}%")
		@CsvSource({
			"0, 0, 5",      // equal parents at 0
			"40, 40, 45",   // equal parents
			"10, 30, 25",   // unequal parents, whole-step average
			"5, 10, 10",    // average 7.5 rounds down to 5
			"30, 10, 25",   // order does not matter
		})
		void addsOneStepToTheQuantizedAverage(int rateA, int rateB, int expected) {
			assertEquals(expected, DomesticationMath.inherit(rateA, rateB, 1));
		}

		@Test
		void clampsAtMaximum() {
			assertEquals(100, DomesticationMath.inherit(100, 100, 1));
			assertEquals(100, DomesticationMath.inherit(95, 100, 1));
			assertEquals(100, DomesticationMath.inherit(90, 90, 20));
		}

		@Test
		void clampsOutOfRangeParents() {
			assertEquals(0, DomesticationMath.inherit(-20, -20, 0));
			assertEquals(5, DomesticationMath.inherit(-20, 0, 1));
			assertEquals(100, DomesticationMath.inherit(150, 150, 0));
		}

		@Test
		void honoursStepsGainedPerBirth() {
			assertEquals(20, DomesticationMath.inherit(10, 10, 2));
			assertEquals(10, DomesticationMath.inherit(10, 10, 0));
		}

		@Test
		void rejectsNegativeSteps() {
			assertThrows(IllegalArgumentException.class, () -> DomesticationMath.inherit(0, 0, -1));
		}
	}

	@Nested
	class StageOf {
		@ParameterizedTest(name = "{0}% -> stage {1}")
		@CsvSource({"0, 0", "19, 0", "20, 1", "39, 1", "40, 2", "80, 4", "99, 4", "100, 4"})
		void textThresholds(int rate, int expected) {
			assertEquals(expected, DomesticationMath.stageOf(rate, TEXT_THRESHOLDS));
		}

		@ParameterizedTest(name = "{0}% -> stage {1}")
		@CsvSource({"0, 0", "19, 0", "20, 0", "25, 1", "74, 2", "75, 3", "99, 3", "100, 4"})
		void diagramThresholds(int rate, int expected) {
			assertEquals(expected, DomesticationMath.stageOf(rate, DIAGRAM_THRESHOLDS));
		}

		@Test
		void clampsOutOfRangeRates() {
			assertEquals(0, DomesticationMath.stageOf(-5, TEXT_THRESHOLDS));
			assertEquals(4, DomesticationMath.stageOf(150, DIAGRAM_THRESHOLDS));
		}
	}

	@Nested
	class LockCap {
		@ParameterizedTest(name = "stage {0} -> {1}%")
		@CsvSource({"0, 15", "1, 35", "3, 75", "4, 100"})
		void textThresholds(int stage, int expected) {
			assertEquals(expected, DomesticationMath.lockCap(stage, TEXT_THRESHOLDS));
		}

		@ParameterizedTest(name = "stage {0} -> {1}%")
		@CsvSource({"0, 20", "3, 95", "4, 100"})
		void diagramThresholds(int stage, int expected) {
			assertEquals(expected, DomesticationMath.lockCap(stage, DIAGRAM_THRESHOLDS));
		}

		@Test
		void rejectsUnknownStage() {
			assertThrows(IllegalArgumentException.class, () -> DomesticationMath.lockCap(5, TEXT_THRESHOLDS));
			assertThrows(IllegalArgumentException.class, () -> DomesticationMath.lockCap(-1, TEXT_THRESHOLDS));
		}
	}

	@Nested
	class Locks {
		private final DomesticationConfig config = DomesticationConfig.DEFAULT;

		@Test
		void unlockedParentsGiveAnUnlockedChild() {
			OptionalInt lock = DomesticationMath.inheritLock(OptionalInt.empty(), OptionalInt.empty());
			assertEquals(OptionalInt.empty(), lock);
			assertEquals(25, DomesticationMath.childRate(10, 30, lock, config));
		}

		@Test
		void oneLockedParentLocksAndCapsTheChild() {
			OptionalInt lock = DomesticationMath.inheritLock(OptionalInt.of(1), OptionalInt.empty());
			assertEquals(OptionalInt.of(1), lock);
			assertEquals(OptionalInt.of(1), DomesticationMath.inheritLock(OptionalInt.empty(), OptionalInt.of(1)));
			// 35 + 35 -> 40 without the lock; stage 1 caps at 35.
			assertEquals(35, DomesticationMath.childRate(35, 35, lock, config));
			// Below the cap, the rate is not changed.
			assertEquals(30, DomesticationMath.childRate(20, 30, lock, config));
		}

		@Test
		void bothLockedAtDifferentStagesUsesTheLowerStage() {
			OptionalInt lock = DomesticationMath.inheritLock(OptionalInt.of(3), OptionalInt.of(1));
			assertEquals(OptionalInt.of(1), lock);
			assertEquals(35, DomesticationMath.childRate(75, 35, lock, config));
		}

		@Test
		void lastStageLockAllowsTheMaximum() {
			OptionalInt lock = DomesticationMath.inheritLock(OptionalInt.of(4), OptionalInt.of(4));
			assertEquals(100, DomesticationMath.childRate(95, 100, lock, config));
		}
	}
}
