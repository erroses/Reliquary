package reliquary.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import reliquary.common.gui.AlkahestTomeMenu;
import reliquary.common.gui.MobCharmBeltMenu;
import reliquary.crafting.*;
import reliquary.crafting.conditions.*;
import reliquary.data.ChestLootEnabledCondition;
import reliquary.data.EntityLootEnabledCondition;
import reliquary.data.RandomChanceLootingSeveringCondition;
import reliquary.data.ReliquaryLootModifierProvider;
import reliquary.entities.GlowingWaterEntity;
import reliquary.entities.HolyHandGrenadeEntity;
import reliquary.entities.XRTippedArrowEntity;
import reliquary.entities.potion.AphroditePotionEntity;
import reliquary.entities.potion.FertilePotionEntity;
import reliquary.entities.potion.ThrownXRPotionEntity;
import reliquary.entities.shot.*;
import reliquary.items.PotionItem;
import reliquary.items.TippedArrowItem;
import reliquary.items.*;
import reliquary.items.util.*;
import reliquary.items.util.fluid.FluidHandlerEmperorChalice;
import reliquary.items.util.fluid.FluidHandlerHeroMedallion;
import reliquary.items.util.fluid.FluidHandlerInfernalChalice;
import reliquary.reference.Colors;
import reliquary.reference.Config;
import reliquary.reference.Reference;
import reliquary.util.RegistryHelper;

import java.util.function.Supplier;

public class ModItems {
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Reference.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB.location(), Reference.MOD_ID);
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Reference.MOD_ID);
	private static final DeferredRegister<Codec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Reference.MOD_ID);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Reference.MOD_ID);
	public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE.location(), Reference.MOD_ID);
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reference.MOD_ID);

	public static final Supplier<AlkahestryTomeItem> ALKAHESTRY_TOME = ITEMS.register("alkahestry_tome", AlkahestryTomeItem::new);
	public static final Supplier<MercyCrossItem> MERCY_CROSS = ITEMS.register("mercy_cross", MercyCrossItem::new);
	public static final Supplier<AngelheartVialItem> ANGELHEART_VIAL = ITEMS.register("angelheart_vial", AngelheartVialItem::new);
	public static final Supplier<AngelicFeatherItem> ANGELIC_FEATHER = ITEMS.register("angelic_feather", AngelicFeatherItem::new);
	public static final Supplier<AphroditePotionItem> APHRODITE_POTION = ITEMS.register("aphrodite_potion", AphroditePotionItem::new);
	public static final Supplier<PotionEssenceItem> POTION_ESSENCE = ITEMS.register("potion_essence", PotionEssenceItem::new);
	public static final Supplier<DestructionCatalystItem> DESTRUCTION_CATALYST = ITEMS.register("destruction_catalyst", DestructionCatalystItem::new);
	public static final Supplier<EmperorChaliceItem> EMPEROR_CHALICE = ITEMS.register("emperor_chalice", EmperorChaliceItem::new);
	public static final Supplier<EnderStaffItem> ENDER_STAFF = ITEMS.register("ender_staff", EnderStaffItem::new);
	public static final Supplier<FertilePotionItem> FERTILE_POTION = ITEMS.register("fertile_potion", FertilePotionItem::new);
	public static final Supplier<FortuneCoinItem> FORTUNE_COIN = ITEMS.register("fortune_coin", FortuneCoinItem::new);
	public static final Supplier<GlacialStaffItem> GLACIAL_STAFF = ITEMS.register("glacial_staff", GlacialStaffItem::new);
	public static final Supplier<GlowingWaterItem> GLOWING_WATER = ITEMS.register("glowing_water", GlowingWaterItem::new);
	public static final Supplier<HolyHandGrenadeItem> HOLY_HAND_GRENADE = ITEMS.register("holy_hand_grenade", HolyHandGrenadeItem::new);
	public static final Supplier<HandgunItem> HANDGUN = ITEMS.register("handgun", HandgunItem::new);
	public static final Supplier<ItemBase> GRIP_ASSEMBLY = ITEMS.register("grip_assembly", () -> new ItemBase(new Item.Properties().stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<ItemBase> BARREL_ASSEMBLY = ITEMS.register("barrel_assembly", () -> new ItemBase(new Item.Properties().stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<ItemBase> HAMMER_ASSEMBLY = ITEMS.register("hammer_assembly", () -> new ItemBase(new Item.Properties().stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<HarvestRodItem> HARVEST_ROD = ITEMS.register("harvest_rod", HarvestRodItem::new);
	public static final Supplier<MobCharmFragmentItem> MOB_CHARM_FRAGMENT = ITEMS.register("mob_charm_fragment", MobCharmFragmentItem::new);
	public static final Supplier<HeroMedallionItem> HERO_MEDALLION = ITEMS.register("hero_medallion", HeroMedallionItem::new);
	public static final Supplier<IceMagusRodItem> ICE_MAGUS_ROD = ITEMS.register("ice_magus_rod", IceMagusRodItem::new);
	public static final Supplier<InfernalChaliceItem> INFERNAL_CHALICE = ITEMS.register("infernal_chalice", InfernalChaliceItem::new);
	public static final Supplier<InfernalClawsItem> INFERNAL_CLAWS = ITEMS.register("infernal_claws", InfernalClawsItem::new);
	public static final Supplier<InfernalTearItem> INFERNAL_TEAR = ITEMS.register("infernal_tear", InfernalTearItem::new);
	public static final Supplier<KrakenShellItem> KRAKEN_SHELL = ITEMS.register("kraken_shell", KrakenShellItem::new);
	public static final Supplier<MidasTouchstoneItem> MIDAS_TOUCHSTONE = ITEMS.register("midas_touchstone", MidasTouchstoneItem::new);
	public static final Supplier<PhoenixDownItem> PHOENIX_DOWN = ITEMS.register("phoenix_down", PhoenixDownItem::new);
	public static final Supplier<PyromancerStaffItem> PYROMANCER_STAFF = ITEMS.register("pyromancer_staff", PyromancerStaffItem::new);
	public static final Supplier<RendingGaleItem> RENDING_GALE = ITEMS.register("rending_gale", RendingGaleItem::new);
	public static final Supplier<RodOfLyssaItem> ROD_OF_LYSSA = ITEMS.register("rod_of_lyssa", RodOfLyssaItem::new);
	public static final Supplier<SojournerStaffItem> SOJOURNER_STAFF = ITEMS.register("sojourner_staff", SojournerStaffItem::new);
	public static final Supplier<TippedArrowItem> TIPPED_ARROW = ITEMS.register("tipped_arrow", TippedArrowItem::new);
	public static final Supplier<VoidTearItem> VOID_TEAR = ITEMS.register("void_tear", VoidTearItem::new);
	public static final Supplier<WitchHatItem> WITCH_HAT = ITEMS.register("witch_hat", WitchHatItem::new);
	public static final Supplier<WitherlessRoseItem> WITHERLESS_ROSE = ITEMS.register("witherless_rose", WitherlessRoseItem::new);
	public static final Supplier<ItemBase> EMPTY_POTION_VIAL = ITEMS.register("empty_potion_vial", () -> new ItemBase(Config.COMMON.disable.disablePotions));
	public static final Supplier<PotionItemBase> POTION = ITEMS.register("potion", PotionItem::new);
	public static final Supplier<PotionItemBase> SPLASH_POTION = ITEMS.register("splash_potion", ThrownPotionItem::new);
	public static final Supplier<PotionItemBase> LINGERING_POTION = ITEMS.register("lingering_potion", ThrownPotionItem::new);
	public static final Supplier<MobCharmBeltItem> MOB_CHARM_BELT = ITEMS.register("mob_charm_belt", MobCharmBeltItem::new);
	public static final Supplier<MobCharmItem> MOB_CHARM = ITEMS.register("mob_charm", MobCharmItem::new);
	public static final Supplier<MagazineItem> EMPTY_MAGAZINE = ITEMS.register("magazines/empty_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.DARKEST, 16)));
	public static final Supplier<MagazineItem> NEUTRAL_MAGAZINE = ITEMS.register("magazines/neutral_magazine", () ->
			new MagazineItem(true, Integer.parseInt(Colors.NEUTRAL_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> EXORCISM_MAGAZINE = ITEMS.register("magazines/exorcism_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.EXORCISM_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> BLAZE_MAGAZINE = ITEMS.register("magazines/blaze_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.BLAZE_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> ENDER_MAGAZINE = ITEMS.register("magazines/ender_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.ENDER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> CONCUSSIVE_MAGAZINE = ITEMS.register("magazines/concussive_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.CONCUSSIVE_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> BUSTER_MAGAZINE = ITEMS.register("magazines/buster_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.BUSTER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> SEEKER_MAGAZINE = ITEMS.register("magazines/seeker_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.SEEKER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> SAND_MAGAZINE = ITEMS.register("magazines/sand_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.SAND_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> STORM_MAGAZINE = ITEMS.register("magazines/storm_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.STORM_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> EMPTY_BULLET = ITEMS.register("bullets/empty_bullet", () ->
			new BulletItem(false, false, Integer.parseInt(Colors.DARKEST, 16)));
	public static final Supplier<BulletItem> NEUTRAL_BULLET = ITEMS.register("bullets/neutral_bullet", () ->
			new BulletItem(false, true, Integer.parseInt(Colors.NEUTRAL_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> EXORCISM_BULLET = ITEMS.register("bullets/exorcism_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.EXORCISM_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> BLAZE_BULLET = ITEMS.register("bullets/blaze_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.BLAZE_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> ENDER_BULLET = ITEMS.register("bullets/ender_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.ENDER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> CONCUSSIVE_BULLET = ITEMS.register("bullets/concussive_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.CONCUSSIVE_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> BUSTER_BULLET = ITEMS.register("bullets/buster_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.BUSTER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> SEEKER_BULLET = ITEMS.register("bullets/seeker_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.SEEKER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> SAND_BULLET = ITEMS.register("bullets/sand_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.SAND_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> STORM_BULLET = ITEMS.register("bullets/storm_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.STORM_SHOT_COLOR, 16)));
	public static final Supplier<ItemBase> ZOMBIE_HEART = ITEMS.register("zombie_heart", MobDropItem::new);
	public static final Supplier<ItemBase> SQUID_BEAK = ITEMS.register("squid_beak", MobDropItem::new);
	public static final Supplier<ItemBase> RIB_BONE = ITEMS.register("rib_bone", MobDropItem::new);
	public static final Supplier<ItemBase> CATALYZING_GLAND = ITEMS.register("catalyzing_gland", MobDropItem::new);
	public static final Supplier<ItemBase> CHELICERAE = ITEMS.register("chelicerae", MobDropItem::new);
	public static final Supplier<ItemBase> SLIME_PEARL = ITEMS.register("slime_pearl", MobDropItem::new);
	public static final Supplier<ItemBase> KRAKEN_SHELL_FRAGMENT = ITEMS.register("kraken_shell_fragment", () -> new ItemBase());
	public static final Supplier<ItemBase> BAT_WING = ITEMS.register("bat_wing", MobDropItem::new);
	public static final Supplier<ItemBase> WITHERED_RIB = ITEMS.register("withered_rib", MobDropItem::new);
	public static final Supplier<ItemBase> MOLTEN_CORE = ITEMS.register("molten_core", MobDropItem::new);
	public static final Supplier<ItemBase> EYE_OF_THE_STORM = ITEMS.register("eye_of_the_storm", MobDropItem::new);
	public static final Supplier<ItemBase> FERTILE_ESSENCE = ITEMS.register("fertile_essence", () -> new ItemBase());
	public static final Supplier<ItemBase> FROZEN_CORE = ITEMS.register("frozen_core", MobDropItem::new);
	public static final Supplier<ItemBase> NEBULOUS_HEART = ITEMS.register("nebulous_heart", MobDropItem::new);
	public static final Supplier<ItemBase> INFERNAL_CLAW = ITEMS.register("infernal_claw", () -> new ItemBase());
	public static final Supplier<ItemBase> GUARDIAN_SPIKE = ITEMS.register("guardian_spike", MobDropItem::new);
	public static final Supplier<ItemBase> CRIMSON_CLOTH = ITEMS.register("crimson_cloth", () -> new ItemBase());
	public static final Supplier<LanternOfParanoiaItem> LANTERN_OF_PARANOIA = ITEMS.register("lantern_of_paranoia", LanternOfParanoiaItem::new);
	public static final Supplier<MagicbaneItem> MAGICBANE = ITEMS.register("magicbane", MagicbaneItem::new);
	public static final Supplier<SalamanderEyeItem> SALAMANDER_EYE = ITEMS.register("salamander_eye", SalamanderEyeItem::new);
	public static final Supplier<SerpentStaffItem> SERPENT_STAFF = ITEMS.register("serpent_staff", SerpentStaffItem::new);
	public static final Supplier<ShearsOfWinterItem> SHEARS_OF_WINTER = ITEMS.register("shears_of_winter", ShearsOfWinterItem::new);
	public static final Supplier<TwilightCloakItem> TWILIGHT_CLOAK = ITEMS.register("twilight_cloak", TwilightCloakItem::new);
	public static final Supplier<ItemBase> GLOWING_BREAD = ITEMS.register("glowing_bread", () ->
			new ItemBase(new Item.Properties().rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(20).saturationMod(1F).fast().build())));

	public static final Supplier<MenuType<AlkahestTomeMenu>> ALKAHEST_TOME_MENU_TYPE = MENU_TYPES.register("alkahest_tome",
			() -> IMenuTypeExtension.create((windowId, inv, data) -> AlkahestTomeMenu.fromBuffer(windowId)));

	public static final Supplier<MenuType<MobCharmBeltMenu>> MOB_CHAR_BELT_MENU_TYPE = MENU_TYPES.register("mob_char_belt",
			() -> IMenuTypeExtension.create(MobCharmBeltMenu::fromBuffer));


	public static final Supplier<Codec<AlkahestryEnabledCondition>> ALKAHESTRY_ENABLED_CONDITION = CONDITION_CODECS.register("alkahestry_enabled", () -> AlkahestryEnabledCondition.CODEC);
	public static final Supplier<Codec<MobDropsCraftableCondition>> MOB_DROPS_CRAFTABLE_CONDITION = CONDITION_CODECS.register("mob_drops_craftable", () -> MobDropsCraftableCondition.CODEC);
	public static final Supplier<Codec<HandgunEnabledCondition>> HANDGUN_ENABLED_CONDITION = CONDITION_CODECS.register("handgun_enabled", () -> HandgunEnabledCondition.CODEC);
	public static final Supplier<Codec<PotionsEnabledCondition>> POTIONS_ENABLED_CONDITION = CONDITION_CODECS.register("potions_enabled", () -> PotionsEnabledCondition.CODEC);
	public static final Supplier<Codec<PassivePedestalEnabledCondition>> PASSIVE_PEDESTAL_ENABLED_CONDITION = CONDITION_CODECS.register("passive_pedestal_enabled", () -> PassivePedestalEnabledCondition.CODEC);
	public static final Supplier<Codec<PedestalEnabledCondition>> PEDESTAL_ENABLED_CONDITION = CONDITION_CODECS.register("pedestal_enabled", () -> PedestalEnabledCondition.CODEC);
	public static final Supplier<Codec<SpawnEggEnabledCondition>> SPAWN_EGG_ENABLED_CONDITION = CONDITION_CODECS.register("spawn_egg_enabled", () -> SpawnEggEnabledCondition.CODEC);

	public static final Supplier<RecipeSerializer<?>> MOB_CHARM_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("mob_charm", MobCharmRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> FRAGMENT_TO_SPAWN_EGG_SERIALIZER = RECIPE_SERIALIZERS.register("fragment_to_spawn_egg", FragmentToSpawnEggRecipe.Serializer::new);
	public static final Supplier<SimpleCraftingRecipeSerializer<?>> MOB_CHARM_REPAIR_SERIALIZER = RECIPE_SERIALIZERS.register("mob_charm_repair", () -> new SimpleCraftingRecipeSerializer<>(MobCharmRepairRecipe::new));
	public static final Supplier<RecipeSerializer<?>> ALKAHESTRY_CHARGING_SERIALIZER = RECIPE_SERIALIZERS.register("alkahestry_charging", AlkahestryChargingRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> ALKAHESTRY_CRAFTING_SERIALIZER = RECIPE_SERIALIZERS.register("alkahestry_crafting", AlkahestryCraftingRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> ALKAHESTRY_DRAIN_SERIALIZER = RECIPE_SERIALIZERS.register("alkahestry_drain", AlkahestryDrainRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> POTION_EFFECTS_SERIALIZER = RECIPE_SERIALIZERS.register("potion_effects", PotionEffectsRecipe.Serializer::new);
	public static final Supplier<LootItemConditionType> CHEST_LOOT_ENABLED_CONDITION = LOOT_CONDITION_TYPES.register("chest_loot_enabled", () -> new LootItemConditionType(ChestLootEnabledCondition.CODEC));
	public static final Supplier<LootItemConditionType> ENTITY_LOOT_ENABLED_CONDITION = LOOT_CONDITION_TYPES.register("entity_loot_enabled", () -> new LootItemConditionType(EntityLootEnabledCondition.CODEC));
	public static final Supplier<LootItemConditionType> RANDOM_CHANCE_LOOTING_SEVERING = LOOT_CONDITION_TYPES.register("random_chance_looting_severing", () -> new LootItemConditionType(RandomChanceLootingSeveringCondition.CODEC));
	public static final Supplier<Codec<ReliquaryLootModifierProvider.InjectLootModifier>> INJECT_LOOT = LOOT_MODIFIERS.register("inject_loot", () -> ReliquaryLootModifierProvider.InjectLootModifier.CODEC);

	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Reference.MOD_ID);
	private static final Supplier<AttachmentType<HarvestRodItemStackHandler>> HARVEST_ROD_ITEM_STACK_HANDLER = ATTACHMENT_TYPES.register(
			"harvest_rod_handler", () -> AttachmentType.serializable(HarvestRodItemStackHandler::new).build());
	private static final Supplier<AttachmentType<VoidTearItemStackHandler>> VOID_TEAR_ITEM_STACK_HANDLER = ATTACHMENT_TYPES.register(
			"void_tear_handler", () -> AttachmentType.serializable(VoidTearItemStackHandler::new).build());
	private static final Supplier<AttachmentType<FilteredItemStackHandler>> ENDER_STAFF_ITEM_STACK_HANDLER = ATTACHMENT_TYPES.register(
			"ender_staff_handler", () -> AttachmentType.serializable(() -> new FilteredItemStackHandler(new FilteredItemStack(Items.ENDER_PEARL, Config.COMMON.items.enderStaff.enderPearlLimit.get(), Config.COMMON.items.enderStaff.enderPearlWorth.get(), false))).build());
	private static final Supplier<AttachmentType<FilteredItemStackHandler>> RENDING_GALE_ITEM_STACK_HANDLER = ATTACHMENT_TYPES.register(
			"rending_gale_handler", () -> AttachmentType.serializable(() -> new FilteredItemStackHandler(new FilteredItemStack(Items.FEATHER, Config.COMMON.items.rendingGale.chargeLimit.get(), Config.COMMON.items.rendingGale.chargeFeatherWorth.get(), false))).build());
	public static final Supplier<AttachmentType<HarvestRodCache>> HARVEST_ROD_CACHE = ATTACHMENT_TYPES.register("harvest_rod_cache", () -> AttachmentType.builder(HarvestRodCache::new).build());
	public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () ->
			CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MERCY_CROSS.get()))
					.title(Component.translatable("itemGroup.reliquary"))
					.displayItems((featureFlags, output) -> {
								ITEMS.getEntries().stream().filter(i -> i.get() instanceof ICreativeTabItemGenerator)
										.forEach(i -> ((ICreativeTabItemGenerator) i.get()).addCreativeTabItems(output::accept));
								ModBlocks.ITEMS.getEntries().stream().filter(i -> i.get() instanceof ICreativeTabItemGenerator)
										.forEach(i -> ((ICreativeTabItemGenerator) i.get()).addCreativeTabItems(output::accept));
							}
					).build());

	public static void registerContainers(RegisterEvent event) {
		if (!event.getRegistryKey().equals(Registries.MENU)) {
			return;
		}

		if (FMLEnvironment.dist.isClient()) {
			ModItemsClient.registerMenuScreens();
		}
	}

	public static void registerDispenseBehaviors() {
		if (Boolean.FALSE.equals(Config.COMMON.disable.disablePotions.get())) {
			DispenserBlock.registerBehavior(ModItems.SPLASH_POTION.get(), new BehaviorDefaultProjectileDispense() {
				@Override
				ProjectileEntityFactory getProjectileEntityFactory() {
					return (world, position, stack) -> new ThrownXRPotionEntity(world, position.x(), position.y(), position.z(), stack);
				}
			});

			DispenserBlock.registerBehavior(ModItems.LINGERING_POTION.get(), new BehaviorDefaultProjectileDispense() {
				@Override
				ProjectileEntityFactory getProjectileEntityFactory() {
					return (world, position, stack) -> new ThrownXRPotionEntity(world, position.x(), position.y(), position.z(), stack);
				}
			});

			DispenserBlock.registerBehavior(ModItems.APHRODITE_POTION.get(), new BehaviorDefaultProjectileDispense() {
				@Override
				ProjectileEntityFactory getProjectileEntityFactory() {
					return (world, position, stack) -> new AphroditePotionEntity(world, position.x(), position.y(), position.z());
				}
			});

			DispenserBlock.registerBehavior(ModItems.FERTILE_POTION.get(), new BehaviorDefaultProjectileDispense() {
				@Override
				ProjectileEntityFactory getProjectileEntityFactory() {
					return (world, position, stack) -> new FertilePotionEntity(world, position.x(), position.y(), position.z());
				}
			});

			DispenserBlock.registerBehavior(ModItems.TIPPED_ARROW.get(), new AbstractProjectileDispenseBehavior() {
				@Override
				protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
					XRTippedArrowEntity entitytippedarrow = new XRTippedArrowEntity(world, position.x(), position.y(), position.z(), stack);
					entitytippedarrow.setPotionEffect(stack);
					entitytippedarrow.pickup = AbstractArrow.Pickup.ALLOWED;
					return entitytippedarrow;
				}
			});
		}
		DispenserBlock.registerBehavior(ModItems.GLOWING_WATER.get(), new BehaviorDefaultProjectileDispense() {
			@Override
			ProjectileEntityFactory getProjectileEntityFactory() {
				return (world, position, stack) -> new GlowingWaterEntity(world, position.x(), position.y(), position.z());
			}
		});

		DispenserBlock.registerBehavior(ModItems.HOLY_HAND_GRENADE.get(), new BehaviorDefaultProjectileDispense() {
			@Override
			ProjectileEntityFactory getProjectileEntityFactory() {
				return (world, position, stack) -> new HolyHandGrenadeEntity(world, position.x(), position.y(), position.z());
			}
		});
	}

	public static void registerHandgunMagazines() {
		HandgunItem handgun = HANDGUN.get();
		handgun.registerMagazine(RegistryHelper.getRegistryName(NEUTRAL_MAGAZINE.get()).toString(), NeutralShotEntity::new, NEUTRAL_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(EXORCISM_MAGAZINE.get()).toString(), ExorcismShotEntity::new, EXORCISM_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(BLAZE_MAGAZINE.get()).toString(), BlazeShotEntity::new, BLAZE_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(ENDER_MAGAZINE.get()).toString(), EnderShotEntity::new, ENDER_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(CONCUSSIVE_MAGAZINE.get()).toString(), ConcussiveShotEntity::new, CONCUSSIVE_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(BUSTER_MAGAZINE.get()).toString(), BusterShotEntity::new, BUSTER_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(SEEKER_MAGAZINE.get()).toString(), SeekerShotEntity::new, SEEKER_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(SAND_MAGAZINE.get()).toString(), SandShotEntity::new, SAND_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(STORM_MAGAZINE.get()).toString(), StormShotEntity::new, STORM_BULLET);
	}

	public static void registerListeners(IEventBus modBus) {
		ITEMS.register(modBus);
		MENU_TYPES.register(modBus);
		RECIPE_SERIALIZERS.register(modBus);
		CONDITION_CODECS.register(modBus);
		LOOT_CONDITION_TYPES.register(modBus);
		LOOT_MODIFIERS.register(modBus);
		CREATIVE_MODE_TABS.register(modBus);
		ATTACHMENT_TYPES.register(modBus);
		modBus.addListener(ModItems::registerContainers);
		modBus.addListener(ModItems::registerCapabilities);
		NeoForge.EVENT_BUS.addListener(ModItems::onResourceReload);
	}

	private abstract static class BehaviorDefaultProjectileDispense implements DispenseItemBehavior {
		abstract ProjectileEntityFactory getProjectileEntityFactory();

		@Override
		public ItemStack dispense(BlockSource source, ItemStack stack) {
			return (new AbstractProjectileDispenseBehavior() {

				@Override
				protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
					return getProjectileEntityFactory().createProjectileEntity(world, position, stack);
				}

				@Override
				protected float getUncertainty() {
					return super.getUncertainty() * 0.5F;
				}

				@Override
				protected float getPower() {
					return super.getPower() * 1.25F;
				}
			}).dispense(source, stack);
		}
	}

	private static void onResourceReload(AddReloadListenerEvent event) {
		MobCharmRecipe.REGISTERED_RECIPES.clear();
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(Capabilities.FluidHandler.ITEM, (itemStack, context) -> new FluidHandlerHeroMedallion(itemStack)
				, HERO_MEDALLION.get());
		event.registerItem(Capabilities.ItemHandler.ITEM, (itemStack, context) -> itemStack.getData(VOID_TEAR_ITEM_STACK_HANDLER), VOID_TEAR.get());
		event.registerItem(Capabilities.ItemHandler.ITEM, (itemStack, context) -> itemStack.getData(HARVEST_ROD_ITEM_STACK_HANDLER), HARVEST_ROD.get());
		event.registerItem(Capabilities.ItemHandler.ITEM, (itemStack, context) -> itemStack.getData(ENDER_STAFF_ITEM_STACK_HANDLER), ENDER_STAFF.get());
		event.registerItem(Capabilities.ItemHandler.ITEM, (itemStack, context) -> itemStack.getData(RENDING_GALE_ITEM_STACK_HANDLER), RENDING_GALE.get());
		event.registerItem(Capabilities.FluidHandler.ITEM, (itemStack, context) -> new FluidHandlerEmperorChalice(itemStack), EMPEROR_CHALICE.get());
		event.registerItem(Capabilities.FluidHandler.ITEM, (itemStack, context) -> new FluidHandlerHeroMedallion(itemStack), HERO_MEDALLION.get());
		event.registerItem(Capabilities.FluidHandler.ITEM, (itemStack, context) -> new FluidHandlerInfernalChalice(itemStack), INFERNAL_CHALICE.get());
	}

	private interface ProjectileEntityFactory {
		Projectile createProjectileEntity(Level world, Position position, ItemStack stack);
	}
}
