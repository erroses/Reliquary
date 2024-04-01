package reliquary.items;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import reliquary.init.ModItems;
import reliquary.reference.Config;
import reliquary.util.RegistryHelper;

import java.util.*;

import static reliquary.items.MobCharmDefinition.*;

public class MobCharmRegistry {
	private MobCharmRegistry() {
	}

	private static final Map<String, MobCharmDefinition> REGISTERED_CHARM_DEFINITIONS = new HashMap<>();
	private static final Map<String, MobCharmDefinition> ENTITY_NAME_CHARM_DEFINITIONS = new HashMap<>();
	private static final Set<String> DYNAMICALLY_REGISTERED = new HashSet<>();

	public static void registerMobCharmDefinition(MobCharmDefinition charmDefinition) {
		REGISTERED_CHARM_DEFINITIONS.put(charmDefinition.getRegistryName(), charmDefinition);
		for (String registryName : charmDefinition.getEntities()) {
			ENTITY_NAME_CHARM_DEFINITIONS.put(registryName, charmDefinition);
		}
	}

	static {
		registerMobCharmDefinition(ZOMBIE);
		registerMobCharmDefinition(SKELETON);
		registerMobCharmDefinition(WITHER_SKELETON);
		registerMobCharmDefinition(CREEPER);
		registerMobCharmDefinition(WITCH);
		registerMobCharmDefinition(ZOMBIFIED_PIGLIN);
		registerMobCharmDefinition(CAVE_SPIDER);
		registerMobCharmDefinition(SPIDER);
		registerMobCharmDefinition(ENDERMAN);
		registerMobCharmDefinition(GHAST);
		registerMobCharmDefinition(SLIME);
		registerMobCharmDefinition(MAGMA_CUBE);
		registerMobCharmDefinition(BLAZE);
		registerMobCharmDefinition(GUARDIAN);
		registerMobCharmDefinition(PIGLIN);
		registerMobCharmDefinition(PIGLIN_BRUTE);
		registerMobCharmDefinition(HOGLIN);
	}

	static Optional<MobCharmDefinition> getCharmDefinitionFor(Entity entity) {
		return Optional.ofNullable(ENTITY_NAME_CHARM_DEFINITIONS.get(RegistryHelper.getRegistryName(entity).toString()));
	}

	public static Optional<MobCharmDefinition> getCharmDefinitionFor(ItemStack stack) {
		if (stack.getItem() != ModItems.MOB_CHARM.get()) {
			return Optional.empty();
		}

		return Optional.ofNullable(ENTITY_NAME_CHARM_DEFINITIONS.get(MobCharmItem.getEntityRegistryName(stack)));
	}

	public static Set<String> getRegisteredNames() {
		return REGISTERED_CHARM_DEFINITIONS.keySet();
	}

	public static void registerDynamicCharmDefinitions() {
		for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
			String registryName = RegistryHelper.getRegistryName(entityType).toString();
			Set<String> blockedEntities = new HashSet<>(Config.COMMON.items.mobCharm.entityBlockList.get());
			if (!ENTITY_NAME_CHARM_DEFINITIONS.containsKey(registryName) && entityType.getCategory() == MobCategory.MONSTER && !blockedEntities.contains(registryName)) {
				registerMobCharmDefinition(new MobCharmDefinition(registryName));
				DYNAMICALLY_REGISTERED.add(registryName);
			}
		}
	}

	public static void handleAddingFragmentDrops(LivingDropsEvent evt) {
		if (!evt.getSource().getMsgId().equals("player")) {
			return;
		}

		LivingEntity entity = evt.getEntity();
		ResourceLocation regName = RegistryHelper.getRegistryName(entity);
		if (!DYNAMICALLY_REGISTERED.contains(regName.toString())) {
			return;
		}

		double dynamicDropChance = Config.COMMON.items.mobCharmFragment.dropChance.get() + evt.getLootingLevel() * Config.COMMON.items.mobCharmFragment.lootingMultiplier.get();

		if (entity.level().random.nextFloat() < dynamicDropChance) {
			ItemEntity fragmentItemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), ModItems.MOB_CHARM_FRAGMENT.get().getStackFor(regName.toString()));
			fragmentItemEntity.setDefaultPickUpDelay();

			evt.getDrops().add(fragmentItemEntity);
		}
	}
}
