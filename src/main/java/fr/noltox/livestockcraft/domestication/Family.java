package fr.noltox.livestockcraft.domestication;

import java.util.Locale;
import java.util.Optional;

/** Breeding goal chosen by the food used for breeding. */
public enum Family {
	/** Quality: show cattle, selected with the Exception Pouch. */
	BEEF,
	/** Quantity: selected with plain wheat. */
	YIELD;

	/** Lowercase id used in saved data and resource names. */
	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<Family> byId(String id) {
		for (Family family : values()) {
			if (family.id().equals(id)) {
				return Optional.of(family);
			}
		}
		return Optional.empty();
	}
}
