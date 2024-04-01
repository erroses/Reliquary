package reliquary.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import reliquary.init.ModItems;
import reliquary.util.InventoryHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CuriosCompat {

	private static final EmptyCuriosHandler EMPTY_HANDLER = new EmptyCuriosHandler();

	public CuriosCompat(IEventBus modBus) {
		modBus.addListener(this::setup);
		modBus.addListener(this::onRegisterCapabilities);

		if (FMLEnvironment.dist.isClient()) {
			CuriosCompatClient.registerLayerDefinitions(modBus);
		}
	}

	public void onRegisterCapabilities(RegisterCapabilitiesEvent evt) {
		evt.registerItem(
				CuriosCapability.ITEM,
				(itemStack, unused) -> new CuriosBaubleItemWrapper(itemStack),
				ModItems.FORTUNE_COIN.get(), ModItems.MOB_CHARM_BELT.get(), ModItems.TWILIGHT_CLOAK.get());
	}

	@SuppressWarnings("unused") //event type parameter needed for addListener to know when to call this method
	private void setup(FMLCommonSetupEvent event) {
		if (FMLEnvironment.dist.isClient()) {
			CuriosCompatClient.registerFortuneCoinToggler();
		}
		ModItems.MOB_CHARM.get().setCharmInventoryHandler(new CuriosCharmInventoryHandler());
		InventoryHelper.addBaublesItemHandlerFactory((player, type) -> (CuriosApi.getCuriosInventory(player)
				.map(handler -> handler.getStacksHandler(type.getIdentifier()).map(ICurioStacksHandler::getStacks).orElse(EMPTY_HANDLER)).orElse(EMPTY_HANDLER)));
	}

	public static Optional<ItemStack> getStackInSlot(LivingEntity entity, String slotName, int slot) {
		return CuriosApi.getCuriosInventory(entity).flatMap(handler -> handler.getStacksHandler(slotName)
				.map(sh -> sh.getStacks().getStackInSlot(slot)));
	}

	public static void setStackInSlot(LivingEntity entity, String slotName, int slot, ItemStack stack) {
		CuriosApi.getCuriosInventory(entity).flatMap(handler -> handler.getStacksHandler(slotName)).ifPresent(sh -> sh.getStacks().setStackInSlot(slot, stack));
	}

	private static class EmptyCuriosHandler extends ItemStackHandler implements IDynamicStackHandler {
		@Override
		public void setPreviousStackInSlot(int i, @Nonnull ItemStack itemStack) {
			//noop
		}

		@Override
		public ItemStack getPreviousStackInSlot(int i) {
			return ItemStack.EMPTY;
		}

		@Override
		public void grow(int i) {
			//noop
		}

		@Override
		public void shrink(int i) {
			//noop
		}
	}
}
