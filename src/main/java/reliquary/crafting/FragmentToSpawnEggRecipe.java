package reliquary.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import reliquary.init.ModItems;

public class FragmentToSpawnEggRecipe extends ShapelessRecipe {
	private final ShapelessRecipe recipeDelegate;

	public FragmentToSpawnEggRecipe(ShapelessRecipe recipeDelegate) {
		super(recipeDelegate.getGroup(), CraftingBookCategory.MISC, recipeDelegate.result, recipeDelegate.getIngredients());
		this.recipeDelegate = recipeDelegate;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		return super.matches(inv, worldIn) && FragmentRecipeHelper.hasOnlyOneFragmentType(inv);
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		return FragmentRecipeHelper.getRegistryName(inv).map(FragmentRecipeHelper::getSpawnEggStack)
				.orElse(new ItemStack(FragmentRecipeHelper.FALL_BACK_SPAWN_EGG));
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.FRAGMENT_TO_SPAWN_EGG_SERIALIZER.get();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	public static class Serializer implements RecipeSerializer<FragmentToSpawnEggRecipe> {
		private final Codec<FragmentToSpawnEggRecipe> codec = RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(FragmentToSpawnEggRecipe::new, recipe -> recipe.recipeDelegate);

		@Override
		public Codec<FragmentToSpawnEggRecipe> codec() {
			return codec;
		}

		@Override
		public FragmentToSpawnEggRecipe fromNetwork(FriendlyByteBuf buffer) {
			return new FragmentToSpawnEggRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(buffer));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, FragmentToSpawnEggRecipe recipe) {
			RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe.recipeDelegate);
		}
	}
}
