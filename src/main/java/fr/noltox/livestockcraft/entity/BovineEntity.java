package fr.noltox.livestockcraft.entity;

import fr.noltox.livestockcraft.LivestockCraft;
import fr.noltox.livestockcraft.domestication.BreedPath;
import fr.noltox.livestockcraft.domestication.BuiltinPathRegistry;
import fr.noltox.livestockcraft.domestication.Climate;
import fr.noltox.livestockcraft.domestication.DomesticationMath;
import fr.noltox.livestockcraft.domestication.Family;
import fr.noltox.livestockcraft.domestication.PathRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * The LivestockCraft bovine. One entity type covers every breed: the wild Aurochs and all its domesticated
 * forms differ only by their synced domestication data.
 */
public class BovineEntity extends Animal implements GeoEntity {

	private static final PathRegistry PATHS = BuiltinPathRegistry.INSTANCE;

	private static final EntityDataAccessor<Boolean> DATA_TAMED =
		SynchedEntityData.defineId(BovineEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_RATE =
		SynchedEntityData.defineId(BovineEntity.class, EntityDataSerializers.INT);
	/** Derived from the rate on the server with the server's thresholds, so clients never compute it. */
	private static final EntityDataAccessor<Integer> DATA_STAGE =
		SynchedEntityData.defineId(BovineEntity.class, EntityDataSerializers.INT);
	/** Breed path id, empty when the animal has no path yet. */
	private static final EntityDataAccessor<String> DATA_PATH =
		SynchedEntityData.defineId(BovineEntity.class, EntityDataSerializers.STRING);
	/** Family id, empty when the animal has no family yet. */
	private static final EntityDataAccessor<String> DATA_FAMILY =
		SynchedEntityData.defineId(BovineEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> DATA_CLIMATE =
		SynchedEntityData.defineId(BovineEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<OptionalInt> DATA_LOCK =
		SynchedEntityData.defineId(BovineEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

	private static final String TAG_TAMED = "Tamed";
	private static final String TAG_RATE = "DomesticationRate";
	private static final String TAG_PATH = "BreedPath";
	private static final String TAG_FAMILY = "Family";
	private static final String TAG_CLIMATE = "Climate";
	private static final String TAG_LOCK = "LockedStage";

	private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public BovineEntity(EntityType<? extends BovineEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Cow.createAttributes();
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 2.0));
		goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		goalSelector.addGoal(3, new TemptGoal(this, 1.25, stack -> stack.is(ItemTags.COW_FOOD), false));
		goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_TAMED, false);
		builder.define(DATA_RATE, DomesticationMath.MIN_RATE);
		builder.define(DATA_STAGE, 0);
		builder.define(DATA_PATH, "");
		builder.define(DATA_FAMILY, "");
		builder.define(DATA_CLIMATE, Climate.WARM.id());
		builder.define(DATA_LOCK, OptionalInt.empty());
	}

	// Breeding is not enabled yet: no food starts love mode and no offspring is created.
	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
		return null;
	}

	// --- Domestication data ---

	public boolean isTamed() {
		return entityData.get(DATA_TAMED);
	}

	public boolean isWild() {
		return !isTamed();
	}

	public void setTamed(boolean tamed) {
		entityData.set(DATA_TAMED, tamed);
	}

	public int getRate() {
		return entityData.get(DATA_RATE);
	}

	/** Sets the rate, clamped and rounded down to a whole step, and updates the stage. Server side only. */
	public void setRate(int rate) {
		int stored = DomesticationMath.quantizeDown(DomesticationMath.clampRate(rate), DomesticationMath.STEP);
		entityData.set(DATA_RATE, stored);
		entityData.set(DATA_STAGE, DomesticationMath.stageOf(stored, LivestockCraft.config().domestication().stageThresholds()));
	}

	public int getStage() {
		return entityData.get(DATA_STAGE);
	}

	public Optional<String> getPathId() {
		String id = entityData.get(DATA_PATH);
		return id.isEmpty() ? Optional.empty() : Optional.of(id);
	}

	/** The breed path, empty if the animal has none or its id is unknown. */
	public Optional<BreedPath> getPath() {
		return getPathId().flatMap(PATHS::byId);
	}

	public void setPathId(@Nullable String pathId) {
		entityData.set(DATA_PATH, pathId == null ? "" : pathId);
	}

	public Optional<Family> getFamily() {
		return Family.byId(entityData.get(DATA_FAMILY));
	}

	public void setFamily(@Nullable Family family) {
		entityData.set(DATA_FAMILY, family == null ? "" : family.id());
	}

	public Climate getClimate() {
		return Climate.byId(entityData.get(DATA_CLIMATE)).orElse(Climate.WARM);
	}

	public void setClimate(Climate climate) {
		entityData.set(DATA_CLIMATE, climate.id());
	}

	/** Stage this animal is locked at, if any. */
	public OptionalInt getLock() {
		return entityData.get(DATA_LOCK);
	}

	public void setLock(OptionalInt stage) {
		if (stage.isPresent() && (stage.getAsInt() < 0 || stage.getAsInt() >= DomesticationMath.STAGE_COUNT)) {
			throw new IllegalArgumentException("Lock stage out of range: " + stage.getAsInt());
		}
		entityData.set(DATA_LOCK, stage);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean(TAG_TAMED, isTamed());
		tag.putInt(TAG_RATE, getRate());
		getPathId().ifPresent(id -> tag.putString(TAG_PATH, id));
		getFamily().ifPresent(family -> tag.putString(TAG_FAMILY, family.id()));
		tag.putString(TAG_CLIMATE, getClimate().id());
		getLock().ifPresent(stage -> tag.putInt(TAG_LOCK, stage));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setTamed(tag.getBoolean(TAG_TAMED));
		// Recomputes the stage, so a change of thresholds in the config applies to existing animals.
		setRate(tag.getInt(TAG_RATE));
		setPathId(tag.contains(TAG_PATH, Tag.TAG_STRING) ? tag.getString(TAG_PATH) : null);
		setFamily(Family.byId(tag.getString(TAG_FAMILY)).orElse(null));
		setClimate(Climate.byId(tag.getString(TAG_CLIMATE)).orElse(Climate.WARM));
		int lock = tag.contains(TAG_LOCK, Tag.TAG_INT) ? tag.getInt(TAG_LOCK) : -1;
		setLock(lock >= 0 && lock < DomesticationMath.STAGE_COUNT ? OptionalInt.of(lock) : OptionalInt.empty());
	}

	// --- Sounds (vanilla cow) ---

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.COW_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.COW_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.COW_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	// --- GeckoLib ---

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(DefaultAnimations.genericWalkIdleController(this));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return geoCache;
	}
}
