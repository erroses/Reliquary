package reliquary.client.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import reliquary.client.particle.CauldronBubbleParticle;
import reliquary.client.particle.CauldronBubbleParticleType;
import reliquary.client.particle.CauldronSteamParticle;
import reliquary.client.particle.CauldronSteamParticleType;
import reliquary.reference.Reference;

import java.util.function.Supplier;

public class ModParticles {
	private ModParticles() {}

	private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Reference.MOD_ID);
	public static final Supplier<CauldronSteamParticleType> CAULDRON_STEAM = PARTICLES.register("cauldron_steam", CauldronSteamParticleType::new);
	public static final Supplier<CauldronBubbleParticleType> CAULDRON_BUBBLE = PARTICLES.register("cauldron_bubble", CauldronBubbleParticleType::new);

	public static void registerListeners(IEventBus modBus) {
		PARTICLES.register(modBus);
	}

	public static class ProviderHandler {
		private ProviderHandler() {}

		public static void registerProviders(RegisterParticleProvidersEvent event) {
			event.registerSpriteSet(CAULDRON_STEAM.get(), CauldronSteamParticle.Provider::new);
			event.registerSpriteSet(CAULDRON_BUBBLE.get(), CauldronBubbleParticle.Provider::new);
		}
	}
}
