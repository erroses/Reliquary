package reliquary.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.ITeleporter;
import reliquary.entities.EnderStaffProjectileEntity;
import reliquary.init.ModBlocks;
import reliquary.items.util.FilteredItemStackHandler;
import reliquary.items.util.IScrollableItem;
import reliquary.reference.Config;
import reliquary.util.NBTHelper;
import reliquary.util.TooltipBuilder;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

public class EnderStaffItem extends ToggleableItem implements IScrollableItem {
	private static final String DIMENSION_TAG = "dimensionID";
	private static final String NODE_X_TAG = "nodeX";
	private static final String NODE_Y_TAG = "nodeY";
	private static final String NODE_Z_TAG = "nodeZ";

	public EnderStaffItem() {
		super(new Properties().stacksTo(1).setNoRepair().rarity(Rarity.EPIC));
	}

	private int getEnderStaffPearlCost() {
		return Config.COMMON.items.enderStaff.enderPearlCastCost.get();
	}

	private int getEnderStaffNodeWarpCost() {
		return Config.COMMON.items.enderStaff.enderPearlNodeWarpCost.get();
	}

	private int getEnderPearlWorth() {
		return Config.COMMON.items.enderStaff.enderPearlWorth.get();
	}

	private int getEnderPearlLimit() {
		return Config.COMMON.items.enderStaff.enderPearlLimit.get();
	}

	private int getNodeWarpCastTime() {
		return Config.COMMON.items.enderStaff.nodeWarpCastTime.get();
	}

	public Mode getMode(ItemStack stack) {
		return NBTHelper.getEnumConstant(stack, "mode", Mode::fromName).orElse(Mode.CAST);
	}

	private void setMode(ItemStack stack, Mode mode) {
		NBTHelper.putString("mode", stack, mode.getSerializedName());
	}

	private void cycleMode(ItemStack stack, boolean next) {
		if (next) {
			setMode(stack, getMode(stack).next());
		} else {
			setMode(stack, getMode(stack).previous());
		}
	}

	@Override
	public InteractionResult onMouseScrolled(ItemStack stack, Player player, double scrollDelta) {
		if (player.level().isClientSide) {
			return InteractionResult.PASS;
		}
		cycleMode(stack, scrollDelta > 0);
		return InteractionResult.SUCCESS;
	}

	@Override
	public void inventoryTick(ItemStack staff, Level world, Entity entity, int itemSlot, boolean isSelected) {
		if (world.isClientSide || world.getGameTime() % 10 != 0) {
			return;
		}

		if (!(entity instanceof Player player)) {
			return;
		}

		if (!isEnabled(staff)) {
			return;
		}

		int pearlCharge = getPearlCount(staff);
		consumeAndCharge(player, getEnderPearlLimit() - pearlCharge, getEnderPearlWorth(), Items.ENDER_PEARL, 16,
				chargeToAdd -> setPearlCount(staff, pearlCharge + chargeToAdd));
	}

	private void setPearlCount(ItemStack stack, int count) {
		if (!(stack.getCapability(Capabilities.ItemHandler.ITEM) instanceof FilteredItemStackHandler filteredHandler)) {
			return;
		}
		filteredHandler.setTotalCount(0, count);
	}

	public int getPearlCount(ItemStack staff) {
		if (!(staff.getCapability(Capabilities.ItemHandler.ITEM) instanceof FilteredItemStackHandler filteredHandler)) {
			return 0;
		}
		return filteredHandler.getTotalAmount(0);
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
		if (!(livingEntity instanceof Player player)) {
			return;
		}

		for (int particles = 0; particles < 2; particles++) {
			level.addParticle(ParticleTypes.PORTAL, player.getX(), player.getEyeY(), player.getZ(), player.level().random.nextGaussian(), player.level().random.nextGaussian(), player.level().random.nextGaussian());
		}
		if (remainingUseDuration == 1) {
			player.releaseUsingItem();
		}
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return getNodeWarpCastTime();
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		if (!(entityLiving instanceof Player player)) {
			return;
		}

		if (timeLeft == 1) {
			doWraithNodeWarpCheck(stack, player.level(), player);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!player.isShiftKeyDown()) {
			if (getMode(stack) == Mode.CAST || getMode(stack) == Mode.LONG_CAST) {
				if (getPearlCount(stack) < getEnderStaffPearlCost() && !player.isCreative()) {
					return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
				}
				shootEnderStaffProjectile(world, player, hand, stack);
			} else {
				player.startUsingItem(hand);
			}
		}
		return super.use(world, player, hand);
	}

	private void shootEnderStaffProjectile(Level world, Player player, InteractionHand hand, ItemStack stack) {
		player.swing(hand);
		player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
		if (!player.level().isClientSide) {
			EnderStaffProjectileEntity enderStaffProjectile = new EnderStaffProjectileEntity(player.level(), player, getMode(stack) != Mode.LONG_CAST);
			enderStaffProjectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
			player.level().addFreshEntity(enderStaffProjectile);
			if (!player.isCreative()) {
				setPearlCount(stack, getPearlCount(stack) - getEnderStaffPearlCost());
			}
		}
	}

	private void doWraithNodeWarpCheck(ItemStack stack, Level level, Player player) {
		CompoundTag tag = stack.getTag();
		if (tag == null || (getPearlCount(stack) < getEnderStaffNodeWarpCost() && !player.isCreative())) {
			return;
		}

		String wraithNodeDimension = tag.getString(DIMENSION_TAG);
		BlockPos wraithNodePos = new BlockPos(tag.getInt(NODE_X_TAG), tag.getInt(NODE_Y_TAG), tag.getInt(NODE_Z_TAG));
		if (!player.level().dimension().location().toString().equals(wraithNodeDimension) && player.level() instanceof ServerLevel serverLevel) {
			ServerLevel destination = serverLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(wraithNodeDimension)));
			if (destination != null && canTeleport(destination, wraithNodePos)) {
				teleportToDimension(player, destination, wraithNodePos);
				if (!player.isCreative() && !player.level().isClientSide) {
					setPearlCount(stack, getPearlCount(stack) - getEnderStaffNodeWarpCost());
				}
			}
		} else {
			if (canTeleport(level, wraithNodePos)) {
				teleportPlayer(level, wraithNodePos, player);
				if (!player.isCreative() && !player.level().isClientSide) {
					setPearlCount(stack, getPearlCount(stack) - getEnderStaffNodeWarpCost());
				}
			}
		}
	}

	private static void teleportToDimension(Player player, ServerLevel destination, BlockPos wraithNodePos) {
		player.changeDimension(destination, new WraithNodeTeleporter() {
			@Override
			public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
				return new PortalInfo(new Vec3(wraithNodePos.getX() + 0.5, wraithNodePos.getY() + 0.875, wraithNodePos.getZ() + 0.5), Vec3.ZERO, entity.getYRot(), entity.getXRot());
			}

			@Override
			public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
				return repositionEntity.apply(false);
			}
		});
	}

	private static boolean canTeleport(Level level, BlockPos pos) {
		if (level.getBlockState(pos).getBlock() != ModBlocks.WRAITH_NODE.get()) {
			return false;
		}

		BlockPos up = pos.above();
		return level.isEmptyBlock(up) && level.isEmptyBlock(up.above());
	}

	private abstract static class WraithNodeTeleporter implements ITeleporter {
		@Override
		public boolean isVanilla() {
			return false;
		}

		@Override
		public boolean playTeleportSound(ServerPlayer player, ServerLevel sourceWorld, ServerLevel destWorld) {
			return false;
		}
	}

	private static void teleportPlayer(Level world, BlockPos pos, Player player) {
		player.teleportTo(pos.getX() + 0.5, pos.getY() + 0.875, pos.getZ() + 0.5);
		player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
		for (int particles = 0; particles < 2; particles++) {
			world.addParticle(ParticleTypes.PORTAL, player.getX(), player.getEyeY(), player.getZ(), world.random.nextGaussian(), world.random.nextGaussian(), world.random.nextGaussian());
		}
	}

	@Override
	protected void addMoreInformation(ItemStack staff, @Nullable Level world, TooltipBuilder tooltipBuilder) {
		tooltipBuilder.description(this, ".tooltip2");
		tooltipBuilder.charge(this, ".tooltip.charge", getPearlCount(staff));

		if (staff.getTag() != null && staff.getTag().contains(NODE_X_TAG) && staff.getTag().contains(NODE_Y_TAG) && staff.getTag().contains(NODE_Z_TAG)) {
			tooltipBuilder.data(this, ".tooltip.position",
					staff.getTag().getInt(NODE_X_TAG),
					staff.getTag().getInt(NODE_Y_TAG),
					staff.getTag().getInt(NODE_Z_TAG),
					staff.getTag().getString(DIMENSION_TAG)
			);
		} else {
			tooltipBuilder.description(this, ".tooltip.position.nowhere");
		}

		if (isEnabled(staff)) {
			tooltipBuilder.absorbActive(Items.ENDER_PEARL.getName(new ItemStack(Items.ENDER_PEARL)).getString());
		} else {
			tooltipBuilder.absorb();
		}
	}

	@Override
	protected boolean hasMoreInformation(ItemStack stack) {
		return true;
	}

	@Override
	public InteractionResult useOn(UseOnContext itemUseContext) {
		ItemStack stack = itemUseContext.getItemInHand();
		Level world = itemUseContext.getLevel();
		BlockPos pos = itemUseContext.getClickedPos();

		// if right clicking on a wraith node, bind the eye to that wraith node.
		if (world.getBlockState(pos).getBlock() == ModBlocks.WRAITH_NODE.get()) {
			setWraithNode(stack, pos, getDimension(world));

			Player player = itemUseContext.getPlayer();
			if (player != null) {
				player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
			}
			for (int particles = 0; particles < 12; particles++) {
				world.addParticle(ParticleTypes.PORTAL, pos.getX() + world.random.nextDouble(), pos.getY() + world.random.nextDouble(), pos.getZ() + world.random.nextDouble(), world.random.nextGaussian(), world.random.nextGaussian(), world.random.nextGaussian());
			}
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}

	private String getDimension(@Nullable Level world) {
		return world != null ? world.dimension().location().toString() : Level.OVERWORLD.location().toString();
	}

	private void setWraithNode(ItemStack eye, BlockPos pos, String dimension) {
		NBTHelper.putInt(NODE_X_TAG, eye, pos.getX());
		NBTHelper.putInt(NODE_Y_TAG, eye, pos.getY());
		NBTHelper.putInt(NODE_Z_TAG, eye, pos.getZ());
		NBTHelper.putString(DIMENSION_TAG, eye, dimension);
	}

	public enum Mode implements StringRepresentable {
		CAST("cast"),
		LONG_CAST("long_cast"),
		NODE_WARP("node_warp");

		private final String name;

		Mode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		public Mode next() {
			return VALUES[(ordinal() + 1) % VALUES.length];
		}

		public Mode previous() {
			return VALUES[Math.floorMod(ordinal() - 1, VALUES.length)];
		}

		private static final Map<String, Mode> NAME_VALUES;
		private static final Mode[] VALUES;

		static {
			ImmutableMap.Builder<String, Mode> builder = new ImmutableMap.Builder<>();
			for (Mode value : Mode.values()) {
				builder.put(value.getSerializedName(), value);
			}
			NAME_VALUES = builder.build();
			VALUES = values();
		}

		public static Mode fromName(String name) {
			return NAME_VALUES.getOrDefault(name, CAST);
		}
	}
}
