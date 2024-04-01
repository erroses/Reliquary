package reliquary.handler;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;

public interface IPlayerHurtHandler extends IPrioritizedHandler {
	boolean canApply(Player player, LivingAttackEvent event);
	boolean apply(Player player, LivingAttackEvent event);
}
