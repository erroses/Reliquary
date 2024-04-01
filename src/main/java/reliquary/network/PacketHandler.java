package reliquary.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import reliquary.reference.Reference;

public class PacketHandler {
	private PacketHandler() {
	}

	public static void registerPackets(final RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(Reference.MOD_ID).versioned("1.0");
		registrar.play(SpawnThrownPotionImpactParticlesPacket.ID, SpawnThrownPotionImpactParticlesPacket::new, play -> play.client(SpawnThrownPotionImpactParticlesPacket::handle));
		registrar.play(SpawnAngelheartVialParticlesPacket.ID, SpawnAngelheartVialParticlesPacket::new, play -> play.client(SpawnAngelheartVialParticlesPacket::handle));
		registrar.play(SpawnPhoenixDownParticlesPacket.ID, buffer -> new SpawnPhoenixDownParticlesPacket(), play -> play.client(SpawnPhoenixDownParticlesPacket::handle));
		registrar.play(ScrolledItemPacket.ID, ScrolledItemPacket::new, play -> play.server(ScrolledItemPacket::handle));
		registrar.play(SpawnConcussiveExplosionParticlesPacket.ID, SpawnConcussiveExplosionParticlesPacket::new, play -> play.client(SpawnConcussiveExplosionParticlesPacket::handle));
		registrar.play(MobCharmDamagePacket.ID, MobCharmDamagePacket::new, play -> play.client(MobCharmDamagePacket::handle));
		registrar.play(PedestalFishHookPacket.ID, PedestalFishHookPacket::new, play -> play.client(PedestalFishHookPacket::handle));
		registrar.play(FortuneCoinTogglePressedPacket.ID, FortuneCoinTogglePressedPacket::new, play -> play.server(FortuneCoinTogglePressedPacket::handle));

	}

	public static void sendToAllNear(Entity entity, CustomPacketPayload packet, double range) {
		PacketDistributor.NEAR.with(
				PacketDistributor.TargetPoint.p(entity.getX(), entity.getY(), entity.getZ(), range, entity.level().dimension()).get()
		).send(packet);
	}

	public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
		PacketDistributor.PLAYER.with(player).send(packet);
	}
}
