package reliquary.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import reliquary.client.gui.hud.CharmPane;
import reliquary.reference.Reference;

public class MobCharmDamagePacket implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "mob_charm_damage");
	private final ItemStack mobCharm;
	private final int slot;

	public MobCharmDamagePacket(ItemStack mobCharm, int slot) {
		this.mobCharm = mobCharm;
		this.slot = slot;
	}

	public MobCharmDamagePacket(FriendlyByteBuf packetBuffer) {
		this(packetBuffer.readItem(), packetBuffer.readByte());
	}

	public void handle(PlayPayloadContext context) {
		context.workHandler().execute(() -> handleMessage(mobCharm, slot));
	}

	private static void handleMessage(ItemStack mobCharm, int slot) {
		CharmPane.addCharmToDraw(mobCharm, slot);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeItem(mobCharm);
		buffer.writeByte(slot);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
