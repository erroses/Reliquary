package reliquary.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import reliquary.reference.Config;
import reliquary.util.TooltipBuilder;

import javax.annotation.Nullable;

public class EmperorChaliceItem extends ToggleableItem {

	public EmperorChaliceItem() {
		super(new Properties().stacksTo(1).setNoRepair().rarity(Rarity.EPIC));
		NeoForge.EVENT_BUS.addListener(this::onBlockRightClick);
	}

	@Override
	protected void addMoreInformation(ItemStack stack, @Nullable Level world, TooltipBuilder tooltipBuilder) {
		tooltipBuilder.description(this, ".tooltip2");
	}

	@Override
	protected boolean hasMoreInformation(ItemStack stack) {
		return true;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 16;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
		if (world.isClientSide) {
			return stack;
		}

		if (!(entityLiving instanceof Player player)) {
			return stack;
		}

		int multiplier = Config.COMMON.items.emperorChalice.hungerSatiationMultiplier.get();
		player.getFoodData().eat(1, (float) multiplier / 2);
		player.hurt(player.damageSources().drown(), multiplier);
		return stack;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack emperorChalice = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			return super.use(world, player, hand);
		}
		boolean isInDrainMode = isEnabled(emperorChalice);
		BlockHitResult result = getPlayerPOVHitResult(world, player, isInDrainMode ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);

		//noinspection ConstantConditions
		if (result == null || result.getType() == HitResult.Type.MISS) {
			if (!isEnabled(emperorChalice)) {
				player.startUsingItem(hand);
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, emperorChalice);
		} else if (result.getType() == HitResult.Type.BLOCK) {
			if (!world.mayInteract(player, result.getBlockPos()) || !player.mayUseItemAt(result.getBlockPos(), result.getDirection(), emperorChalice)) {
				return new InteractionResultHolder<>(InteractionResult.FAIL, emperorChalice);
			}

			IFluidHandlerItem fluidHandler = emperorChalice.getCapability(Capabilities.FluidHandler.ITEM);
			if (fluidHandler != null) {
				boolean success;
				if (!isEnabled(emperorChalice)) {
					success = placeWater(world, player, hand, fluidHandler, result);
				} else {
					success = FluidUtil.tryPickUpFluid(emperorChalice, player, world, result.getBlockPos(), result.getDirection()).isSuccess();
				}
				if (success) {
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, emperorChalice);
				}
			}
		}

		return new InteractionResultHolder<>(InteractionResult.PASS, emperorChalice);
	}

	private boolean placeWater(Level world, Player player, InteractionHand hand, IFluidHandlerItem fluidHandler, BlockHitResult result) {
		if (FluidUtil.tryPlaceFluid(player, world, hand, result.getBlockPos(), fluidHandler, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME))) {
			return true;
		}
		return FluidUtil.tryPlaceFluid(player, world, hand, result.getBlockPos().relative(result.getDirection()), fluidHandler, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME));
	}

	private void onBlockRightClick(PlayerInteractEvent.RightClickBlock evt) {
		if (evt.getItemStack().getItem() == this) {
			Level world = evt.getLevel();
			BlockState state = world.getBlockState(evt.getPos());
			if (!isEnabled(evt.getItemStack()) && state.getBlock() == Blocks.CAULDRON) {
				fillCauldron(evt, world);
			} else if (isEnabled(evt.getItemStack()) && state.getBlock() == Blocks.WATER_CAULDRON && ((LayeredCauldronBlock) state.getBlock()).isFull(state)) {
				emptyCauldron(evt, world);
			}
		}
	}

	private void emptyCauldron(PlayerInteractEvent.RightClickBlock evt, Level world) {
		world.setBlockAndUpdate(evt.getPos(), Blocks.CAULDRON.defaultBlockState());
		cancelEvent(evt);
	}

	private void fillCauldron(PlayerInteractEvent.RightClickBlock evt, Level world) {
		world.setBlockAndUpdate(evt.getPos(), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
		cancelEvent(evt);
	}

	private void cancelEvent(PlayerInteractEvent.RightClickBlock evt) {
		evt.setUseItem(Event.Result.DENY);
		evt.setCanceled(true);
		evt.setCancellationResult(InteractionResult.SUCCESS);
	}
}
