package fr.noltox.livestockcraft.registry;

import fr.noltox.livestockcraft.LivestockCraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public final class ModItems {

	/** Spawns a wild Aurochs. Colors are placeholders until the final egg design. */
	public static final Supplier<Item> AUROCHS_SPAWN_EGG = LivestockCraft.xplat().register(
		BuiltInRegistries.ITEM, "aurochs_spawn_egg",
		() -> LivestockCraft.xplat().createSpawnEgg(ModEntities.BOVINE, 0x3B2A1E, 0xC9B08A, new Item.Properties())
	);

	private ModItems() {
	}

	public static void init() {
		LivestockCraft.xplat().addToCreativeTab(CreativeModeTabs.SPAWN_EGGS, AUROCHS_SPAWN_EGG);
	}
}
