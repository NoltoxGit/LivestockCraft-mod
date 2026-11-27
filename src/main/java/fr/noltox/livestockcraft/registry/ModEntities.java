package fr.noltox.livestockcraft.registry;

import fr.noltox.livestockcraft.LivestockCraft;
import fr.noltox.livestockcraft.entity.BovineEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public final class ModEntities {

	/** Same size and tracking range as the vanilla cow. */
	public static final Supplier<EntityType<BovineEntity>> BOVINE = LivestockCraft.xplat().register(
		BuiltInRegistries.ENTITY_TYPE, "bovine",
		() -> EntityType.Builder.of(BovineEntity::new, MobCategory.CREATURE)
			.sized(0.9F, 1.4F)
			.eyeHeight(1.3F)
			.passengerAttachments(1.36875F)
			.clientTrackingRange(10)
			.build(LivestockCraft.id("bovine").toString())
	);

	private ModEntities() {
	}

	public static void init() {
		LivestockCraft.xplat().registerAttributes(BOVINE, BovineEntity::createAttributes);
	}
}
