package reliquary.crafting.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import reliquary.reference.Config;

public class PedestalEnabledCondition implements ICondition {
	private static final PedestalEnabledCondition INSTANCE = new PedestalEnabledCondition();
	public static final Codec<PedestalEnabledCondition> CODEC = MapCodec.unit(INSTANCE).stable().codec();

	@Override
	public boolean test(IContext context) {
		return !Config.COMMON.disable.disablePedestal.get();
	}

	@Override
	public Codec<? extends ICondition> codec() {
		return CODEC;
	}
}
