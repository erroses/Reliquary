package reliquary.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import reliquary.entities.ConcussiveExplosion;
import reliquary.reference.Reference;

public class SpawnConcussiveExplosionParticlesPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "spawn_concussive_explosion_particles");
	private final float size;
	private final Vec3 pos;

	public SpawnConcussiveExplosionParticlesPacket(float size, Vec3 pos) {
		this.size = size;
		this.pos = pos;
	}

	public SpawnConcussiveExplosionParticlesPacket(FriendlyByteBuf packetBuffer) {
		this(packetBuffer.readFloat(), new Vec3(packetBuffer.readDouble(), packetBuffer.readDouble(), packetBuffer.readDouble()));
	}

	public void handle(PlayPayloadContext context) {
		context.workHandler().execute(() -> context.level().ifPresent(level -> handleMessage(level, pos, size)));
	}

	private static void handleMessage(Level level, Vec3 pos, float size) {
		ConcussiveExplosion explosion = new ConcussiveExplosion(level, null, null, pos, size, false);
		explosion.finalizeExplosion(false);
	}

	@Override
	public void write(FriendlyByteBuf pBuffer) {
		pBuffer.writeFloat(size);
		pBuffer.writeDouble(pos.x());
		pBuffer.writeDouble(pos.y());
		pBuffer.writeDouble(pos.z());
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
