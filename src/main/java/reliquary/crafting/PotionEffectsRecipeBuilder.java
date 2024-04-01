package reliquary.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import reliquary.crafting.conditions.PotionsEnabledCondition;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PotionEffectsRecipeBuilder {
	private final Item result;
	private final int count;
	private final List<String> rows = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
	private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
	@Nullable
	private String group;
	private final float durationFactor;

	private PotionEffectsRecipeBuilder(ItemLike result, int count, float durationFactor) {
		this.result = result.asItem();
		this.count = count;
		this.durationFactor = durationFactor;
	}

	public static PotionEffectsRecipeBuilder potionEffectsRecipe(ItemLike item, int count, float durationFactor) {
		return new PotionEffectsRecipeBuilder(item, count, durationFactor);
	}

	public PotionEffectsRecipeBuilder define(Character pSymbol, TagKey<Item> pTag) {
		return define(pSymbol, Ingredient.of(pTag));
	}

	public PotionEffectsRecipeBuilder define(Character pSymbol, ItemLike pItem) {
		return define(pSymbol, Ingredient.of(pItem));
	}

	public PotionEffectsRecipeBuilder define(Character pSymbol, Ingredient pIngredient) {
		if (key.containsKey(pSymbol)) {
			throw new IllegalArgumentException("Symbol '" + pSymbol + "' is already defined!");
		} else if (pSymbol == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			key.put(pSymbol, pIngredient);
			return this;
		}
	}

	public PotionEffectsRecipeBuilder unlockedBy(String pName, Criterion<?> pCriterion) {
		criteria.put(pName, pCriterion);
		return this;
	}

	public PotionEffectsRecipeBuilder pattern(String pPattern) {
		if (!rows.isEmpty() && pPattern.length() != rows.get(0).length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			rows.add(pPattern);
			return this;
		}
	}

	public PotionEffectsRecipeBuilder group(@Nullable String pGroupName) {
		group = pGroupName;
		return this;
	}

	public void save(RecipeOutput recipeOutput, ResourceLocation id) {
		Advancement.Builder advancementBuilder = recipeOutput.advancement()
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
				.rewards(AdvancementRewards.Builder.recipe(id))
				.requirements(AdvancementRequirements.Strategy.OR);
		criteria.forEach(advancementBuilder::addCriterion);
		recipeOutput.withConditions(new PotionsEnabledCondition()).accept(id,
				new PotionEffectsRecipe(
						Objects.requireNonNullElse(group, ""),
						ensureValid(id),
						new ItemStack(result, count),
						durationFactor
				), null);
	}

	private ShapedRecipePattern ensureValid(ResourceLocation id) {
		if (criteria.isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + id);
		} else {
			return ShapedRecipePattern.of(key, rows);
		}
	}
}
