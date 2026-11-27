package fr.noltox.livestockcraft.client.render;

import fr.noltox.livestockcraft.LivestockCraft;
import fr.noltox.livestockcraft.entity.BovineEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/** Placeholder: one adult model and one texture for every bovine. The head bone follows the look direction. */
public class BovineModel extends DefaultedEntityGeoModel<BovineEntity> {

	public BovineModel() {
		super(LivestockCraft.id("bovine_adult"), "head");
		withAltAnimations(LivestockCraft.id("bovine"));
		withAltTexture(LivestockCraft.id("bovine/placeholder"));
	}
}
