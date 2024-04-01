package reliquary;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import reliquary.client.init.ModParticles;
import reliquary.crafting.AlkahestryRecipeRegistry;
import reliquary.data.DataGenerators;
import reliquary.handler.ClientEventHandler;
import reliquary.handler.CommonEventHandler;
import reliquary.init.*;
import reliquary.items.MobCharmRegistry;
import reliquary.network.PacketHandler;
import reliquary.reference.Config;
import reliquary.reference.Reference;
import reliquary.util.potions.PotionMap;

@Mod(Reference.MOD_ID)
public class Reliquary {

	@SuppressWarnings("java:S1118") //needs to be public for mod to work
	public Reliquary(IEventBus modBus) {
		NeoForgeMod.enableMilkFluid();
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ClientEventHandler.registerHandlers();
		}
		modBus.addListener(Reliquary::setup);
		modBus.addListener(Reliquary::loadComplete);
		modBus.addListener(Config::onFileChange);
		modBus.addListener(DataGenerators::gatherData);
		modBus.addListener(PacketHandler::registerPackets);

		ModFluids.registerHandlers(modBus);
		ModItems.registerListeners(modBus);
		ModBlocks.registerListeners(modBus);
		ModEntities.registerListeners(modBus);
		ModPotions.registerListeners(modBus);
		ModSounds.registerListeners(modBus);
		ModEnchantments.register(modBus);
		ModParticles.registerListeners(modBus);

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);

		IEventBus eventBus = NeoForge.EVENT_BUS;
		CommonEventHandler.registerEventBusListeners(eventBus);
		eventBus.addListener(MobCharmRegistry::handleAddingFragmentDrops);
		eventBus.addListener(AlkahestryRecipeRegistry::onResourceReload);

		ModCompat.initCompats(modBus);
	}

	public static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(ModItems::registerDispenseBehaviors);
		PotionMap.initPotionMap();
		ModItems.registerHandgunMagazines();
		PedestalItems.init();
	}

	public static void loadComplete(FMLLoadCompleteEvent event) {
		MobCharmRegistry.registerDynamicCharmDefinitions();
	}
}
