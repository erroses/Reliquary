package reliquary.crafting.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import reliquary.reference.Config;

public class PotionsEnabledCondition implements ICondition {
	private static final PotionsEnabledCondition INSTANCE = new PotionsEnabledCondition();
	public static final Codec<PotionsEnabledCondition> CODEC = MapCodec.unit(INSTANCE).stable().codec();

	@Override
	public boolean test(IContext context) {
		return !Config.COMMON.disable.disablePotions.get();
	}

	@Override
	public Codec<? extends ICondition> codec() {
		return CODEC;
	}
}
