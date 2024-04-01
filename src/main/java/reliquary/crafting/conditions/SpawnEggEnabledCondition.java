package reliquary.crafting.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import reliquary.reference.Config;

public class SpawnEggEnabledCondition implements ICondition {
	private static final SpawnEggEnabledCondition INSTANCE = new SpawnEggEnabledCondition();
	public static final Codec<SpawnEggEnabledCondition> CODEC = MapCodec.unit(INSTANCE).stable().codec();

	@Override
	public boolean test(IContext context) {
		return !Config.COMMON.disable.disableSpawnEggRecipes.get();
	}

	@Override
	public Codec<? extends ICondition> codec() {
		return CODEC;
	}
}
