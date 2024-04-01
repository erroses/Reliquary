package reliquary.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import reliquary.init.ModEnchantments;
import reliquary.init.ModItems;

import java.util.Set;

public class RandomChanceLootingSeveringCondition implements LootItemCondition {
	public static final Codec<RandomChanceLootingSeveringCondition> CODEC = RecordCodecBuilder.create(
			p_298496_ -> p_298496_.group(
							Codec.FLOAT.fieldOf("chance").forGetter(condition -> condition.chance),
							Codec.FLOAT.fieldOf("looting_multiplier").forGetter(condition -> condition.lootingMultiplier),
							Codec.FLOAT.fieldOf("severing_multiplier").forGetter(condition -> condition.severingMultiplier)
					)
					.apply(p_298496_, RandomChanceLootingSeveringCondition::new)
	);
	final float chance;
	final float lootingMultiplier;
	private final float severingMultiplier;

	RandomChanceLootingSeveringCondition(float chance, float lootingMultiplier, float severingMultiplier) {
		this.chance = chance;
		this.lootingMultiplier = lootingMultiplier;
		this.severingMultiplier = severingMultiplier;
	}

	public LootItemConditionType getType() {
		return ModItems.RANDOM_CHANCE_LOOTING_SEVERING.get();
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.KILLER_ENTITY);
	}

	public boolean test(LootContext lootContext) {
		int i = lootContext.getLootingModifier();
		return lootContext.getRandom().nextFloat() < chance + i * lootingMultiplier + getSeveringModifier(lootContext) * severingMultiplier;
	}

	private int getSeveringModifier(LootContext lootContext) {
		Entity killer = lootContext.getParamOrNull(LootContextParams.KILLER_ENTITY);
		if (!(killer instanceof LivingEntity livingEntity)) {
			return 0;
		}
		Enchantment severingEnchantment = ModEnchantments.SEVERING.get();
		Iterable<ItemStack> iterable = severingEnchantment.getSlotItems(livingEntity).values();
		int severingLevel = 0;

		for (ItemStack itemstack : iterable) {
			int j = EnchantmentHelper.getItemEnchantmentLevel(severingEnchantment, itemstack);
			if (itemstack.getItem() == ModItems.MAGICBANE.get()) {
				j += 2;
			}

			if (j > severingLevel) {
				severingLevel = j;
			}
		}

		return severingLevel;
	}

	public static Builder randomChanceLootingSevering(float percent, float lootingMultiplier, float severingMultiplier) {
		return () -> new RandomChanceLootingSeveringCondition(percent, lootingMultiplier, severingMultiplier);
	}
}
