package fr.noltox.livestockcraft.platform;

import java.nio.file.Path;

public interface Platform {
	boolean isModLoaded(String modId);

	/** The loader's config directory, where {@code livestockcraft.toml} lives. */
	Path configDir();

	ModLoader loader();

	String mcVersion();

	boolean isDevelopmentEnvironment();

	default boolean isDebug() {
		return isDevelopmentEnvironment();
	}

	enum ModLoader {
		FABRIC, NEOFORGE
	}
}
