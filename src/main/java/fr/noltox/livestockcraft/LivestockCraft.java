package fr.noltox.livestockcraft;

import fr.noltox.livestockcraft.config.ConfigFile;
import fr.noltox.livestockcraft.config.LivestockCraftConfig;
import fr.noltox.livestockcraft.platform.Platform;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

//? fabric {
import fr.noltox.livestockcraft.platform.fabric.FabricPlatform;
//?} neoforge {
/*import fr.noltox.livestockcraft.platform.neoforge.NeoforgePlatform;
 *///?}

@SuppressWarnings("LoggingSimilarMessage")
public class LivestockCraft {

	public static final String MOD_ID = /*$ mod_id*/ "livestockcraft";
	public static final String MOD_VERSION = /*$ mod_version*/ "0.1.0";
	public static final String MOD_FRIENDLY_NAME = /*$ mod_name*/ "LivestockCraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Platform PLATFORM = createPlatformInstance();

	private static LivestockCraftConfig config = LivestockCraftConfig.DEFAULT;

	public static void onInitialize() {
		LOGGER.info("Initializing {} on {}", MOD_ID, LivestockCraft.xplat().loader());
		LOGGER.debug("{}: { version: {}; friendly_name: {} }", MOD_ID, MOD_VERSION, MOD_FRIENDLY_NAME);
		loadConfig();
	}

	/** Values from {@code livestockcraft.toml}, loaded once at startup. */
	public static LivestockCraftConfig config() {
		return config;
	}

	private static void loadConfig() {
		Path dir = xplat().configDir();
		try {
			ConfigFile.Result result = ConfigFile.load(dir);
			result.warnings().forEach(warning -> LOGGER.warn("Config: {}", warning));
			config = result.config();
			LOGGER.info("Loaded config from {}{}: {}",
				dir.resolve(ConfigFile.FILE_NAME), result.written() ? " (written)" : "", config);
		} catch (IOException e) {
			LOGGER.error("Could not read or write {} in {}; using defaults", ConfigFile.FILE_NAME, dir, e);
		}
	}

	public static void onInitializeClient() {
		LOGGER.info("Initializing {} Client on {}", MOD_ID, LivestockCraft.xplat().loader());
	}

	public static Platform xplat() {
		return PLATFORM;
	}

	private static Platform createPlatformInstance() {
		//? fabric {
		return new FabricPlatform();
		//?} neoforge {
		/*return new NeoforgePlatform();
		 *///?}
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static ResourceLocation id(String namespace, String path) {
		return ResourceLocation.fromNamespaceAndPath(namespace, path);
	}
}
