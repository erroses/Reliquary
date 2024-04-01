package reliquary.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import reliquary.entities.*;
import reliquary.entities.potion.AphroditePotionEntity;
import reliquary.entities.potion.FertilePotionEntity;
import reliquary.entities.potion.ThrownXRPotionEntity;
import reliquary.entities.shot.*;
import reliquary.reference.Reference;

import java.util.function.Supplier;

public class ModEntities {
	private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Reference.MOD_ID);

	public static final Supplier<EntityType<AphroditePotionEntity>> APHRODITE_POTION = ENTITY_TYPES.register("aphrodite_potion", () -> getDefaultSizeEntityType(AphroditePotionEntity::new));
	public static final Supplier<EntityType<FertilePotionEntity>> FERTILE_POTION = ENTITY_TYPES.register("fertile_potion", () -> getDefaultSizeEntityType(FertilePotionEntity::new));
	public static final Supplier<EntityType<ThrownXRPotionEntity>> THROWN_POTION = ENTITY_TYPES.register("thrown_potion", () -> getDefaultSizeEntityType(ThrownXRPotionEntity::new));
	public static final Supplier<EntityType<BlazeShotEntity>> BLAZE_SHOT = ENTITY_TYPES.register("blaze_shot", () -> getShotEntityType(BlazeShotEntity::new));
	public static final Supplier<EntityType<BusterShotEntity>> BUSTER_SHOT = ENTITY_TYPES.register("buster_shot", () -> getShotEntityType(BusterShotEntity::new));
	public static final Supplier<EntityType<ConcussiveShotEntity>> CONCUSSIVE_SHOT = ENTITY_TYPES.register("concussive_shot", () -> getShotEntityType(ConcussiveShotEntity::new));
	public static final Supplier<EntityType<EnderShotEntity>> ENDER_SHOT = ENTITY_TYPES.register("ender_shot", () -> getShotEntityType(EnderShotEntity::new));
	public static final Supplier<EntityType<ExorcismShotEntity>> EXORCISM_SHOT = ENTITY_TYPES.register("exorcism_shot", () -> getShotEntityType(ExorcismShotEntity::new));
	public static final Supplier<EntityType<NeutralShotEntity>> NEUTRAL_SHOT = ENTITY_TYPES.register("neutral_shot", () -> getShotEntityType(NeutralShotEntity::new));
	public static final Supplier<EntityType<SandShotEntity>> SAND_SHOT = ENTITY_TYPES.register("sand_shot", () -> getShotEntityType(SandShotEntity::new));
	public static final Supplier<EntityType<SeekerShotEntity>> SEEKER_SHOT = ENTITY_TYPES.register("seeker_shot", () -> getShotEntityType(SeekerShotEntity::new));
	public static final Supplier<EntityType<StormShotEntity>> STORM_SHOT = ENTITY_TYPES.register("storm_shot", () -> getShotEntityType(StormShotEntity::new));
	public static final Supplier<EntityType<EnderStaffProjectileEntity>> ENDER_STAFF_PROJECTILE = ENTITY_TYPES.register("ender_staff_projectile", () -> getEntityType(EnderStaffProjectileEntity::new, 0.25F, 0.25F, 256));
	public static final Supplier<EntityType<GlowingWaterEntity>> GLOWING_WATER = ENTITY_TYPES.register("glowing_water", () -> getDefaultSizeEntityType(GlowingWaterEntity::new));
	public static final Supplier<EntityType<HolyHandGrenadeEntity>> HOLY_HAND_GRENADE = ENTITY_TYPES.register("holy_hand_grenade", () -> getDefaultSizeEntityType(HolyHandGrenadeEntity::new));
	public static final Supplier<EntityType<KrakenSlimeEntity>> KRAKEN_SLIME = ENTITY_TYPES.register("kraken_slime", () -> getDefaultSizeEntityType(KrakenSlimeEntity::new));
	public static final Supplier<EntityType<LyssaHook>> LYSSA_HOOK = ENTITY_TYPES.register("lyssa_hook", () -> getDefaultSizeEntityType(LyssaHook::new));
	public static final Supplier<EntityType<XRTippedArrowEntity>> TIPPED_ARROW = ENTITY_TYPES.register("tipped_arrow", () -> getDefaultSizeEntityType(XRTippedArrowEntity::new));
	public static final Supplier<EntityType<SpecialSnowballEntity>> SPECIAL_SNOWBALL = ENTITY_TYPES.register("special_snowball", () -> getEntityType(SpecialSnowballEntity::new, 0.01F, 0.01F));

	public static final ResourceKey<DamageType> BULLET_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Reference.MOD_ID, "bullet"));

	private ModEntities() {}

	public static void registerListeners(IEventBus modBus) {
		ENTITY_TYPES.register(modBus);
	}

	private static <T extends Entity> EntityType<T> getDefaultSizeEntityType(EntityType.EntityFactory<T> factory) {
		return getEntityType(factory, 0.25F, 0.25F);
	}

	private static <T extends ShotEntityBase> EntityType<T> getShotEntityType(EntityType.EntityFactory<T> factory) {
		return getEntityType(factory, 0.01F, 0.01F);
	}

	private static <T extends Entity> EntityType<T> getEntityType(EntityType.EntityFactory<T> factory, float width, float height) {
		return getEntityType(factory, width, height, 128);
	}

	private static <T extends Entity> EntityType<T> getEntityType(EntityType.EntityFactory<T> factory, float width, float height, int trackingRange) {
		return EntityType.Builder.of(factory, MobCategory.MISC)
				.sized(width, height).updateInterval(5).setTrackingRange(trackingRange).setShouldReceiveVelocityUpdates(true)
				.build("");
	}
}
