package reliquary.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.IPlantable;
import net.neoforged.neoforge.items.IItemHandler;
import reliquary.blocks.FertileLilyPadBlock;
import reliquary.entities.EntityXRFakePlayer;
import reliquary.init.ModItems;
import reliquary.items.util.HarvestRodCache;
import reliquary.items.util.HarvestRodItemStackHandler;
import reliquary.items.util.IScrollableItem;
import reliquary.reference.Config;
import reliquary.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class HarvestRodItem extends ToggleableItem implements IScrollableItem {
	private static final String MODE_NBT_TAG = "mode";
	private static final String PLANTABLE_INDEX_NBT_TAG = "plantable_index";
	private static final int AOE_START_COOLDOWN = 10;

	public HarvestRodItem() {
		super(new Properties().stacksTo(1).setNoRepair());
	}

	@Override
	public MutableComponent getName(ItemStack stack) {
		return super.getName(stack).withStyle(ChatFormatting.DARK_GREEN);
	}

	@Override
	protected void addMoreInformation(ItemStack rod, @Nullable Level world, TooltipBuilder tooltipBuilder) {
		tooltipBuilder.charge(this, ".tooltip2", getBoneMealCount(rod));
		for (int slot = 1; slot < getCountPlantable(rod); slot++) {
			ItemStack plantable = getPlantableInSlot(rod, slot);
			tooltipBuilder.charge(this, ".tooltip3", plantable.getItem().getName(plantable).getString(), getPlantableQuantity(rod, slot));
		}

		if (isEnabled(rod)) {
			tooltipBuilder.absorbActive(new ItemStack(Items.BONE_MEAL).getHoverName().getString());
		} else {
			tooltipBuilder.absorb();
		}
	}

	@Override
	protected boolean hasMoreInformation(ItemStack stack) {
		return true;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	private int getBonemealLimit() {
		return Config.COMMON.items.harvestRod.boneMealLimit.get();
	}

	private int getBonemealWorth() {
		return Config.COMMON.items.harvestRod.boneMealWorth.get();
	}

	public int getBonemealCost() {
		return Config.COMMON.items.harvestRod.boneMealCost.get();
	}

	public int getLuckRolls() {
		return Config.COMMON.items.harvestRod.boneMealLuckRolls.get();
	}

	public int getLuckPercent() {
		return Config.COMMON.items.harvestRod.boneMealLuckPercentChance.get();
	}

	private int getBreakRadius() {
		return Config.COMMON.items.harvestRod.aoeRadius.get();
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
		if (world.isClientSide || world.getGameTime() % 10 != 0 || !(entity instanceof Player player)) {
			return;
		}

		if (isEnabled(stack)) {
			int currentCharge = getBoneMealCount(stack);
			consumeAndCharge(player, getBonemealLimit() - currentCharge, getBonemealWorth(), Items.BONE_MEAL, 16,
					chargeToAdd -> setBoneMealCount(stack, currentCharge + chargeToAdd));
			consumePlantables(stack, player);
		}
	}

	private void consumePlantables(ItemStack harvestRod, Player player) {
		int leftToInsert = 16;

		for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
			ItemStack currentStack = player.getInventory().items.get(slot);
			if (isPlantable(currentStack)) {
				int countInserted = incrementPlantable(harvestRod, currentStack, leftToInsert);
				leftToInsert -= countInserted;
				currentStack.shrink(countInserted);
				player.getInventory().items.set(slot, currentStack.isEmpty() ? ItemStack.EMPTY : currentStack);
				if (leftToInsert == 0) {
					break;
				}
			}
		}
	}

	private boolean isPlantable(ItemStack currentStack) {
		Item item = currentStack.getItem();
		return item instanceof BlockItem blockItem && blockItem.getBlock() instanceof IPlantable;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
		if (player.level().isClientSide) {
			return false;
		}

		boolean brokenBlock = false;

		Block block = player.level().getBlockState(pos).getBlock();
		if (block instanceof IPlantable || block == Blocks.MELON || block == Blocks.PUMPKIN) {
			for (int xOff = -getBreakRadius(); xOff <= getBreakRadius(); xOff++) {
				for (int yOff = -getBreakRadius(); yOff <= getBreakRadius(); yOff++) {
					for (int zOff = -getBreakRadius(); zOff <= getBreakRadius(); zOff++) {
						brokenBlock |= doHarvestBlockBreak(block, stack, pos, player, xOff, yOff, zOff);
					}
				}
			}
		}

		return brokenBlock;
	}

	private boolean doHarvestBlockBreak(Block initialBlock, ItemStack stack, BlockPos pos, Player player, int xOff, int yOff, int zOff) {
		pos = pos.offset(xOff, yOff, zOff);

		BlockState blockState = player.level().getBlockState(pos);
		Block block = blockState.getBlock();

		if ((initialBlock == Blocks.MELON || initialBlock == Blocks.PUMPKIN) && !(block == Blocks.MELON || block == Blocks.PUMPKIN)) {
			return false;
		}

		if (!(block instanceof IPlantable || block == Blocks.MELON || block == Blocks.PUMPKIN)) {
			return false;
		}
		if (block instanceof FertileLilyPadBlock) {
			return false;
		}

		if (player.level().isClientSide) {
			for (int particles = 0; particles <= 8; particles++) {
				player.level().levelEvent(player, 2001, pos, Block.getId(blockState));
			}
		} else if (player.level() instanceof ServerLevel serverLevel) {
			List<ItemStack> drops = Block.getDrops(blockState, serverLevel, pos, null, player, stack);
			for (ItemStack itemStack : drops) {
				float f = 0.7F;
				double d = (serverLevel.random.nextFloat() * f) + (1.0F - f) * 0.5D;
				double d1 = (serverLevel.random.nextFloat() * f) + (1.0F - f) * 0.5D;
				double d2 = (serverLevel.random.nextFloat() * f) + (1.0F - f) * 0.5D;
				ItemEntity entityitem = new ItemEntity(player.level(), pos.getX() + d, pos.getY() + d1, pos.getZ() + d2, itemStack);
				entityitem.setPickUpDelay(10);
				player.level().addFreshEntity(entityitem);
			}

			player.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			player.awardStat(Stats.BLOCK_MINED.get(blockState.getBlock()));
			player.causeFoodExhaustion(0.01F);
		}

		return true;
	}

	private void boneMealBlock(ItemStack stack, Player player, Level world, BlockPos pos) {
		ItemStack fakeItemStack = new ItemStack(Items.BONE_MEAL);

		boolean usedRod = false;
		for (int repeatedUses = 0; repeatedUses <= getLuckRolls(); repeatedUses++) {
			if ((repeatedUses == 0 || world.random.nextInt(100) <= getLuckPercent()) && BoneMealItem.applyBonemeal(fakeItemStack, world, pos, player)) {
				if (!usedRod) {
					usedRod = true;
				}
				player.level().levelEvent(1505, pos, 0);
				player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.1F, 0.5F * (RandHelper.getRandomMinusOneToOne(player.level().random) * 0.7F + 1.2F));
			}
		}

		if (usedRod && !player.isCreative()) {
			setBoneMealCount(stack, getBoneMealCount(stack) - getBonemealCost());
		}
	}

	public int getBoneMealCount(ItemStack stack) {
		return getFromHandler(stack, HarvestRodItemStackHandler::getBoneMealCount).orElse(0);
	}

	private <T> Optional<T> getFromHandler(ItemStack harvestRod, Function<HarvestRodItemStackHandler, T> get) {
		return InventoryHelper.getFromHandler(harvestRod, get, HarvestRodItemStackHandler.class);
	}

	private void runOnHandler(ItemStack harvestRod, Consumer<HarvestRodItemStackHandler> run) {
		InventoryHelper.runOnItemHandler(harvestRod, run, HarvestRodItemStackHandler.class);
	}

	public void setBoneMealCount(ItemStack harvestRod, int boneMealCount) {
		runOnHandler(harvestRod, h -> h.setBoneMealCount(boneMealCount));
	}

	private int incrementPlantable(ItemStack harvestRod, ItemStack plantable, int maxCount) {
		return getFromHandler(harvestRod, h -> {
			ItemStack plantableCopy = plantable.copy();
			plantableCopy.setCount(Math.min(maxCount, plantableCopy.getCount()));
			return h.insertPlantable(plantableCopy).map(HarvestRodItemStackHandler.PlantableSlotInserted::getCountInserted).orElse(0);
		}).orElse(0);
	}

	private void decrementPlantable(ItemStack harvestRod, byte slot) {
		getFromHandler(harvestRod, h -> h.getStackInSlot(slot).getCount()).ifPresent(amount -> setPlantableQuantity(harvestRod, slot, amount - 1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			return super.use(world, player, hand);
		}

		player.startUsingItem(hand);

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 300;
	}

	@Override
	public void releaseUsing(ItemStack harvestRod, Level world, LivingEntity entity, int timeLeft) {
		if (entity.level().isClientSide || !(entity instanceof Player player)) {
			return;
		}

		BlockHitResult result = getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.ANY);

		if (result.getType() == HitResult.Type.BLOCK) {
			HarvestRodCache harvestRodCache = harvestRod.getData(ModItems.HARVEST_ROD_CACHE);
			harvestRodCache.reset();
			BlockPos pos = result.getBlockPos();

			switch (getMode(harvestRod)) {
				case BONE_MEAL:
					if (getBoneMealCount(harvestRod) >= getBonemealCost() || player.isCreative()) {
						boneMealBlock(harvestRod, player, world, pos);
					}
					break;
				case PLANTABLE:
					if (getPlantableQuantity(harvestRod, getCurrentPlantableSlot(harvestRod)) > 0 || player.isCreative()) {
						plantItem(harvestRod, player, pos, player.getUsedItemHand());
					}
					break;
				case HOE:
					hoeLand(world, pos);
					break;
				default:
			}

		} else {
			removeStackFromCurrent(harvestRod, player);
		}
	}

	private void hoeLand(Level world, BlockPos pos) {
		ItemStack fakeHoe = new ItemStack(Items.WOODEN_HOE);
		EntityXRFakePlayer fakePlayer = XRFakePlayerFactory.get((ServerLevel) world);
		fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, fakeHoe);

		if (Items.WOODEN_HOE.useOn(ItemHelper.getItemUseContext(pos, fakePlayer)) == InteractionResult.SUCCESS) {
			world.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	private void removeStackFromCurrent(ItemStack stack, Player player) {
		if (getMode(stack) == Mode.BONE_MEAL && getBoneMealCount(stack) > 0) {
			ItemStack boneMealStack = new ItemStack(Items.BONE_MEAL);
			int numberToAdd = Math.min(boneMealStack.getMaxStackSize(), getBoneMealCount(stack));
			IItemHandler playerInventory = InventoryHelper.getItemHandlerFrom(player);
			int numberAdded = InventoryHelper.tryToAddToInventory(boneMealStack, playerInventory, numberToAdd);
			setBoneMealCount(stack, getBoneMealCount(stack) - numberAdded);
		} else if (getMode(stack) == Mode.PLANTABLE) {
			byte plantableSlot = getCurrentPlantableSlot(stack);
			ItemStack plantableStack = getCurrentPlantable(stack);
			int plantableQuantity = getPlantableQuantity(stack, plantableSlot);
			int numberToAdd = Math.min(plantableStack.getMaxStackSize(), plantableQuantity);
			IItemHandler playerInventory = InventoryHelper.getItemHandlerFrom(player);
			int numberAdded = InventoryHelper.tryToAddToInventory(plantableStack, playerInventory, numberToAdd);

			int updatedPlantableQuantity = getPlantableQuantity(stack, plantableSlot) - numberAdded;

			setPlantableQuantity(stack, plantableSlot, updatedPlantableQuantity);
		}
	}

	private void shiftModeOnEmptyPlantable(ItemStack harvestRod, byte plantableSlot) {
		if (plantableSlot > 0) {
			setCurrentPlantableSlot(harvestRod, (byte) (plantableSlot - 1));
		}
		cycleMode(harvestRod, true);
	}

	private void plantItem(ItemStack harvestRod, Player player, BlockPos pos, InteractionHand hand) {
		byte plantableSlot = getCurrentPlantableSlot(harvestRod);
		ItemStack fakePlantableStack = getCurrentPlantable(harvestRod).copy();
		fakePlantableStack.setCount(1);

		EntityXRFakePlayer fakePlayer = XRFakePlayerFactory.get((ServerLevel) player.level());
		fakePlayer.setItemInHand(hand, fakePlantableStack);

		if (fakePlantableStack.useOn(ItemHelper.getItemUseContext(pos, fakePlayer)).consumesAction()) {
			player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, 0.5F * (RandHelper.getRandomMinusOneToOne(player.level().random) * 0.7F + 1.2F));

			if (!player.isCreative()) {
				decrementPlantable(harvestRod, plantableSlot);
			}
		}
	}

	public ItemStack getCurrentPlantable(ItemStack harvestRod) {
		int currentSlot = getCurrentPlantableSlot(harvestRod);
		return getPlantableInSlot(harvestRod, currentSlot);
	}

	public ItemStack getPlantableInSlot(ItemStack harvestRod, int slot) {
		return getFromHandler(harvestRod, h -> h.getStackInSlot(slot)).orElse(ItemStack.EMPTY);
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack harvestRod, int remainingUseDuration) {
		if (livingEntity.level().isClientSide || !(livingEntity instanceof Player player)) {
			return;
		}

		if (isCoolDownOver(harvestRod, remainingUseDuration)) {
			BlockHitResult result = getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.ANY);
			if (result.getType() == HitResult.Type.BLOCK) {
				Level world = player.level();
				HarvestRodCache harvestRodCache = harvestRod.getData(ModItems.HARVEST_ROD_CACHE);
				doAction(harvestRod, player, world, harvestRodCache, result.getBlockPos());
			}
		}
	}

	private void doAction(ItemStack harvestRod, Player player, Level world, HarvestRodCache cache, BlockPos pos) {
		switch (getMode(harvestRod)) {
			case BONE_MEAL:
				if (getBoneMealCount(harvestRod) >= getBonemealCost() || player.isCreative()) {
					getNextBlockToBoneMeal(world, cache, pos, Config.COMMON.items.harvestRod.aoeRadius.get())
							.ifPresent(blockToBoneMeal -> boneMealBlock(harvestRod, player, world, blockToBoneMeal));
				}
				break;
			case PLANTABLE:
				if (getCountPlantable(harvestRod) > 0) {
					clearPlantableIfNoLongerValid(harvestRod, getCurrentPlantableSlot(harvestRod));
				}
				if (getPlantableQuantity(harvestRod, getCurrentPlantableSlot(harvestRod)) >= 1 || player.isCreative()) {
					getNextBlockToPlantOn(world, cache, pos, Config.COMMON.items.harvestRod.aoeRadius.get(), (IPlantable) ((BlockItem) getCurrentPlantable(harvestRod).getItem()).getBlock())
							.ifPresent(blockToPlantOn -> plantItem(harvestRod, player, blockToPlantOn, player.getUsedItemHand()));
				}
				break;
			case HOE:
				getNextBlockToHoe(world, cache, pos, Config.COMMON.items.harvestRod.aoeRadius.get()).ifPresent(blockToHoe -> hoeLand(world, blockToHoe));
				break;
			default:
				break;
		}
	}

	public void clearPlantableIfNoLongerValid(ItemStack harvestRod, byte slot) {
		if (getPlantableInSlot(harvestRod, slot).isEmpty()) {
			setPlantableQuantity(harvestRod, slot, 0);
		}
	}

	private Optional<BlockPos> getNextBlockToHoe(Level world, HarvestRodCache cache, BlockPos pos, int range) {
		if (cache.isQueueEmpty() || !pos.equals(cache.getStartBlockPos())) {
			fillQueue(cache, pos, range, currentPos -> {
				BlockState blockState = world.getBlockState(currentPos);
				Block block = blockState.getBlock();

				return world.isEmptyBlock(currentPos.above()) && (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH || block == Blocks.DIRT || block == Blocks.COARSE_DIRT);
			});
		}

		return cache.getNextBlockInQueue();
	}

	private void fillQueue(HarvestRodCache cache, BlockPos pos, int range, Predicate<BlockPos> isValidBlock) {
		cache.setStartBlockPos(pos);
		cache.clearBlockQueue();
		BlockPos.betweenClosedStream(pos.offset(-range, -range, -range), pos.offset(range, range, range))
				.forEach(currentPos -> {
					if (isValidBlock.test(currentPos)) {
						cache.addBlockToQueue(currentPos.immutable());
					}
				});
	}

	private Optional<BlockPos> getNextBlockToPlantOn(Level world, HarvestRodCache cache, BlockPos pos, int range, IPlantable plantable) {
		if (cache.isQueueEmpty() || !pos.equals(cache.getStartBlockPos())) {
			fillQueueToPlant(world, cache, pos, range, plantable);
		}

		return cache.getNextBlockInQueue();
	}

	private void fillQueueToPlant(Level world, HarvestRodCache cache, BlockPos pos, int range, IPlantable plantable) {
		boolean checkerboard = false;
		boolean bothOddOrEven = false;

		if (plantable == Blocks.PUMPKIN_STEM || plantable == Blocks.MELON_STEM) {
			checkerboard = true;
			boolean xEven = pos.getX() % 2 == 0;
			boolean zEven = pos.getZ() % 2 == 0;
			bothOddOrEven = xEven == zEven;
		}

		boolean finalCheckerboard = checkerboard;
		boolean finalBothOddOrEven = bothOddOrEven;
		fillQueue(cache, pos, range, currentPos -> {
			BlockState blockState = world.getBlockState(currentPos);
			return (!finalCheckerboard || (finalBothOddOrEven == ((currentPos.getX() % 2 == 0) == (currentPos.getZ() % 2 == 0)))) && blockState.getBlock().canSustainPlant(blockState, world, pos, Direction.UP, plantable) && world.isEmptyBlock(currentPos.above());
		});
	}

	@Override
	public InteractionResult onMouseScrolled(ItemStack stack, Player player, double scrollDelta) {
		if (player.level().isClientSide) {
			return InteractionResult.PASS;
		}
		cycleMode(stack, scrollDelta > 0);
		return InteractionResult.SUCCESS;
	}

	private boolean isCoolDownOver(ItemStack stack, int count) {
		return getUseDuration(stack) - count >= AOE_START_COOLDOWN && (getUseDuration(stack) - count) % Config.COMMON.items.harvestRod.aoeCooldown.get() == 0;
	}

	private Optional<BlockPos> getNextBlockToBoneMeal(Level world, HarvestRodCache cache, BlockPos pos, int range) {
		if (cache.isQueueEmpty() || !pos.equals(cache.getStartBlockPos())) {
			fillQueue(cache, pos, range, currentPos -> {
				BlockState blockState = world.getBlockState(currentPos);
				return blockState.getBlock() instanceof BonemealableBlock bonemealableBlock && bonemealableBlock.isValidBonemealTarget(world, currentPos, blockState);
			});
		}

		return cache.getNextBlockInQueue();
	}

	private void cycleMode(ItemStack harvestRod, boolean next) {
		Mode currentMode = getMode(harvestRod);
		int plantableCount = getCountPlantable(harvestRod);
		if (next) {
			setNextMode(harvestRod, currentMode, plantableCount);
		} else {
			setPreviousMode(harvestRod, currentMode, plantableCount);
		}
	}

	private void setPreviousMode(ItemStack harvestRod, Mode currentMode, int plantableCount) {
		if (currentMode == Mode.PLANTABLE && getCurrentPlantableSlot(harvestRod) > 1) {
			setCurrentPlantableSlot(harvestRod, (byte) (getCurrentPlantableSlot(harvestRod) - 1));
			return;
		}
		Mode previousMode = currentMode.previous();
		if (previousMode == Mode.PLANTABLE) {
			if (plantableCount == 0) {
				previousMode = previousMode.previous();
			} else {
				setCurrentPlantableSlot(harvestRod, (byte) plantableCount);
			}
		}
		setMode(harvestRod, previousMode);
	}

	private void setNextMode(ItemStack harvestRod, Mode currentMode, int plantableCount) {
		if (currentMode == Mode.PLANTABLE && plantableCount > getCurrentPlantableSlot(harvestRod)) {
			setCurrentPlantableSlot(harvestRod, (byte) (getCurrentPlantableSlot(harvestRod) + 1));
			return;
		}
		Mode nextMode = currentMode.next();
		if (nextMode == Mode.PLANTABLE) {
			if (plantableCount == 0) {
				nextMode = nextMode.next();
			} else {
				setCurrentPlantableSlot(harvestRod, (byte) 1);
			}
		}
		setMode(harvestRod, nextMode);
	}

	public int getCountPlantable(ItemStack harvestRod) {
		return getFromHandler(harvestRod, HarvestRodItemStackHandler::getCountPlantable).orElse(0);
	}

	public byte getCurrentPlantableSlot(ItemStack stack) {
		//noinspection ConstantConditions
		return stack.hasTag() ? stack.getTag().getByte(PLANTABLE_INDEX_NBT_TAG) : (byte) -1;
	}

	private void setCurrentPlantableSlot(ItemStack stack, byte index) {
		if (stack.hasTag()) {
			//noinspection ConstantConditions
			stack.getTag().putByte(PLANTABLE_INDEX_NBT_TAG, index);
		}
	}

	private void setMode(ItemStack stack, Mode mode) {
		NBTHelper.putString(MODE_NBT_TAG, stack, mode.getSerializedName());
	}

	public Mode getMode(ItemStack stack) {
		return NBTHelper.getEnumConstant(stack, MODE_NBT_TAG, Mode::valueOf).orElse(Mode.BONE_MEAL);
	}

	public int getPlantableQuantity(ItemStack harvestRod, int slot) {
		return getFromHandler(harvestRod, h -> h.getTotalAmount(slot)).orElse(0);
	}

	public void setPlantableQuantity(ItemStack harvestRod, byte plantableSlot, int quantity) {
		runOnHandler(harvestRod, h -> h.setTotalCount(plantableSlot, quantity));
		if (quantity == 0) {
			shiftModeOnEmptyPlantable(harvestRod, plantableSlot);
			return;
		}
		getFromHandler(harvestRod, h -> h.getStackInSlot(plantableSlot));
	}

	public enum Mode implements StringRepresentable {
		BONE_MEAL, PLANTABLE, HOE;

		@Override
		public String getSerializedName() {
			return name();
		}

		public Mode next() {
			return VALUES[(ordinal() + 1) % VALUES.length];
		}

		public Mode previous() {
			return VALUES[Math.floorMod(ordinal() - 1, VALUES.length)];
		}

		private static final Mode[] VALUES;

		static {
			ImmutableMap.Builder<String, Mode> builder = new ImmutableMap.Builder<>();
			for (Mode value : Mode.values()) {
				builder.put(value.getSerializedName(), value);
			}
			VALUES = values();
		}
	}
}