package com.mysteria.mysteryuniverse.systems.starcrafting;

import com.mysteria.customapi.items.CustomItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class StarCraftingRecipe implements Recipe {

	private final NamespacedKey key;
	private final ItemStack output;
	private String[] rows;
	private Map<Character, ItemStack> ingredients = new HashMap<>();

	public StarCraftingRecipe(@Nonnull NamespacedKey key, @Nonnull ItemStack result) {
		this.key = key;
		this.output = new ItemStack(result);
	}

	public StarCraftingRecipe shape(final @Nonnull String... shape) {
		Validate.notNull(shape, "Must provide a shape");
		Validate.isTrue(shape.length > 0 && shape.length < 4, "Crafting recipes should be 1-3 rows, not ", shape.length);

		int lastLen = -1;
		for (String row : shape) {
			Validate.notNull(row, "Shape cannot have null rows");
			Validate.isTrue(row.length() > 0 && row.length() < 4, "Crafting rows should be 1-3 characters, not ", row.length());

			Validate.isTrue(lastLen == -1 || lastLen == row.length(), "Crafting recipes must be rectangular");
			lastLen = row.length();
		}
		this.rows = new String[shape.length];
		for (int i = 0; i < shape.length; i++) {
			this.rows[i] = shape[i];
		}

		HashMap<Character, ItemStack> newIngredients = new HashMap<>();
		for (String row : shape) {
			for (Character c : row.toCharArray()) {
				newIngredients.put(c, ingredients.get(c));
			}
		}
		// to make this easier
		newIngredients.put(' ', null);
		this.ingredients = newIngredients;
		return this;
	}

	public StarCraftingRecipe setIngredient(char key, @Nonnull Material material) {
		Validate.isTrue(ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
		Validate.isTrue(material != Material.AIR, "Recipe material cannot be air:");
		ingredients.put(key, new ItemStack(material));
		return this;
	}

	public StarCraftingRecipe setIngredient(char key, @Nonnull ItemStack ingredient) {
		Validate.isTrue(ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
		ingredients.put(key, new ItemStack(ingredient));
		return this;
	}

	public StarCraftingRecipe setIngredient(char key, @Nonnull CustomItem ingredient) {
		Validate.isTrue(ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
		ingredients.put(key, ingredient.getItemStack());
		return this;
	}

	public StarCraftingRecipe setIngredient(char key, @Nonnull CustomItem ingredient, int amount) {
		Validate.isTrue(ingredients.containsKey(key), "Symbol does not appear in the shape:", key);
		ingredients.put(key, ingredient.getItemStack(amount));
		return this;
	}

	@Nonnull
	public ItemStack getResult() {
		return this.output;
	}

	public NamespacedKey getKey() {
		return key;
	}

	@Nonnull
	public String[] getShape() {
		return rows.clone();
	}

	public Map<Character, ItemStack> getIngredientMap() {
		return ingredients;
	}


	public boolean match(@Nonnull ItemStack[][] input) {
		for (int i = 0; i < rows.length; i++) {
			for (int j = 0; j < rows[i].length(); j++) {
				ItemStack itemThere = ingredients.get(rows[i].charAt(j));
				if (itemThere == null) {
					if (input[i][j] != null) return false;
				} else if (!itemThere.isSimilar(input[i][j])){
					return false;
				} else if (input[i][j] != null && input[i][j].getAmount() < itemThere.getAmount()) {
					return false;
				}
			}
		}

		return true;
	}

	@Nullable
	public ItemStack getItemStack(int row, int column) {
		try {
			return this.ingredients.get(rows[row].charAt(column));
		} catch (Exception ignored) {
			return null;
		}
	}

}
