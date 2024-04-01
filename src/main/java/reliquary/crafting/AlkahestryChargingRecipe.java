package reliquary.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import reliquary.init.ModItems;
import reliquary.items.AlkahestryTomeItem;

public class AlkahestryChargingRecipe implements CraftingRecipe {
	private final Ingredient chargingIngredient;
	private final int chargeToAdd;
	private final ItemStack recipeOutput;
	private final Ingredient tomeIngredient;

	public AlkahestryChargingRecipe(Ingredient chargingIngredient, int chargeToAdd) {
		this.chargingIngredient = chargingIngredient;
		this.chargeToAdd = chargeToAdd;
		tomeIngredient = Ingredient.of(AlkahestryTomeItem.setCharge(new ItemStack(ModItems.ALKAHESTRY_TOME.get()), 0));

		recipeOutput = new ItemStack(ModItems.ALKAHESTRY_TOME.get());
		AlkahestryTomeItem.addCharge(recipeOutput, chargeToAdd);

		AlkahestryRecipeRegistry.registerChargingRecipe(this);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		boolean hasTome = false;
		boolean hasIngredient = false;

		for (int x = 0; x < inv.getContainerSize(); x++) {
			ItemStack slotStack = inv.getItem(x);

			if (!slotStack.isEmpty()) {
				boolean inRecipe = false;
				if (chargingIngredient.test(slotStack)) {
					inRecipe = true;
					hasIngredient = true;
				} else if (!hasTome && slotStack.getItem() == ModItems.ALKAHESTRY_TOME.get() && AlkahestryTomeItem.getCharge(slotStack) + chargeToAdd <= AlkahestryTomeItem.getChargeLimit()) {
					inRecipe = true;
					hasTome = true;
				}

				if (!inRecipe) {
					return false;
				}
			}
		}

		return hasIngredient && hasTome;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		int numberOfIngredients = 0;
		ItemStack tome = ItemStack.EMPTY;
		for (int slot = 0; slot < inv.getContainerSize(); slot++) {
			ItemStack stack = inv.getItem(slot);
			if (chargingIngredient.test(stack)) {
				numberOfIngredients++;
			} else if (stack.getItem() == ModItems.ALKAHESTRY_TOME.get()) {
				tome = stack.copy();
			}
		}

		AlkahestryTomeItem.addCharge(tome, chargeToAdd * numberOfIngredients);

		return tome;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, chargingIngredient, tomeIngredient);
	}

	public ItemStack getRecipeOutput() {
		return recipeOutput;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registryAccess) {
		return recipeOutput;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModItems.ALKAHESTRY_CHARGING_SERIALIZER.get();
	}

	public int getChargeToAdd() {
		return chargeToAdd;
	}

	public Ingredient getChargingIngredient() {
		return chargingIngredient;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.MISC;
	}

	public static class Serializer implements RecipeSerializer<AlkahestryChargingRecipe> {
		private final Codec<AlkahestryChargingRecipe> codec = RecordCodecBuilder.create(
				instance -> instance.group(
								Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.chargingIngredient),
								Codec.INT.fieldOf("charge").forGetter(recipe -> recipe.chargeToAdd)
						)
						.apply(instance, AlkahestryChargingRecipe::new));

		@Override
		public Codec<AlkahestryChargingRecipe> codec() {
			return codec;
		}

		@Override
		public AlkahestryChargingRecipe fromNetwork(FriendlyByteBuf pBuffer) {
			return new AlkahestryChargingRecipe(Ingredient.fromNetwork(pBuffer), pBuffer.readInt());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, AlkahestryChargingRecipe recipe) {
			recipe.chargingIngredient.toNetwork(buffer);
			buffer.writeInt(recipe.chargeToAdd);
		}
	}
}
