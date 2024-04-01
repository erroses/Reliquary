package reliquary.network;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import reliquary.compat.curios.CuriosCompat;
import reliquary.init.ModItems;
import reliquary.items.FortuneCoinItem;
import reliquary.reference.Reference;

import java.util.function.Supplier;

public class FortuneCoinTogglePressedPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "fortune_coin_toggle_pressed");
	private final InventoryType inventoryType;
	private final int slot;
	private final String identifier;

	public FortuneCoinTogglePressedPacket(InventoryType inventoryType, int slot) {
		this(inventoryType, "", slot);
	}

	public FortuneCoinTogglePressedPacket(InventoryType inventoryType, String identifier, int slot) {
		this.inventoryType = inventoryType;
		this.identifier = identifier;
		this.slot = slot;
	}

	public FortuneCoinTogglePressedPacket(FriendlyByteBuf packetBuffer) {
		this(InventoryType.values()[packetBuffer.readByte()], packetBuffer.readUtf(), packetBuffer.readInt());
	}

	public void handle(PlayPayloadContext context) {
		context.workHandler().execute(() -> context.player().ifPresent(p -> handleMessage(p, inventoryType, slot, identifier)));
	}

	private static void handleMessage(Player player, InventoryType inventoryType, int slot, String identifier) {
		switch (inventoryType) {
			case MAIN -> {
				ItemStack stack2 = player.getInventory().items.get(slot);
				if (stack2.getItem() == ModItems.FORTUNE_COIN.get()) {
					ModItems.FORTUNE_COIN.get().toggle(stack2);
					showMessage(player, stack2);
				}
			}
			case OFF_HAND -> {
				ItemStack stack1 = player.getInventory().offhand.get(0);
				if (stack1.getItem() == ModItems.FORTUNE_COIN.get()) {
					ModItems.FORTUNE_COIN.get().toggle(stack1);
					showMessage(player, stack1);
				}
			}
			case CURIOS -> run(() -> () -> CuriosCompat.getStackInSlot(player, identifier, slot)
					.ifPresent(stack -> {
						if (stack.getItem() == ModItems.FORTUNE_COIN.get()) {
							ModItems.FORTUNE_COIN.get().toggle(stack);
							showMessage(player, stack);
							CuriosCompat.setStackInSlot(player, identifier, slot, stack);
						}
					}));
		}
	}

	private static void showMessage(Player player, ItemStack fortuneCoin) {
		player.displayClientMessage(Component.translatable("chat.reliquary.fortune_coin.toggle",
						FortuneCoinItem.isEnabled(fortuneCoin) ?
								Component.translatable("chat.reliquary.fortune_coin.on").withStyle(ChatFormatting.GREEN)
								: Component.translatable("chat.reliquary.fortune_coin.off").withStyle(ChatFormatting.RED))
				, true);
	}

	private static void run(Supplier<Runnable> toRun) {
		toRun.get().run();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeByte(inventoryType.ordinal());
		buffer.writeUtf(identifier);
		buffer.writeInt(slot);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}

	public enum InventoryType {
		MAIN,
		OFF_HAND,
		CURIOS
	}
}
