package reliquary.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import reliquary.reference.Reference;

import java.util.Map;
import java.util.Set;

class BlockLootSubProvider extends net.minecraft.data.loot.BlockLootSubProvider {
	protected BlockLootSubProvider() {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags());
	}

	@Override
	public void generate() {
		BuiltInRegistries.BLOCK.entrySet().stream()
				.filter(e -> e.getKey().location().getNamespace().equals(Reference.MOD_ID))
				.map(Map.Entry::getValue).forEach(this::dropSelf);
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return BuiltInRegistries.BLOCK.entrySet().stream()
				.filter(e -> e.getKey().location().getNamespace().equals(Reference.MOD_ID))
				.map(Map.Entry::getValue)
				.toList();
	}
}
