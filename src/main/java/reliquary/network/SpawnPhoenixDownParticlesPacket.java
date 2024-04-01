package reliquary.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import reliquary.reference.Reference;

public class SpawnPhoenixDownParticlesPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "spawn_phoenix_down_particles");

	public SpawnPhoenixDownParticlesPacket() {
		//noop
	}

	public void handle(PlayPayloadContext context) {
		context.workHandler().execute(this::handleMessage);
	}

	@SuppressWarnings("ConstantConditions") // the player isn't null when particles are spawned
	private void handleMessage() {
		LocalPlayer player = Minecraft.getInstance().player;
		for (int particles = 0; particles <= 400; particles++) {
			player.level().addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), player.level().random.nextGaussian() * 8, player.level().random.nextGaussian() * 8, player.level().random.nextGaussian() * 8);
		}
	}

	@Override
	public void write(FriendlyByteBuf pBuffer) {
		//noop
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
