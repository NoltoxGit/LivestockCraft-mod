package fr.noltox.livestockcraft.client;

import fr.noltox.livestockcraft.client.render.BovineRenderer;
import fr.noltox.livestockcraft.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** Client-only setup shared by both loaders. Never referenced from common code. */
public final class LivestockCraftClient {

	private LivestockCraftClient() {
	}

	/** Loader hook for entity renderer registration. */
	public interface RendererRegistrar {
		<T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
	}

	public static void registerRenderers(RendererRegistrar registrar) {
		registrar.register(ModEntities.BOVINE.get(), BovineRenderer::new);
	}
}
