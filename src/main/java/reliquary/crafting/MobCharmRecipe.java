package reliquary.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import reliquary.init.ModItems;
import reliquary.items.MobCharmItem;

import java.util.HashSet;
import java.util.Set;

public class MobCharmRecipe extends ShapedRecipe {
	public static final Set<MobCharmRecipe> REGISTERED_RECIPES = new HashSet<>();

	private final ShapedRecipe compose;

	public MobCharmRecipe(ShapedRecipe compose) {
		super(compose.getGroup(), CraftingBookCategory.MISC, compose.pattern, compose.result);
		this.compose = compose;
		REGISTERED_RECIPES.add(this);
	}

	public ShapedRecipe getCompose() {
		return compose;
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		return super.matches(inv, worldIn) && FragmentRecipeHelper.hasOnlyOneFragmentType(inv);
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		ItemStack ret = super.assemble(inv, registryAccess);
		FragmentRecipeHelper.getRegistryName(inv).ifPresent(regName -> MobCharmItem.setEntityRegistryName(ret, regName));
		return ret;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.MOB_CHARM_RECIPE_SERIALIZER.get();
	}

	public static class Serializer implements RecipeSerializer<MobCharmRecipe> {
		private final Codec<MobCharmRecipe> codec = RecipeSerializer.SHAPED_RECIPE.codec().xmap(MobCharmRecipe::new, recipe -> recipe.compose);

		@Override
		public Codec<MobCharmRecipe> codec() {
			return codec;
		}

		@Override
		public MobCharmRecipe fromNetwork(FriendlyByteBuf buffer) {
			return new MobCharmRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(buffer));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, MobCharmRecipe recipe) {
			RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe.compose);
		}
	}
}
