package fr.noltox.livestockcraft.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.nio.file.Path;
import java.util.function.Supplier;

public interface Platform {
	boolean isModLoaded(String modId);

	/** The loader's config directory, where {@code livestockcraft.toml} lives. */
	Path configDir();

	/**
	 * Registers {@code factory}'s value under {@code livestockcraft:<name>}. The returned supplier may only be
	 * queried once registries are populated. Must be called during mod initialization.
	 */
	<R, T extends R> Supplier<T> register(Registry<R> registry, String name, Supplier<T> factory);

	/** Registers the default attributes of a mod entity type. */
	<T extends LivingEntity> void registerAttributes(Supplier<EntityType<T>> type, Supplier<AttributeSupplier.Builder> attributes);

	/** Creates a spawn egg item for an entity type registered through {@link #register}. */
	Item createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties properties);

	/** Appends an item to a vanilla creative tab. */
	void addToCreativeTab(ResourceKey<CreativeModeTab> tab, Supplier<? extends ItemLike> item);

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
