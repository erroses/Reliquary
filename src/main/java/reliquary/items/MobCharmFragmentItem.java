package reliquary.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import reliquary.util.NBTHelper;

import java.util.function.Consumer;

public class MobCharmFragmentItem extends ItemBase {
	public MobCharmFragmentItem() {
		super(new Properties());
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		for (String entityRegistryName : MobCharmRegistry.getRegisteredNames()) {
			itemConsumer.accept(getStackFor(entityRegistryName));
		}
	}

	public ItemStack getStackFor(String entityRegistryName) {
		ItemStack ret = new ItemStack(this);
		NBTHelper.putString("entity", ret, entityRegistryName);
		return ret;
	}

	public static String getEntityRegistryName(ItemStack charm) {
		return NBTHelper.getString("entity", charm);
	}

	public static ResourceLocation getEntityEggRegistryName(ItemStack charm) {
		return new ResourceLocation(getEntityRegistryName(charm));
	}

	@Override
	public MutableComponent getName(ItemStack stack) {
		ResourceLocation entityEggRegistryName = getEntityEggRegistryName(stack);
		return BuiltInRegistries.ENTITY_TYPE.getOptional(entityEggRegistryName)
				.map(entityType -> Component.translatable(getDescriptionId(), entityType.getDescription().getString()).withStyle(ChatFormatting.GREEN))
				.orElseGet(() -> super.getName(stack));
	}
}
