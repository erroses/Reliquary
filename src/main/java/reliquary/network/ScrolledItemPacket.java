package reliquary.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import reliquary.items.util.IScrollableItem;
import reliquary.reference.Reference;

public class ScrolledItemPacket implements CustomPacketPayload {

	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "scrolled_item");
	private final double scrollDelta;

	public ScrolledItemPacket(double scrollDelta) {
		this.scrollDelta = scrollDelta;
	}

	public ScrolledItemPacket(FriendlyByteBuf buffer) {
		this(buffer.readDouble());
	}

	public void handle(PlayPayloadContext context) {
		context.workHandler().execute(() -> context.player().ifPresent(player -> handleMessage(player, scrollDelta)));
	}

	private void handleMessage(Player sender, double scrollDelta) {
		ItemStack stack = sender.getMainHandItem();

		if (stack.getItem() instanceof IScrollableItem leftClickableItem) {
			leftClickableItem.onMouseScrolled(stack, sender, scrollDelta);
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeDouble(scrollDelta);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
