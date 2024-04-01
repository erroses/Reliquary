package reliquary.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import reliquary.reference.Reference;

import java.util.function.Supplier;

public class ModEnchantments {
	private ModEnchantments() {
	}

	private static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(BuiltInRegistries.ENCHANTMENT, Reference.MOD_ID);
	public static final Supplier<Enchantment> SEVERING = ENCHANTMENTS.register("severing", SeveringEnchantment::new);

	public static void register(IEventBus modBus) {
		ENCHANTMENTS.register(modBus);
	}

	public static class SeveringEnchantment extends Enchantment {
		protected SeveringEnchantment() {
			super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
		}

		@Override
		public int getMaxLevel() {
			return 5;
		}

		@Override
		public int getMinCost(int level) {
			return 15 + (level - 1) * 9;
		}

		@Override
		public int getMaxCost(int level) {
			return super.getMinCost(level) + 50;
		}

	}

}
