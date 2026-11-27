package fr.noltox.livestockcraft.platform.fabric;

//? fabric {

import fr.noltox.livestockcraft.LivestockCraft;
import fr.noltox.livestockcraft.client.LivestockCraftClient;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		LivestockCraft.onInitializeClient();
		LivestockCraftClient.registerRenderers(EntityRendererRegistry::register);
	}

}
//?}
