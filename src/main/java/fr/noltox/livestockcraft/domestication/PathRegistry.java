package fr.noltox.livestockcraft.domestication;

import java.util.List;
import java.util.Optional;

/**
 * Source of breed path definitions. Entity code only depends on this interface, so the built-in
 * definitions can later be replaced by data-driven ones.
 */
public interface PathRegistry {

	List<BreedPath> all();

	default Optional<BreedPath> byId(String id) {
		return all().stream().filter(path -> path.id().equals(id)).findFirst();
	}

	/** Path a new animal of {@code family} follows; empty if the family has no path yet. */
	default Optional<BreedPath> defaultFor(Family family) {
		return all().stream().filter(path -> path.family() == family).findFirst();
	}
}
