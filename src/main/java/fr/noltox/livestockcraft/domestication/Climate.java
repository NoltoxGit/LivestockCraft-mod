package fr.noltox.livestockcraft.domestication;

import java.util.Locale;
import java.util.Optional;

/** Climate of an animal, fixed at spawn from its biome. */
public enum Climate {
	WARM,
	COLD;

	/** Lowercase id used in saved data and resource names. */
	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<Climate> byId(String id) {
		for (Climate climate : values()) {
			if (climate.id().equals(id)) {
				return Optional.of(climate);
			}
		}
		return Optional.empty();
	}
}
