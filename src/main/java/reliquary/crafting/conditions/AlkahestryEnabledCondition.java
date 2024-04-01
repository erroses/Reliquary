package reliquary.crafting.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import reliquary.reference.Config;

public class AlkahestryEnabledCondition implements ICondition {
	private static final AlkahestryEnabledCondition INSTANCE = new AlkahestryEnabledCondition();
	public static final Codec<AlkahestryEnabledCondition> CODEC = MapCodec.unit(INSTANCE).stable().codec();

	@Override
	public boolean test(IContext context) {
		return !Config.COMMON.disable.disableAlkahestry.get();
	}

	@Override
	public Codec<? extends ICondition> codec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "alkahestry_enabled";
	}
}
