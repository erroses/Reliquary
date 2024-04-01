package reliquary.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import reliquary.potions.CurePotion;
import reliquary.potions.FlightPotion;
import reliquary.potions.PacificationPotion;
import reliquary.reference.Reference;

import java.util.function.Supplier;

public class ModPotions {
	private ModPotions() {}

	private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Reference.MOD_ID);

	public static Supplier<MobEffect> FLIGHT_POTION = MOB_EFFECTS.register("flight", FlightPotion::new);
	public static Supplier<MobEffect> PACIFICATION_POTION = MOB_EFFECTS.register("pacification", PacificationPotion::new);
	public static Supplier<MobEffect> CURE_POTION = MOB_EFFECTS.register("cure", CurePotion::new);

	public static void registerListeners(IEventBus modBus) {
		MOB_EFFECTS.register(modBus);
	}
}
