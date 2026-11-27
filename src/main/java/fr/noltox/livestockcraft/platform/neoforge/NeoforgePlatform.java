package fr.noltox.livestockcraft.platform.neoforge;

//? neoforge {

/*import fr.noltox.livestockcraft.LivestockCraft;
import fr.noltox.livestockcraft.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NeoforgePlatform implements Platform {

	private final Map<ResourceKey<?>, DeferredRegister<?>> registers = new LinkedHashMap<>();
	private final List<Consumer<EntityAttributeCreationEvent>> attributeListeners = new ArrayList<>();
	private final List<Consumer<BuildCreativeModeTabContentsEvent>> creativeTabListeners = new ArrayList<>();
	private boolean bound;

	// Hooks everything registered during mod initialization to the mod event bus. Called once, after init.
	public void bind(IEventBus modBus) {
		if (bound) {
			throw new IllegalStateException("NeoforgePlatform is already bound");
		}
		bound = true;
		registers.values().forEach(register -> register.register(modBus));
		modBus.addListener(EntityAttributeCreationEvent.class, event -> attributeListeners.forEach(listener -> listener.accept(event)));
		modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> creativeTabListeners.forEach(listener -> listener.accept(event)));
	}

	private void checkNotBound() {
		if (bound) {
			throw new IllegalStateException("Registration must happen during mod initialization");
		}
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R, T extends R> Supplier<T> register(Registry<R> registry, String name, Supplier<T> factory) {
		checkNotBound();
		DeferredRegister<R> register = (DeferredRegister<R>) registers.computeIfAbsent(
			registry.key(), key -> DeferredRegister.create(registry, LivestockCraft.MOD_ID));
		return register.register(name, factory);
	}

	@Override
	public <T extends LivingEntity> void registerAttributes(Supplier<EntityType<T>> type, Supplier<AttributeSupplier.Builder> attributes) {
		checkNotBound();
		attributeListeners.add(event -> event.put(type.get(), attributes.get().build()));
	}

	@Override
	public Item createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties properties) {
		return new DeferredSpawnEggItem(type, backgroundColor, highlightColor, properties);
	}

	@Override
	public void addToCreativeTab(ResourceKey<CreativeModeTab> tab, Supplier<? extends ItemLike> item) {
		checkNotBound();
		creativeTabListeners.add(event -> {
			if (event.getTabKey() == tab) {
				event.accept(item.get());
			}
		});
	}

	@Override
	public ModLoader loader() {
		return ModLoader.NEOFORGE;
	}

	@Override
	public String mcVersion() {
		return FMLLoader.versionInfo().mcVersion();
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}
}
*///?}
