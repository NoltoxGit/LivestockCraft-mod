package fr.noltox.livestockcraft.domestication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class DefinitionsTest {

	@Test
	void defaultConfigUsesTextThresholds() {
		assertEquals(List.of(0, 20, 40, 60, 80), DomesticationConfig.DEFAULT.stageThresholds());
		assertEquals(1, DomesticationConfig.DEFAULT.stepsGainedPerBirth());
	}

	@Test
	void configAcceptsDiagramThresholds() {
		new DomesticationConfig(List.of(0, 25, 50, 75, 100), 1);
	}

	@Test
	void configRejectsInvalidThresholds() {
		assertThrows(IllegalArgumentException.class, () -> new DomesticationConfig(List.of(0, 20, 40, 60), 1));
		assertThrows(IllegalArgumentException.class, () -> new DomesticationConfig(List.of(5, 20, 40, 60, 80), 1));
		assertThrows(IllegalArgumentException.class, () -> new DomesticationConfig(List.of(0, 40, 20, 60, 80), 1));
		assertThrows(IllegalArgumentException.class, () -> new DomesticationConfig(List.of(0, 20, 20, 60, 80), 1));
		assertThrows(IllegalArgumentException.class, () -> new DomesticationConfig(List.of(0, 22, 40, 60, 80), 1));
		assertThrows(IllegalArgumentException.class, () -> new DomesticationConfig(List.of(0, 20, 40, 60, 105), 1));
	}

	@Test
	void configRejectsOutOfRangeSteps() {
		assertThrows(IllegalArgumentException.class, () -> new DomesticationConfig(List.of(0, 20, 40, 60, 80), -1));
		assertThrows(IllegalArgumentException.class, () -> new DomesticationConfig(List.of(0, 20, 40, 60, 80), 21));
	}

	@Test
	void blackAngusPathIsTheBeefDefault() {
		PathRegistry registry = BuiltinPathRegistry.INSTANCE;
		BreedPath path = registry.byId("black_angus").orElseThrow();
		assertEquals(Family.BEEF, path.family());
		assertEquals(List.of("aurochs", "bos_taurus", "buchan_humlie", "angus_doddie", "black_angus"), path.skinIds());
		assertEquals("black_angus", path.skinId(4));
		assertEquals(Optional.of(path), registry.defaultFor(Family.BEEF));
		assertTrue(registry.defaultFor(Family.YIELD).isEmpty());
	}

	@Test
	void pathRejectsWrongSizesAndIds() {
		List<Double> yields = List.of(1.0, 1.0, 1.0, 1.0, 1.0);
		List<String> skins = List.of("a", "b", "c", "d", "e");
		assertThrows(IllegalArgumentException.class, () -> new BreedPath("Bad Id", Family.BEEF, skins, yields));
		assertThrows(IllegalArgumentException.class, () -> new BreedPath("p", Family.BEEF, List.of("a"), yields));
		assertThrows(IllegalArgumentException.class, () -> new BreedPath("p", Family.BEEF, skins, List.of(1.0)));
		assertThrows(IllegalArgumentException.class,
			() -> new BreedPath("p", Family.BEEF, skins, List.of(1.0, 1.0, -1.0, 1.0, 1.0)));
	}

	@Test
	void enumIdsRoundTrip() {
		for (Family family : Family.values()) {
			assertEquals(Optional.of(family), Family.byId(family.id()));
		}
		for (Climate climate : Climate.values()) {
			assertEquals(Optional.of(climate), Climate.byId(climate.id()));
		}
		assertEquals("beef", Family.BEEF.id());
		assertEquals("cold", Climate.COLD.id());
		assertTrue(Family.byId("BEEF").isEmpty());
	}
}
