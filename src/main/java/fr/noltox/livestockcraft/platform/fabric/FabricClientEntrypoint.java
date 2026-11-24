package fr.noltox.livestockcraft.platform.fabric;

//? fabric {

import fr.noltox.livestockcraft.LivestockCraft;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		LivestockCraft.onInitializeClient();
	}

}
//?}
