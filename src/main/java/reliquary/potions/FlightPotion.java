package reliquary.potions;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.Nullable;
import reliquary.init.ModPotions;

public class FlightPotion extends MobEffect {

	public FlightPotion() {
		super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
		NeoForge.EVENT_BUS.addListener(this::onEffectExpired);
		NeoForge.EVENT_BUS.addListener(this::onEffectRemoved);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public void applyEffectTick(LivingEntity entityLivingBase, int amplifier) {
		if (entityLivingBase.level().isClientSide || !(entityLivingBase instanceof Player player)) {
			return;
		}

		if (!player.getAbilities().mayfly) {
			player.getAbilities().mayfly = true;
			((ServerPlayer) player).connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
		}
		player.fallDistance = 0;
	}

	@Override
	public void removeAttributeModifiers(AttributeMap pAttributeMap) {
		super.removeAttributeModifiers(pAttributeMap);
	}

	private void onEffectExpired(MobEffectEvent.Expired event) {
		removeFlight(event.getEntity(), event.getEffectInstance());
	}

	private void onEffectRemoved(MobEffectEvent.Remove event) {
		removeFlight(event.getEntity(), event.getEffectInstance());
	}

	private static void removeFlight(LivingEntity entity, @Nullable MobEffectInstance effectInstance) {
		if (effectInstance == null || effectInstance.getEffect() != ModPotions.FLIGHT_POTION.get()) {
			return;
		}

		if (!(entity instanceof Player player)) {
			return;
		}

		if (!player.isCreative()) {
			player.getAbilities().mayfly = false;
			player.getAbilities().flying = false;
			((ServerPlayer) player).connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
		}
	}
}
