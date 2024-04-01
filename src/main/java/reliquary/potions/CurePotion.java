package reliquary.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import reliquary.init.ModPotions;

public class CurePotion extends MobEffect {

	public CurePotion() {
		super(MobEffectCategory.BENEFICIAL, 15723850);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public void applyEffectTick(LivingEntity entityLivingBase, int potency) {
		if (entityLivingBase instanceof ZombieVillager zombieVillager) {
			if (!zombieVillager.isConverting() && entityLivingBase.hasEffect(MobEffects.WEAKNESS)) {
				zombieVillager.startConverting(null, (entityLivingBase.level().random.nextInt(2401) + 3600) / (potency + 2));
				entityLivingBase.removeEffect(ModPotions.CURE_POTION.get());
			}
		} else {
			entityLivingBase.removeEffect(ModPotions.CURE_POTION.get());
		}
	}
}
