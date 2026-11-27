package fr.noltox.livestockcraft.client.render;

import fr.noltox.livestockcraft.entity.BovineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BovineRenderer extends GeoEntityRenderer<BovineEntity> {

	public BovineRenderer(EntityRendererProvider.Context context) {
		super(context, new BovineModel());
		// Same shadow as the vanilla cow. Babies are scaled down by GeckoLib through the vanilla age scale.
		this.shadowRadius = 0.7F;
	}
}
