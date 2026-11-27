package fr.noltox.livestockcraft.platform.neoforge;

//? neoforge {

/*import fr.noltox.livestockcraft.LivestockCraft;
import fr.noltox.livestockcraft.client.LivestockCraftClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = LivestockCraft.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		LivestockCraft.onInitializeClient();
	}

	@SubscribeEvent
	public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
		LivestockCraftClient.registerRenderers(event::registerEntityRenderer);
	}
}
*///?}
