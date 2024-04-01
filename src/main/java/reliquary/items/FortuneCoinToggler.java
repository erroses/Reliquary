package reliquary.items;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import reliquary.handler.ClientEventHandler;
import reliquary.init.ModItems;
import reliquary.network.FortuneCoinTogglePressedPacket;

public class FortuneCoinToggler {
	private static FortuneCoinToggler coinToggler = new FortuneCoinToggler();

	public static void setCoinToggler(FortuneCoinToggler toggler) {
		coinToggler = toggler;
	}

	@SuppressWarnings({"squid:S1172", "unused"}) //used in addListener reflection code
	public static void handleKeyInputEvent(TickEvent.ClientTickEvent event) {
		if (ClientEventHandler.FORTUNE_COIN_TOGGLE_KEYBIND.consumeClick()) {
			coinToggler.findAndToggle();
		}
	}

	public boolean findAndToggle() {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return false;
		}

		for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
			ItemStack stack = player.getInventory().items.get(slot);
			if (stack.getItem() == ModItems.FORTUNE_COIN.get()) {
				PacketDistributor.SERVER.noArg().send(new FortuneCoinTogglePressedPacket(FortuneCoinTogglePressedPacket.InventoryType.MAIN, slot));

				ModItems.FORTUNE_COIN.get().toggle(stack);
				return true;
			}
		}
		if (player.getInventory().offhand.get(0).getItem() == ModItems.FORTUNE_COIN.get()) {
			PacketDistributor.SERVER.noArg().send(new FortuneCoinTogglePressedPacket(FortuneCoinTogglePressedPacket.InventoryType.OFF_HAND, 0));
			ModItems.FORTUNE_COIN.get().toggle(player.getInventory().offhand.get(0));
			return true;
		}
		return false;
	}
}
