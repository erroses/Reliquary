package reliquary.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import reliquary.reference.Reference;

import java.util.function.Supplier;

public class ModSounds {
	private ModSounds() {
	}

	private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Reference.MOD_ID);

	public static final Supplier<SoundEvent> BOOK = SOUND_EVENTS.register("book", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MOD_ID, "book")));
	public static final Supplier<SoundEvent> HANDGUN_LOAD = SOUND_EVENTS.register("handgun_load", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MOD_ID, "handgun_load")));
	public static final Supplier<SoundEvent> HANDGUN_SHOT = SOUND_EVENTS.register("handgun_shot", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MOD_ID, "handgun_shot")));

	public static void registerListeners(IEventBus modBus) {
		SOUND_EVENTS.register(modBus);
	}
}
