package reliquary.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import reliquary.api.IPedestal;
import reliquary.client.render.PedestalFishHookRenderer;
import reliquary.reference.Reference;
import reliquary.util.WorldHelper;

public class PedestalFishHookPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "pedestal_fish_hook");
	private final BlockPos pedestalPos;
	private final double hookX;
	private final double hookY;
	private final double hookZ;

	public PedestalFishHookPacket(BlockPos pedestalPos, double hookX, double hookY, double hookZ) {
		this.pedestalPos = pedestalPos;
		this.hookX = hookX;
		this.hookY = hookY;
		this.hookZ = hookZ;
	}

	public PedestalFishHookPacket(FriendlyByteBuf packetBuffer) {
		this(new BlockPos(packetBuffer.readInt(), packetBuffer.readInt(), packetBuffer.readInt()),
				packetBuffer.readDouble(), packetBuffer.readDouble(), packetBuffer.readDouble());
	}

	public void handle(PlayPayloadContext context) {
		context.workHandler().execute(() -> handleMessage(pedestalPos, hookX, hookY, hookZ));
	}

	private static void handleMessage(BlockPos pedestalPos, double hookX, double hookY, double hookZ) {
		ClientLevel world = Minecraft.getInstance().level;
		WorldHelper.getBlockEntity(world, pedestalPos, IPedestal.class).ifPresent(pedestal -> {
			PedestalFishHookRenderer.HookRenderingData data = null;
			if (hookY > 0) {
				data = new PedestalFishHookRenderer.HookRenderingData(hookX, hookY, hookZ);
			}

			pedestal.setItemData(data);
		});
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(pedestalPos.getX());
		buffer.writeInt(pedestalPos.getY());
		buffer.writeInt(pedestalPos.getZ());
		buffer.writeDouble(hookX);
		buffer.writeDouble(hookY);
		buffer.writeDouble(hookZ);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
