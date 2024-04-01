package reliquary.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import reliquary.reference.Reference;

public class SpawnThrownPotionImpactParticlesPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "thrown_potion_impact_particles");
	private final int color;
	private final double posX;
	private final double posY;
	private final double posZ;

	public SpawnThrownPotionImpactParticlesPacket(int color, double x, double y, double z) {
		this.color = color;
		posX = x;
		posY = y;
		posZ = z;
	}

	public SpawnThrownPotionImpactParticlesPacket(FriendlyByteBuf buffer) {
		color = buffer.readInt();
		posX = buffer.readDouble();
		posY = buffer.readDouble();
		posZ = buffer.readDouble();
	}

	public void handle(PlayPayloadContext context) {
		context.workHandler().execute(() -> handleMessage(color, posX, posY, posZ));
	}

	private void handleMessage(int color, double posX, double posY, double posZ) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		RandomSource rand = mc.level.random;

		float red = (((color >> 16) & 255) / 256F);
		float green = (((color >> 8) & 255) / 256F);
		float blue = ((color & 255) / 256F);

		for (int i = 0; i < 100; ++i) {
			double var39 = rand.nextDouble() * 4.0D;
			double angle = rand.nextDouble() * Math.PI * 2.0D;
			double xSpeed = Math.cos(angle) * var39;
			double ySpeed = 0.01D + rand.nextDouble() * 0.5D;
			double zSpeed = Math.sin(angle) * var39;

			Particle particle = mc.particleEngine.createParticle(ParticleTypes.EFFECT, posX + xSpeed * 0.1D, posY + 0.3D, posZ + zSpeed * 0.1D, xSpeed, ySpeed, zSpeed);
			if (particle != null) {
				float var32 = 0.75F + rand.nextFloat() * 0.25F;
				particle.setColor(red * var32, green * var32, blue * var32);
				particle.setPower((float) var39);
			}
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(color);
		buffer.writeDouble(posX);
		buffer.writeDouble(posY);
		buffer.writeDouble(posZ);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
