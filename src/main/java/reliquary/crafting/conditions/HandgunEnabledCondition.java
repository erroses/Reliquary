package reliquary.crafting.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import reliquary.reference.Config;

public class HandgunEnabledCondition implements ICondition {
	private static final HandgunEnabledCondition INSTANCE = new HandgunEnabledCondition();
	public static final Codec<HandgunEnabledCondition> CODEC = MapCodec.unit(INSTANCE).stable().codec();;

	@Override
	public boolean test(IContext context) {
		return !Config.COMMON.disable.disableHandgun.get();
	}

	@Override
	public Codec<? extends ICondition> codec() {
		return CODEC;
	}

	@Override
	public String toString() {
		return "handgun_enabled";
	}
}
