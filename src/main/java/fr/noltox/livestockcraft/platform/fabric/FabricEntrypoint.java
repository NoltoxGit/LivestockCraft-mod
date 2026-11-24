package fr.noltox.livestockcraft.platform.fabric;

//? fabric {

import fr.noltox.livestockcraft.LivestockCraft;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ModInitializer;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		LivestockCraft.onInitialize();
	}
}
//?}
