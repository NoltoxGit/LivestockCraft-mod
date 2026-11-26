package fr.noltox.livestockcraft.domestication;

import java.util.List;

/** Breed paths defined in code. */
public final class BuiltinPathRegistry implements PathRegistry {

	public static final BreedPath BLACK_ANGUS = new BreedPath(
		"black_angus",
		Family.BEEF,
		List.of("aurochs", "bos_taurus", "buchan_humlie", "angus_doddie", "black_angus"),
		// Neutral until yields are tuned.
		List.of(1.0, 1.0, 1.0, 1.0, 1.0)
	);

	public static final BuiltinPathRegistry INSTANCE = new BuiltinPathRegistry();

	private static final List<BreedPath> PATHS = List.of(BLACK_ANGUS);

	private BuiltinPathRegistry() {
	}

	@Override
	public List<BreedPath> all() {
		return PATHS;
	}
}
