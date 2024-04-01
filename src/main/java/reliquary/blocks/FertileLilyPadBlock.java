package reliquary.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.IPlantable;
import net.neoforged.neoforge.common.PlantType;
import reliquary.items.ICreativeTabItemGenerator;
import reliquary.reference.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class FertileLilyPadBlock extends BushBlock implements ICreativeTabItemGenerator {
	public static final MapCodec<FertileLilyPadBlock> CODEC = simpleCodec(FertileLilyPadBlock::new);
	private static final Map<ResourceKey<Level>, Long> currentDimensionTicks = new HashMap<>();
	private static final Map<ResourceKey<Level>, Set<BlockPos>> dimensionPositionsTicked = new HashMap<>();
	private static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.5D, 15.0D);

	@Override
	public PlantType getPlantType(BlockGetter world, BlockPos pos) {
		return PlantType.WATER;
	}

	public FertileLilyPadBlock() {
		super(Properties.of().mapColor(MapColor.PLANT));
	}
	private FertileLilyPadBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		itemConsumer.accept(new ItemStack(this));
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);

		if (!level.isClientSide()) {
			level.scheduleTick(pos, this, 1);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		ResourceKey<Level> dimKey = world.dimension();
		if (!currentDimensionTicks.containsKey(dimKey) || currentDimensionTicks.get(dimKey) != world.getGameTime()) {
			currentDimensionTicks.put(dimKey, world.getGameTime());
			dimensionPositionsTicked.put(dimKey, new HashSet<>());
		} else if (dimensionPositionsTicked.computeIfAbsent(dimKey, k -> new HashSet<>()).contains(pos)) {
			return;
		}
		growCropsNearby(world, pos, state);
		dimensionPositionsTicked.computeIfAbsent(dimKey, k -> new HashSet<>()).add(pos);
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
		world.addParticle(ParticleTypes.ENTITY_EFFECT, pos.getX() + 0.5D + rand.nextGaussian() / 8, pos.getY(), pos.getZ() + 0.5D + rand.nextGaussian() / 8, 0.0D, 0.9D, 0.5D);
	}

	private int secondsBetweenGrowthTicks() {
		return Config.COMMON.blocks.fertileLilypad.secondsBetweenGrowthTicks.get();
	}

	private int tileRange() {
		return Config.COMMON.blocks.fertileLilypad.tileRange.get();
	}

	private int fullPotencyRange() {
		return Config.COMMON.blocks.fertileLilypad.fullPotencyRange.get();
	}

	@SuppressWarnings("deprecation")
	private void growCropsNearby(ServerLevel world, BlockPos pos, BlockState state) {
		BlockPos.betweenClosed(pos.offset(-tileRange(), -1, -tileRange()), pos.offset(tileRange(), tileRange(), tileRange())).forEach(cropPos -> {
			if (!world.hasChunkAt(cropPos)) {
				return;
			}
			BlockState cropState = world.getBlockState(cropPos);
			Block cropBlock = cropState.getBlock();

			if (isAllowedCropBlock(cropBlock) && (cropBlock instanceof IPlantable || cropBlock instanceof BonemealableBlock) && !(cropBlock instanceof FertileLilyPadBlock)) {
				double distance = Math.sqrt(cropPos.distSqr(pos));
				tickCropBlock(world, cropPos, cropState, cropBlock, distance);
			}
		});
		world.scheduleTick(pos, state.getBlock(), secondsBetweenGrowthTicks() * 20);
	}

	private boolean isAllowedCropBlock(Block cropBlock) {
		return cropBlock != Blocks.GRASS_BLOCK && !(cropBlock instanceof DoublePlantBlock);
	}

	@SuppressWarnings("deprecation")
	private void tickCropBlock(ServerLevel world, BlockPos cropPos, BlockState cropState, Block cropBlock, double distance) {
		distance -= fullPotencyRange();
		distance = Math.max(1D, distance);
		double distanceCoefficient = 1D - (distance / tileRange());

		//it schedules the next tick.
		world.scheduleTick(cropPos, cropBlock, (int) (distanceCoefficient * secondsBetweenGrowthTicks() * 20F));
		cropBlock.randomTick(cropState, world, cropPos, world.random);
		world.levelEvent(2005, cropPos, Math.max((int) (tileRange() - distance), 1));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		super.entityInside(state, worldIn, pos, entityIn);
		if (entityIn instanceof Boat) {
			worldIn.destroyBlock(pos, true);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AABB;
	}

	@Override
	protected MapCodec<? extends BushBlock> codec() {
		return CODEC;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
		FluidState ifluidstate = worldIn.getFluidState(pos);
		return ifluidstate.getType() == Fluids.WATER || state.getBlock() instanceof IceBlock;
	}
}
