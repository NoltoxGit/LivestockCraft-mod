package fr.noltox.livestockcraft.platform.fabric;

//? fabric {

import fr.noltox.livestockcraft.LivestockCraft;
import fr.noltox.livestockcraft.platform.Platform;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;

import java.nio.file.Path;
import java.util.function.Supplier;

public class FabricPlatform implements Platform {

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public Path configDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	public <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Supplier<T> factory) {
		T value = Registry.register(registry, LivestockCraft.id(name), factory.get());
		return () -> value;
	}

	@Override
	public <T extends LivingEntity> void registerAttributes(Supplier<EntityType<T>> type, Supplier<AttributeSupplier.Builder> attributes) {
		FabricDefaultAttributeRegistry.register(type.get(), attributes.get());
	}

	@Override
	public Item createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties properties) {
		return new SpawnEggItem(type.get(), backgroundColor, highlightColor, properties);
	}

	@Override
	public void addToCreativeTab(ResourceKey<CreativeModeTab> tab, Supplier<? extends ItemLike> item) {
		ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> entries.accept(item.get()));
	}

	@Override
	public ModLoader loader() {
		return ModLoader.FABRIC;
	}

	@Override
	public String mcVersion() {
		return FabricLoader.getInstance().getRawGameVersion();
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
}
//?}
