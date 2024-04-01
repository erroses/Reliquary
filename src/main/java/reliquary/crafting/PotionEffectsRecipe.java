package reliquary.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import reliquary.init.ModItems;
import reliquary.items.util.IPotionItem;
import reliquary.util.potions.XRPotionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PotionEffectsRecipe implements CraftingRecipe {
	private final ShapedRecipePattern pattern;
	private final ItemStack result;
	private final String group;
	private final float potionDurationFactor;

	public PotionEffectsRecipe(String group, ShapedRecipePattern pattern, ItemStack result, float potionDurationFactor) {
		this.group = group;
		this.pattern = pattern;
		this.result = result;
		this.potionDurationFactor = potionDurationFactor;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		ItemStack newOutput = result.copy();

		findMatchAndUpdateEffects(inv).ifPresent(targetEffects -> XRPotionHelper.addPotionEffectsToStack(newOutput, targetEffects));

		return newOutput;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= pattern.width() && height >= pattern.height();
	}

	private Optional<List<MobEffectInstance>> findMatchAndUpdateEffects(CraftingContainer inv) {
		List<MobEffectInstance> targetEffects;
		for (int startX = 0; startX <= inv.getWidth() - pattern.width(); startX++) {
			for (int startY = 0; startY <= inv.getHeight() - pattern.height(); ++startY) {
				targetEffects = new ArrayList<>();
				if (checkMatchAndUpdateEffects(inv, targetEffects, startX, startY, false)) {
					return Optional.of(targetEffects);
				}
				targetEffects = new ArrayList<>();
				if (checkMatchAndUpdateEffects(inv, targetEffects, startX, startY, true)) {
					return Optional.of(targetEffects);
				}
			}
		}
		return Optional.empty();
	}

	private boolean checkMatchAndUpdateEffects(CraftingContainer inv, List<MobEffectInstance> targetEffects, int startX, int startY, boolean mirror) {
		for (int x = 0; x < pattern.width(); x++) {
			for (int y = 0; y < pattern.height(); y++) {
				int subX = x - startX;
				int subY = y - startY;

				Ingredient target = getTarget(subX, subY, mirror);

				if (target.test(inv.getItem(x + y * inv.getWidth()))) {
					updateTargetEffects(inv, targetEffects, x, y);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level world) {
		for (int x = 0; x <= inv.getWidth() - pattern.width(); x++) {
			for (int y = 0; y <= inv.getHeight() - pattern.height(); ++y) {
				if (checkMatch(inv, x, y, false)) {
					return true;
				}

				if (checkMatch(inv, x, y, true)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean checkMatch(CraftingContainer inv, int startX, int startY, boolean mirror) {
		List<MobEffectInstance> targetEffects = new ArrayList<>();
		for (int x = 0; x < inv.getWidth(); x++) {
			for (int y = 0; y < inv.getHeight(); y++) {
				int subX = x - startX;
				int subY = y - startY;

				Ingredient target = getTarget(subX, subY, mirror);

				if (!target.test(inv.getItem(x + y * inv.getWidth()))) {
					return false;
				}
				if (!updateTargetEffects(inv, targetEffects, x, y)) {
					return false;
				}

			}
		}
		return true;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return result;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return pattern.ingredients();
	}

	private Ingredient getTarget(int subX, int subY, boolean mirror) {
		if (subX >= 0 && subY >= 0 && subX < pattern.width() && subY < pattern.height()) {
			if (mirror) {
				return pattern.ingredients().get(pattern.width() - subX - 1 + subY * pattern.width());
			} else {
				return pattern.ingredients().get(subX + subY * pattern.width());
			}
		}
		return Ingredient.EMPTY;
	}

	private boolean updateTargetEffects(CraftingContainer inv, List<MobEffectInstance> targetEffects, int x, int y) {
		ItemStack invStack = inv.getItem(x + y * inv.getWidth());
		if (invStack.getItem() instanceof IPotionItem potionItem) {
			List<MobEffectInstance> effects = potionItem.getEffects(invStack);
			if (effects.isEmpty()) {
				return true;
			}

			if (targetEffects.isEmpty()) {
				targetEffects.addAll(XRPotionHelper.changePotionEffectsDuration(effects, potionDurationFactor));
			} else {
				return XRPotionHelper.changePotionEffectsDuration(effects, potionDurationFactor).equals(targetEffects); // Two items with different MobEffects marked as to be copied
			}
		}
		return true;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.POTION_EFFECTS_SERIALIZER.get();
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	public static class Serializer implements RecipeSerializer<PotionEffectsRecipe> {
		private final Codec<PotionEffectsRecipe> codec = RecordCodecBuilder.create(
				instance -> instance.group(
								ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(recipe -> recipe.group),
								ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
								ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
								Codec.FLOAT.fieldOf("duration_factor").forGetter(recipe -> recipe.potionDurationFactor)
						)
						.apply(instance, PotionEffectsRecipe::new));

		@Override
		public Codec<PotionEffectsRecipe> codec() {
			return codec;
		}

		@Override
		public PotionEffectsRecipe fromNetwork(FriendlyByteBuf buffer) {
			return new PotionEffectsRecipe(buffer.readUtf(), ShapedRecipePattern.fromNetwork(buffer), buffer.readItem(), buffer.readFloat());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, PotionEffectsRecipe recipe) {
			buffer.writeUtf(recipe.group);
			recipe.pattern.toNetwork(buffer);
			buffer.writeItem(recipe.result);
			buffer.writeFloat(recipe.potionDurationFactor);
		}
	}
}
