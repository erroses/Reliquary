package reliquary.crafting.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import reliquary.reference.Config;

public class MobDropsCraftableCondition implements ICondition {
	private static final MobDropsCraftableCondition INSTANCE = new MobDropsCraftableCondition();
	public static final Codec<MobDropsCraftableCondition> CODEC = MapCodec.unit(INSTANCE).stable().codec();

	@Override
	public boolean test(IContext context) {
		return Config.COMMON.dropCraftingRecipesEnabled.get();
	}

	@Override
	public Codec<? extends ICondition> codec() {
		return CODEC;
	}
}
