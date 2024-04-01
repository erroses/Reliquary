package reliquary.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import reliquary.init.ModItems;
import reliquary.reference.Reference;

public class SpawnAngelheartVialParticlesPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "angelheart_vial_particles");

	public SpawnAngelheartVialParticlesPacket() {
		//noop
	}

	public SpawnAngelheartVialParticlesPacket(FriendlyByteBuf buffer) {
		//noop
	}

	public void handle(PlayPayloadContext context) {
		context.workHandler().execute(this::handleMessage);
	}

	private void handleMessage() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		double var8 = player.getX();
		double var10 = player.getY();
		double var12 = player.getZ();
		RandomSource var7 = player.level().random;
		ItemParticleOption itemParticleData = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.ANGELHEART_VIAL.get()));
		for (int var15 = 0; var15 < 8; ++var15) {
			player.level().addParticle(itemParticleData, var8, var10, var12, var7.nextGaussian() * 0.15D, var7.nextDouble() * 0.2D, var7.nextGaussian() * 0.15D);
		}

		// purple, for reals.
		float red = 1.0F;
		float green = 0.0F;
		float blue = 1.0F;

		for (int var20 = 0; var20 < 100; ++var20) {
			double var39 = var7.nextDouble() * 4.0D;
			double var23 = var7.nextDouble() * Math.PI * 2.0D;
			double var25 = Math.cos(var23) * var39;
			double var27 = 0.01D + var7.nextDouble() * 0.5D;
			double var29 = Math.sin(var23) * var39;
			if (player.level().isClientSide) {
				Particle var31 = Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.EFFECT, var8 + var25 * 0.1D, var10 + 0.3D, var12 + var29 * 0.1D, var25, var27, var29);
				if (var31 != null) {
					float var32 = 0.75F + var7.nextFloat() * 0.25F;
					var31.setColor(red * var32, green * var32, blue * var32);
					var31.setPower((float) var39);
				}
			}
		}
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		//noop
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
