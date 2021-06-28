package com.mysteria.mysteryuniverse.systems.recipemanager;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.systems.starcrafting.StarCraftingManager;
import com.mysteria.mysteryuniverse.systems.starcrafting.StarCraftingRecipe;
import com.mysteria.utils.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.*;

@SuppressWarnings("deprecation")
public class RecipeManager {

	private final Component name;
	private final int size;

	public RecipeManager() {
		if (MysteryUniversePlugin.getRecipeManager() != null) {
			throw new IllegalStateException();
		}
		name = Component.text("Showing Recipe: ", NamedTextColor.DARK_GRAY);
		size = 45;
		new RecipeManagerListeners();
		loadRecipes();
	}

	public boolean RecipeGUI(@Nonnull Player p, @Nonnull ItemStack itemStack) {

		Recipe foundRecipe = null;
		String recipeType = "Crafting";
		for (StarCraftingRecipe recipe : MysteryUniversePlugin.getStarCraftingManager().getRecipes()) {
			if (recipe.getResult().isSimilar(itemStack)) {
				foundRecipe = recipe;
				recipeType = "Star " + recipeType;
				break;
			}
		}
		if (foundRecipe == null) {
			Iterator<Recipe> it = Bukkit.recipeIterator();
			while (it.hasNext()) {
				Recipe recipe = it.next();
				if (recipe.getResult().isSimilar(itemStack)) {
					if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
						foundRecipe = recipe;
						recipeType = recipeType + " Table";
						break;
					}
				}
			}
		}

		if (foundRecipe == null) {
			return false;
		}

		Component name = getGUIName().append(Component.text(recipeType, NamedTextColor.DARK_GRAY));
		Inventory inv = Bukkit.createInventory(null, size, name);

		ItemStack empty = ItemBuilder.builder(CustomItem.EMPTY.getItemStack())
				.adaptGUI()
				.build();

		for (int i = 0; i < size; i++) {
			inv.setItem(i, empty);
		}

		String[] shape;
		Map<Character, RecipeChoice> ingredients;
		if (foundRecipe instanceof StarCraftingRecipe) {
			StarCraftingRecipe recipe = (StarCraftingRecipe) foundRecipe;
			shape = recipe.getShape();
			Map<Character, RecipeChoice> map = new HashMap<>();
			for (Map.Entry<Character, ItemStack> entry : recipe.getIngredientMap().entrySet()) {
				if (entry.getValue() != null) {
					map.put(entry.getKey(), new RecipeChoice.ExactChoice(entry.getValue()));
				}
			}
			ingredients = map;
		}
		else if (foundRecipe instanceof ShapedRecipe) {
			ShapedRecipe recipe = (ShapedRecipe) foundRecipe;
			shape = recipe.getShape();
			ingredients = recipe.getChoiceMap();
		}
		else {
			ShapelessRecipe recipe = (ShapelessRecipe) foundRecipe;
			Map<Character, RecipeChoice> map = new HashMap<>();
			ArrayList<Character> chars = new ArrayList<>(Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'));
			for (RecipeChoice choice : recipe.getChoiceList()) {
				map.put(chars.get(0), choice);
				chars.remove(0);
			}
			StringBuilder stringBuilder = new StringBuilder("         ");
			int i = 0;
			for (char character : map.keySet()) {
				stringBuilder.setCharAt(i, character);
				i++;
			}
			String[] shapelessShape = new String[3];
			shapelessShape[0] = stringBuilder.substring(0, 3);
			shapelessShape[1] = stringBuilder.substring(4, 6);
			shapelessShape[2] = stringBuilder.substring(7, 9);
			shape = shapelessShape;
			ingredients = map;
		}

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				setInvItem(inv, x * 9 + y + 10, ingredients, shape, x, y);
				//inv.setItem(x * 9 + y + 10, getItemStack(ingredients, shape, x, y));
			}
		}

		inv.setItem(24, foundRecipe.getResult());

		p.openInventory(inv);

		return true;

	}

	private void setInvItem(@Nonnull Inventory inv, int index, @Nonnull Map<Character, RecipeChoice> ingredients,
							@Nonnull String[] shape, int row, int column) {
		RecipeChoice choice;
		try {
			choice = ingredients.get(shape[row].charAt(column));
		} catch (Exception ignored) {
			inv.setItem(index, null);
			return;
		}
		if (choice != null) {
			if (choice instanceof RecipeChoice.MaterialChoice) {
				RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
				List<Material> list = materialChoice.getChoices();
				if (list.size() > 1) {
					inv.setItem(index, null);
					new BukkitRunnable() {
						int i = 0;
						@Override
						public void run() {
							if (inv.getViewers().size() > 0) {
								inv.setItem(index, new ItemStack(list.get(i)));
								i = (i + 1) % list.size();
							} else {
								this.cancel();
							}
						}
					}.runTaskTimer(MysteryUniversePlugin.getInstance(), 1, 30);
				} else {
					inv.setItem(index, materialChoice.getItemStack());
				}
			} else {
				inv.setItem(index, choice.getItemStack());
			}
		} else {
			inv.setItem(index, null);
		}
	}

	@Nonnull
	public Component getGUIName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	private void loadRecipes() {
		Bukkit.resetRecipes();

		/*
		Iterator<Recipe> it = Bukkit.recipeIterator();

		while (it.hasNext()) {
			Recipe recipe = it.next();
			if (recipe instanceof SmokingRecipe) {
				Bukkit.removeRecipe(((SmokingRecipe) recipe).getKey());
				Bukkit.getLogger().info(((SmokingRecipe) recipe).getKey() + "");
			}
		}
		*/

		List<String> removelist = new ArrayList<>();

		// Furnace
		removelist.add("copper_ingot_from_blasting_copper_ore");
		removelist.add("copper_ingot_from_blasting_deepslate_copper_ore");
		removelist.add("copper_ingot_from_blasting_raw_copper");
		removelist.add("copper_ingot_from_smelting_copper_ore");
		removelist.add("copper_ingot_from_smelting_deepslate_copper_ore");
		removelist.add("copper_ingot_from_smelting_raw_copper");

		removelist.add("gold_ingot_from_smelting_raw_gold");
		removelist.add("gold_ingot_from_blasting_raw_gold");
		removelist.add("gold_ingot_from_blasting_gold_ore");
		removelist.add("gold_ingot_from_smelting_gold_ore");
		removelist.add("gold_ingot_from_smelting_nether_gold_ore");
		removelist.add("gold_ingot_from_blasting_nether_gold_ore");
		removelist.add("gold_ingot_from_smelting_deepslate_gold_ore");
		removelist.add("gold_ingot_from_blasting_deepslate_gold_ore");
		removelist.add("gold_nugget_from_smelting");
		removelist.add("gold_nugget_from_blasting");

		removelist.add("iron_ingot_from_smelting_raw_iron");
		removelist.add("iron_ingot_from_blasting_raw_iron");
		removelist.add("iron_ingot_from_smelting_iron_ore");
		removelist.add("iron_ingot_from_smelting_deepslate_iron_ore");
		removelist.add("iron_ingot_from_blasting_iron_ore");
		removelist.add("iron_ingot_from_blasting_deepslate_iron_ore");
		removelist.add("iron_nugget_from_smelting");
		removelist.add("iron_nugget_from_blasting");

		removelist.add("lapis_lazuli_from_smelting_lapis_ore");
		removelist.add("lapis_lazuli_from_smelting_deepslate_lapis_ore");
		removelist.add("lapis_lazuli_from_blasting_lapis_ore");
		removelist.add("lapis_lazuli_from_blasting_deepslate_lapis_ore");

		removelist.add("redstone_from_smelting_redstone_ore");
		removelist.add("redstone_from_smelting_deepslate_redstone_ore");
		removelist.add("redstone_from_blasting_redstone_ore");
		removelist.add("redstone_from_blasting_deepslate_redstone_ore");

		removelist.add("emerald_from_smelting_emerald_ore");
		removelist.add("emerald_from_smelting_deepslate_emerald_ore");
		removelist.add("emerald_from_blasting_emerald_ore");
		removelist.add("emerald_from_blasting_deepslate_emerald_ore");

		removelist.add("diamond_from_smelting_diamond_ore");
		removelist.add("diamond_from_smelting_deepslate_diamond_ore");
		removelist.add("diamond_from_blasting_diamond_ore");
		removelist.add("diamond_from_blasting_deepslate_diamond_ore");

		removelist.add("netherite_scrap");
		removelist.add("netherite_scrap_from_blasting");
		removelist.add("netherite_ingot");

		removelist.add("green_dye");
		// Crafting Table
		removelist.add("repair_item");
		removelist.add("end_crystal");
		// Planks
		removelist.add("oak_planks");
		removelist.add("spruce_planks");
		removelist.add("birch_planks");
		removelist.add("jungle_planks");
		removelist.add("acacia_planks");
		removelist.add("dark_oak_planks");
		removelist.add("crimson_planks");
		removelist.add("warped_planks");
		// Boats
		removelist.add("oak_boat");
		removelist.add("spruce_boat");
		removelist.add("birch_boat");
		removelist.add("jungle_boat");
		removelist.add("acacia_boat");
		removelist.add("dark_oak_boat");
		// Blocks
		removelist.add("iron_block");
		removelist.add("gold_block");
		removelist.add("diamond_block");
		removelist.add("emerald_block");
		removelist.add("netherite_block");
		removelist.add("lapis_block");
		// Ores from their blocks
		removelist.add("iron_ingot_from_iron_block");
		removelist.add("gold_ingot_from_gold_block");
		removelist.add("diamond");
		removelist.add("emerald");
		removelist.add("netherite_ingot_from_netherite_block");
		removelist.add("lapis_lazuli");
		// Other
		removelist.add("blast_furnace");
		removelist.add("furnace");
		removelist.add("chest");
		removelist.add("barrel");
		removelist.add("respawn_anchor");
		removelist.add("beacon");
		removelist.add("smoker");
		removelist.add("torch");
		removelist.add("enchanting_table");
		// Tools (removed)
		removelist.add("wooden_hoe");
		// Tools
		removelist.add("crossbow");
		removelist.add("shield");
		removelist.add("stone_sword");
		removelist.add("stone_pickaxe");
		removelist.add("stone_axe");
		removelist.add("stone_shovel");
		removelist.add("stone_hoe");
		removelist.add("iron_sword");
		removelist.add("iron_pickaxe");
		removelist.add("iron_axe");
		removelist.add("iron_shovel");
		removelist.add("iron_hoe");
		removelist.add("diamond_sword");
		removelist.add("diamond_pickaxe");
		removelist.add("diamond_axe");
		removelist.add("diamond_shovel");
		removelist.add("diamond_hoe");
		removelist.add("netherite_sword_smithing");
		removelist.add("netherite_pickaxe_smithing");
		removelist.add("netherite_axe_smithing");
		removelist.add("netherite_shovel_smithing");
		removelist.add("netherite_hoe_smithing");
		removelist.add("netherite_helmet_smithing");
		removelist.add("netherite_chestplate_smithing");
		removelist.add("netherite_leggings_smithing");
		removelist.add("netherite_boots_smithing");

		for (String value : removelist) Bukkit.removeRecipe(NamespacedKey.minecraft(value));

		ItemStack gold_nuggets = new ItemStack(Material.GOLD_NUGGET,3);
		ItemStack iron_nuggets = new ItemStack(Material.IRON_NUGGET,3);
		ItemStack copper_ingot = new ItemStack(Material.COPPER_INGOT);


		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("gold_ingot_from_smelting_raw_gold"),
						gold_nuggets, Material.RAW_GOLD, 0.2F, 1200), "gold_nuggets_smelting"));
		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("gold_ingot_from_smelting_deepslate_gold_ore"),
						gold_nuggets, Material.DEEPSLATE_GOLD_ORE, 0.2F, 1200), "gold_nuggets_smelting"));
		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("gold_ingot_from_smelting_gold_ore"),
						gold_nuggets, Material.GOLD_ORE, 0.2F, 1200), "gold_nuggets_smelting"));
		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("gold_ingot_from_smelting_nether_gold_ore"),
						new ItemStack(Material.GOLD_NUGGET), Material.NETHER_GOLD_ORE, 0.1F, 1200), "gold_nuggets_smelting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("gold_ingot_from_blasting_raw_gold"),
						gold_nuggets, Material.RAW_GOLD, 0.3F, 600), "gold_nuggets_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("gold_ingot_from_blasting_deepslate_gold_ore"),
						gold_nuggets, Material.DEEPSLATE_GOLD_ORE, 0.3F, 600), "gold_nuggets_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("gold_ingot_from_blasting_gold_ore"),
						gold_nuggets, Material.GOLD_ORE, 0.3F, 600), "gold_nuggets_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("gold_ingot_from_blasting_nether_gold_ore"),
						new ItemStack(Material.GOLD_NUGGET), Material.NETHER_GOLD_ORE, 0.1F, 500), "gold_nuggets_blasting"));


		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("copper_ingot_from_smelting_raw_copper"),
						copper_ingot, Material.RAW_COPPER, 0.2F, 400), "copper_ingot"));
		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("copper_ingot_from_smelting_copper_ore"),
						copper_ingot, Material.COPPER_ORE, 0.2F, 400), "copper_ingot"));
		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("copper_ingot_from_smelting_deepslate_copper_ore"),
						copper_ingot, Material.DEEPSLATE_COPPER_ORE, 0.2F, 400), "copper_ingot"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("copper_ingot_from_blasting_raw_copper"),
						copper_ingot, Material.RAW_COPPER, 0.3F, 200), "copper_ingot"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("copper_ingot_from_blasting_copper_ore"),
						copper_ingot, Material.COPPER_ORE, 0.3F, 200), "copper_ingot"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("copper_ingot_from_blasting_deepslate_copper_ore"),
						copper_ingot, Material.DEEPSLATE_COPPER_ORE, 0.3F, 200), "copper_ingot"));


		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("iron_ingot_from_smelting_raw_iron"),
						iron_nuggets, Material.RAW_IRON, 0.2F, 600), "iron_nuggets_smelting"));
		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("iron_ingot_from_smelting_iron_ore"),
						iron_nuggets, Material.IRON_ORE, 0.2F, 600), "iron_nuggets_smelting"));
		Bukkit.addRecipe(setGroup(
				new FurnaceRecipe(NamespacedKey.minecraft("iron_ingot_from_smelting_deepslate_iron_ore"),
						iron_nuggets, Material.DEEPSLATE_IRON_ORE, 0.2F, 600), "iron_nuggets_smelting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("iron_ingot_from_blasting_raw_iron"),
						iron_nuggets, Material.RAW_IRON, 0.3F, 300), "iron_nuggets_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("iron_ingot_from_blasting_iron_ore"),
						iron_nuggets, Material.IRON_ORE, 0.3F, 300), "iron_nuggets_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("iron_ingot_from_blasting_deepslate_iron_ore"),
						iron_nuggets, Material.DEEPSLATE_IRON_ORE, 0.3F, 300), "iron_nuggets_blasting"));


		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("lapis_lazuli_from_blasting_lapis_ore"),
						new ItemStack(Material.LAPIS_LAZULI), Material.LAPIS_ORE, 0.2F, 400), "lapis_lazuli_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("lapis_lazuli_from_blasting_deepslate_lapis_ore"),
						new ItemStack(Material.LAPIS_LAZULI), Material.DEEPSLATE_LAPIS_ORE, 0.2F, 400), "lapis_lazuli_blasting"));


		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("redstone_from_blasting_redstone_ore"),
						new ItemStack(Material.REDSTONE), Material.REDSTONE_ORE, 0.2F, 400), "redstone_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("redstone_from_blasting_deepslate_redstone_ore"),
						new ItemStack(Material.REDSTONE), Material.DEEPSLATE_REDSTONE_ORE, 0.2F, 400), "redstone_blasting"));


		Bukkit.addRecipe(new FurnaceRecipe(NamespacedKey.minecraft("green_dye"), new ItemStack(Material.GREEN_DYE), Material.CACTUS, 0, 200));


		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("emerald_from_blasting_emerald_ore"),
						CustomItem.STARDUST.getItemStack(), Material.EMERALD_ORE, 5, 800), "stardust_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("emerald_from_blasting_deepslate_emerald_ore"),
						CustomItem.STARDUST.getItemStack(), Material.DEEPSLATE_EMERALD_ORE, 5, 800), "stardust_blasting"));


		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("diamond_from_blasting_diamond_ore"),
						CustomItem.DIAMOND_SHARD.getItemStack(3), Material.DIAMOND_ORE, 1.0F, 1600), "diamond_shards_blasting"));
		Bukkit.addRecipe(setGroup(
				new BlastingRecipe(NamespacedKey.minecraft("diamond_from_blasting_deepslate_diamond_ore"),
						CustomItem.DIAMOND_SHARD.getItemStack(3), Material.DEEPSLATE_DIAMOND_ORE, 1.0F, 1600), "diamond_shards_blasting"));


		Bukkit.addRecipe(new BlastingRecipe(NamespacedKey.minecraft("netherite_scrap_from_blasting"), new ItemStack(Material.NETHERITE_SCRAP), Material.ANCIENT_DEBRIS, 3, 2400));
		Bukkit.addRecipe(new BlastingRecipe(NamespacedKey.minecraft("void_substance_from_blasting"), CustomItem.VOID_SUBSTANCE.getItemStack(), Material.EMERALD_BLOCK, 3, 2400));




		ShapelessRecipe recipeShapeless;
		ShapedRecipe recipeShaped;
		StarCraftingRecipe recipeStarCrafting;

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("oak_planks"), new ItemStack(Material.OAK_PLANKS, 2))
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.OAK_LOGS));
		recipeShapeless.setGroup("planks");
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("spruce_planks"), new ItemStack(Material.SPRUCE_PLANKS, 2))
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.SPRUCE_LOGS));
		recipeShapeless.setGroup("planks");
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("birch_planks"), new ItemStack(Material.BIRCH_PLANKS, 2))
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.BIRCH_LOGS));
		recipeShapeless.setGroup("planks");
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("jungle_planks"), new ItemStack(Material.JUNGLE_PLANKS, 2))
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.JUNGLE_LOGS));
		recipeShapeless.setGroup("planks");
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("acacia_planks"), new ItemStack(Material.ACACIA_PLANKS, 2))
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.ACACIA_LOGS));
		recipeShapeless.setGroup("planks");
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("dark_oak_planks"), new ItemStack(Material.DARK_OAK_PLANKS, 2))
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.DARK_OAK_LOGS));
		recipeShapeless.setGroup("planks");
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("crimson_planks"), new ItemStack(Material.CRIMSON_PLANKS, 2))
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.CRIMSON_STEMS));
		recipeShapeless.setGroup("planks");
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("warped_planks"), new ItemStack(Material.WARPED_PLANKS, 2))
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.WARPED_STEMS));
		recipeShapeless.setGroup("planks");
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("flint_and_coal"), CustomItem.FLINT_AND_COAL.getItemStack())
				.addIngredient(Material.FLINT)
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.ITEMS_COALS))
				.addIngredient(new RecipeChoice.MaterialChoice(Material.STRING, Material.PAPER));
		Bukkit.addRecipe(recipeShapeless);

		recipeShapeless = new ShapelessRecipe(NamespacedKey.minecraft("flammable_arrow"), CustomItem.FLAMMABLE_ARROW.getItemStack())
				.addIngredient(Material.PAPER)
				.addIngredient(new RecipeChoice.MaterialChoice(Tag.ITEMS_COALS))
				.addIngredient(Material.ARROW)
				.addIngredient(Material.STRING);
		Bukkit.addRecipe(recipeShapeless);






		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("oak_boat"), new ItemStack(Material.OAK_BOAT))
				.shape("S S", "# #", "###")
				.setIngredient('S', Material.WOODEN_SHOVEL)
				.setIngredient('#', Material.OAK_PLANKS);
		recipeShaped.setGroup("boat");
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("spruce_boat"), new ItemStack(Material.SPRUCE_BOAT))
				.shape("S S", "# #", "###")
				.setIngredient('S', Material.WOODEN_SHOVEL)
				.setIngredient('#', Material.SPRUCE_PLANKS);
		recipeShaped.setGroup("boat");
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("birch_boat"), new ItemStack(Material.BIRCH_BOAT))
				.shape("S S", "# #", "###")
				.setIngredient('S', Material.WOODEN_SHOVEL)
				.setIngredient('#', Material.BIRCH_PLANKS);
		recipeShaped.setGroup("boat");
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("jungle_boat"), new ItemStack(Material.JUNGLE_BOAT))
				.shape("S S", "# #", "###")
				.setIngredient('S', Material.WOODEN_SHOVEL)
				.setIngredient('#', Material.JUNGLE_PLANKS);
		recipeShaped.setGroup("boat");
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("acacia_boat"), new ItemStack(Material.ACACIA_BOAT))
				.shape("S S", "# #", "###")
				.setIngredient('S', Material.WOODEN_SHOVEL)
				.setIngredient('#', Material.ACACIA_PLANKS);
		recipeShaped.setGroup("boat");
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("dark_oak_boat"), new ItemStack(Material.DARK_OAK_BOAT))
				.shape("S S", "# #", "###")
				.setIngredient('S', Material.WOODEN_SHOVEL)
				.setIngredient('#', Material.DARK_OAK_PLANKS);
		recipeShaped.setGroup("boat");
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("cobweb"), new ItemStack(Material.COBWEB))
				.shape("AAA", "ABA", "AAA")
				.setIngredient('A', Material.STRING)
				.setIngredient('B', Material.BONE_MEAL);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("furnace"), new ItemStack(Material.FURNACE))
				.shape("XAX", "A A", "XAX")
				.setIngredient('X', Material.ANDESITE)
				.setIngredient('A', new RecipeChoice.MaterialChoice(Material.COBBLESTONE, Material.BLACKSTONE));
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("smoker"), new ItemStack(Material.SMOKER))
				.shape("ILI", "LFL", "ILI")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('F', Material.FURNACE)
				.setIngredient('L', new RecipeChoice.MaterialChoice(Tag.LOGS));
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("blast_furnace"), new ItemStack(Material.BLAST_FURNACE))
				.shape("IGI", "IFI", "BBB")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('G', Material.GOLD_INGOT)
				.setIngredient('F', Material.FURNACE)
				.setIngredient('B', Material.BRICKS);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("chest"), new ItemStack(Material.CHEST))
				.shape("WLW", "LIL", "WLW")
				.setIngredient('W', new RecipeChoice.MaterialChoice(Material.ACACIA_WOOD, Material.BIRCH_WOOD, Material.DARK_OAK_WOOD, Material.JUNGLE_WOOD, Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.WARPED_HYPHAE, Material.CRIMSON_HYPHAE))
				.setIngredient('L', new RecipeChoice.MaterialChoice(Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG, Material.WARPED_STEM, Material.CRIMSON_STEM))
				.setIngredient('I', Material.IRON_INGOT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("barrel"), new ItemStack(Material.BARREL))
				.shape("WSW", "PNP", "WSW")
				.setIngredient('W', new RecipeChoice.MaterialChoice(Material.ACACIA_WOOD, Material.BIRCH_WOOD, Material.DARK_OAK_WOOD, Material.JUNGLE_WOOD, Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.WARPED_HYPHAE, Material.CRIMSON_HYPHAE))
				.setIngredient('S', new RecipeChoice.MaterialChoice(Tag.WOODEN_SLABS))
				.setIngredient('P', new RecipeChoice.MaterialChoice(Tag.PLANKS))
				.setIngredient('N', Material.IRON_NUGGET);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("chainmail_helmet"), new ItemStack(Material.CHAINMAIL_HELMET))
				.shape(" I ", "IXI")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('X', Material.LEATHER_HELMET);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("chainmail_chestplate"), new ItemStack(Material.CHAINMAIL_CHESTPLATE))
				.shape(" I ", "IXI", " I ")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('X', Material.LEATHER_CHESTPLATE);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("chainmail_leggings"), new ItemStack(Material.CHAINMAIL_LEGGINGS))
				.shape(" I ", "IXI", " I ")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('X', Material.LEATHER_LEGGINGS);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("chainmail_boots"), new ItemStack(Material.CHAINMAIL_BOOTS))
				.shape("IXI")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('X', Material.LEATHER_BOOTS);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("iron_sword"), new ItemStack(Material.IRON_SWORD))
				.shape("I", "I", "F")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("iron_pickaxe"), new ItemStack(Material.IRON_PICKAXE))
				.shape("III", " F ", " F ")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("iron_axe"), new ItemStack(Material.IRON_AXE))
				.shape("II", "IF", " F")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("iron_shovel"), new ItemStack(Material.IRON_SHOVEL))
				.shape("I", "F", "F")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("iron_hoe"), new ItemStack(Material.IRON_HOE))
				.shape("II", " F", " F")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("diamond_sword"), new ItemStack(Material.DIAMOND_SWORD))
				.shape("D", "D", "F")
				.setIngredient('D', Material.DIAMOND)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("diamond_pickaxe"), new ItemStack(Material.DIAMOND_PICKAXE))
				.shape("DDD", " F ", " F ")
				.setIngredient('D', Material.DIAMOND)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("diamond_axe"), new ItemStack(Material.DIAMOND_AXE))
				.shape("DD", "DF", " F")
				.setIngredient('D', Material.DIAMOND)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("diamond_shovel"), new ItemStack(Material.DIAMOND_SHOVEL))
				.shape("D", "F", "F")
				.setIngredient('D', Material.DIAMOND)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("diamond_hoe"), new ItemStack(Material.DIAMOND_HOE))
				.shape("DD", " F", " F")
				.setIngredient('D', Material.DIAMOND)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("iron_band"), CustomItem.IRON_BAND.getItemStack())
				.shape(" I ", "IBI", " I ")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('B', Material.LAVA_BUCKET);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("hatchet"), CustomItem.HATCHET.getItemStack())
				.shape("FF", " S")
				.setIngredient('F', Material.FLINT)
				.setIngredient('S', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("stone_sword"), new ItemStack(Material.STONE_SWORD))
				.shape("S", "S", "I")
				.setIngredient('S', new RecipeChoice.MaterialChoice(Material.STONE, Material.BLACKSTONE))
				.setIngredient('I', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("stone_pickaxe"), new ItemStack(Material.STONE_PICKAXE))
				.shape("SSS", " I ", " I ")
				.setIngredient('S', new RecipeChoice.MaterialChoice(Material.STONE, Material.BLACKSTONE))
				.setIngredient('I', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("stone_axe"), new ItemStack(Material.STONE_AXE))
				.shape("SS", "SI", " I")
				.setIngredient('S', new RecipeChoice.MaterialChoice(Material.STONE, Material.BLACKSTONE))
				.setIngredient('I', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("stone_shovel"), new ItemStack(Material.STONE_SHOVEL))
				.shape("S", "I", "I")
				.setIngredient('S', new RecipeChoice.MaterialChoice(Material.STONE, Material.BLACKSTONE))
				.setIngredient('I', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("stone_hoe"), new ItemStack(Material.STONE_HOE))
				.shape("SS", " I", " I")
				.setIngredient('S', new RecipeChoice.MaterialChoice(Material.STONE, Material.BLACKSTONE))
				.setIngredient('I', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_sword"), new ItemStack(Material.NETHERITE_SWORD))
				.shape("N", "N", "F")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_pickaxe"), new ItemStack(Material.NETHERITE_PICKAXE))
				.shape("NNN", " F ", " F ")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_axe"), new ItemStack(Material.NETHERITE_AXE))
				.shape("NN", "NF", " F")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_shovel"), new ItemStack(Material.NETHERITE_SHOVEL))
				.shape("N", "F", "F")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_hoe"), new ItemStack(Material.NETHERITE_HOE))
				.shape("NN", " F", " F")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('F', Material.FLINT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_helmet"), new ItemStack(Material.NETHERITE_HELMET))
				.shape("NNN", "NXN")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('X', Material.CHAINMAIL_HELMET);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_chestplate"), new ItemStack(Material.NETHERITE_CHESTPLATE))
				.shape("NXN", "NNN", "NNN")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('X', Material.CHAINMAIL_CHESTPLATE);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_leggings"), new ItemStack(Material.NETHERITE_LEGGINGS))
				.shape("NNN", "NXN", "N N")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('X', Material.CHAINMAIL_LEGGINGS);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("netherite_boots"), new ItemStack(Material.NETHERITE_BOOTS))
				.shape("NXN", "N N")
				.setIngredient('N', Material.NETHERITE_INGOT)
				.setIngredient('X', Material.CHAINMAIL_BOOTS);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("homing_arrow"), CustomItem.HOMING_ARROW.getItemStack())
				.shape(" R ", " A ", "R R")
				.setIngredient('A', Material.ARROW)
				.setIngredient('R', Material.REDSTONE);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("prismarine_arrow"), CustomItem.PRISMARINE_ARROW.getItemStack())
				.shape("A A", " P ", " A ")
				.setIngredient('A', Material.ARROW)
				.setIngredient('P', Material.PRISMARINE_SHARD);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("lapis_arrow"),  CustomItem.LAPIS_ARROW.getItemStack(4))
				.shape(" A ", "ALA", " A ")
				.setIngredient('A', Material.ARROW)
				.setIngredient('L', Material.LAPIS_LAZULI);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("star_crafting_table"), new ItemStack(Material.RESPAWN_ANCHOR))
				.shape("IGI", "GSG", "IGI")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('G', Material.GOLD_INGOT)
				.setIngredient('S', Material.EMERALD_ORE);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("torch"), new ItemStack(Material.TORCH, 2))
				.shape("C", "S")
				.setIngredient('C', new RecipeChoice.MaterialChoice(Tag.ITEMS_COALS))
				.setIngredient('S', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("shield"), new ItemStack(Material.SHIELD))
				.shape("PIP", "IPI", "PPP")
				.setIngredient('P', new RecipeChoice.MaterialChoice(Tag.PLANKS))
				.setIngredient('I', Material.IRON_INGOT);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("empty_scroll"), CustomItem.EMPTY_SCROLL.getItemStack())
				.shape(" P ", " P ", "NPS")
				.setIngredient('P', Material.PAPER)
				.setIngredient('N', Material.IRON_NUGGET)
				.setIngredient('S', Material.STRING);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("copper_sword"), CustomItem.COPPER_SWORD.getItemStack())
				.shape("C", "C", "S")
				.setIngredient('C', Material.COPPER_INGOT)
				.setIngredient('S', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("copper_pickaxe"), CustomItem.COPPER_PICKAXE.getItemStack())
				.shape("CCC", " S ", " S ")
				.setIngredient('C', Material.COPPER_INGOT)
				.setIngredient('S', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("copper_axe"), CustomItem.COPPER_AXE.getItemStack())
				.shape("CC", "CS", " S")
				.setIngredient('C', Material.COPPER_INGOT)
				.setIngredient('S', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("copper_shovel"), CustomItem.COPPER_SHOVEL.getItemStack())
				.shape("C", "S", "S")
				.setIngredient('C', Material.COPPER_INGOT)
				.setIngredient('S', Material.STICK);
		Bukkit.addRecipe(recipeShaped);

		recipeShaped = new ShapedRecipe(NamespacedKey.minecraft("copper_hoe"), CustomItem.COPPER_HOE.getItemStack())
				.shape("CC", " S", " S")
				.setIngredient('C', Material.COPPER_INGOT)
				.setIngredient('S', Material.STICK);
		Bukkit.addRecipe(recipeShaped);










		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("diamond_from_starcrafting"), new ItemStack(Material.DIAMOND))
				.shape("ddd", "dDd", "ddd")
				.setIngredient('d', CustomItem.DIAMOND_SHARD)
				.setIngredient('D', CustomItem.DIAMOND_SHARD, 2);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("powered_diamond"), CustomItem.POWERED_DIAMOND.getItemStack())
				.shape("DDD", "DVD", "DDD")
				.setIngredient('D', Material.DIAMOND_BLOCK)
				.setIngredient('V', Material.VINE);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("obsidian"), new ItemStack(Material.OBSIDIAN))
				.shape("OOO", "OOO", "OOO")
				.setIngredient('O', CustomItem.OBSIDIAN_SHARD);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("golden_band"), CustomItem.GOLDEN_BAND.getItemStack())
				.shape(" G ", "GBG", " G ")
				.setIngredient('B', CustomItem.IRON_BAND)
				.setIngredient('G', Material.GOLD_INGOT);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("emerald_band"), CustomItem.EMERALD_BAND.getItemStack())
				.shape(" E ", "EBE", " E ")
				.setIngredient('B', CustomItem.IRON_BAND)
				.setIngredient('E', Material.EMERALD);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("diamond_band"), CustomItem.DIAMOND_BAND.getItemStack())
				.shape(" D ", "DBD", " D ")
				.setIngredient('B', CustomItem.IRON_BAND)
				.setIngredient('D', Material.DIAMOND);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("netherite_band"), CustomItem.NETHERITE_BAND.getItemStack())
				.shape(" N ", "NBN", " N ")
				.setIngredient('B', CustomItem.IRON_BAND)
				.setIngredient('N', Material.NETHERITE_INGOT);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("ring_of_sacrifice"), CustomItem.RING_OF_SACRIFICE.getItemStack())
				.shape("D  ", " B ", "   ")
				.setIngredient('B', CustomItem.GOLDEN_BAND)
				.setIngredient('D', CustomItem.DIAMOND_SHARD, 2);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("starmetal_ingot"), CustomItem.STARMETAL_INGOT.getItemStack())
				.shape(" S ", " G ", "SIS")
				.setIngredient('I', Material.IRON_INGOT)
				.setIngredient('G', Material.GOLD_INGOT)
				.setIngredient('S', CustomItem.STARDUST);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("netherite_ingot_from_starcrafting"), new ItemStack(Material.NETHERITE_INGOT))
				.shape("NMN", "MRM", "GMG")
				.setIngredient('N', new ItemStack(Material.NETHERITE_SCRAP, 2))
				.setIngredient('G', new ItemStack(Material.GOLD_INGOT, 2))
				.setIngredient('M', CustomItem.METEORITE_FRAGMENT)
				.setIngredient('R', CustomItem.RADIOACTIVE_COAL);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("forbidden_book"), CustomItem.FORBIDDEN_BOOK.getItemStack())
				.shape("ORO", "NTN", "ONO")
				.setIngredient('O', Material.OBSIDIAN)
				.setIngredient('R', Material.REDSTONE)
				.setIngredient('T', CustomItem.TOME)
				.setIngredient('N', Material.GOLD_NUGGET);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("book_of_regression"), CustomItem.BOOK_OF_REGRESSION.getItemStack())
				.shape("ORO", "NTN", "ONO")
				.setIngredient('O', new ItemStack(Material.GOLD_NUGGET, 3))
				.setIngredient('R', Material.LAPIS_LAZULI)
				.setIngredient('T', CustomItem.TOME)
				.setIngredient('N', Material.IRON_NUGGET);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("blaze_rod"), new ItemStack(Material.BLAZE_ROD))
				.shape("  F", " F ", "F  ")
				.setIngredient('F', CustomItem.BLAZE_FRAGMENT, 3);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("bloodstone"), CustomItem.BLOODSTONE.getItemStack())
				.shape("SSS", "SBS", "SSS")
				.setIngredient('S', Material.STONE)
				.setIngredient('B', CustomItem.BLOOD_BOTTLE, 2);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("soulstone"), CustomItem.SOULSTONE.getItemStack())
				.shape("SSS", "SES", "SSS")
				.setIngredient('S', Material.STONE)
				.setIngredient('E', CustomItem.SPIRIT_ESSENCE, 6);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("archangels_forgiveness"), CustomItem.ARCHANGELS_FORGIVENESS.getItemStack())
				.shape(" V ", "DBD", " S ")
				.setIngredient('B', CustomItem.IRON_BAND)
				.setIngredient('V', CustomItem.VOODOO_DOLL)
				.setIngredient('D', Material.DIAMOND)
				.setIngredient('S', Material.WITHER_SKELETON_SKULL);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("tome"), CustomItem.TOME.getItemStack())
				.shape("LLL", "SSS", "LLL")
				.setIngredient('S', CustomItem.EMPTY_SCROLL)
				.setIngredient('L', Material.LEATHER);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("enchanting_table"), new ItemStack(Material.ENCHANTING_TABLE))
				.shape(" T ", "DSD", "OOO")
				.setIngredient('T', CustomItem.TOME)
				.setIngredient('D', Material.DIAMOND)
				.setIngredient('S', CustomItem.STARDUST)
				.setIngredient('O', Material.OBSIDIAN);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("book_of_eternity"), CustomItem.BOOK_OF_ETERNITY.getItemStack())
				.shape(" U ", " B ", "LLL")
				.setIngredient('U', new ItemStack(Material.LAPIS_LAZULI, 3))
				.setIngredient('L', CustomItem.LEAF)
				.setIngredient('B', Material.BOOK);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("book_of_consciousness"), CustomItem.BOOK_OF_CONSCIOUSNESS.getItemStack())
				.shape("DDD", "WTW", "VVV")
				.setIngredient('T', CustomItem.TOME)
				.setIngredient('W', new ItemStack(Material.WARPED_FUNGUS, 2))
				.setIngredient('D', CustomItem.DAYDREAM_FEATHER)
				.setIngredient('V', CustomItem.VOID_TENDRIL);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("nature_crystal"), CustomItem.NATURE_CRYSTAL.getItemStack())
				.shape(" EE", "EEE", "EE ")
				.setIngredient('E', Material.EMERALD);
		StarCraftingManager.addRecipe(recipeStarCrafting);

		recipeStarCrafting = new StarCraftingRecipe(NamespacedKey.minecraft("bronze_ingot"), CustomItem.BRONZE_INGOT.getItemStack())
				.shape(" C ", " I ", "   ")
				.setIngredient('C', Material.COPPER_INGOT)
				.setIngredient('I', Material.IRON_INGOT);
		StarCraftingManager.addRecipe(recipeStarCrafting);




	}

	private Recipe setGroup(@Nonnull Recipe recipe, @Nonnull String group) {
		if (recipe instanceof FurnaceRecipe) {
			FurnaceRecipe rrecipe = (FurnaceRecipe) recipe;
			rrecipe.setGroup(group);
			return rrecipe;
		}
		else if (recipe instanceof BlastingRecipe) {
			BlastingRecipe rrecipe = (BlastingRecipe) recipe;
			rrecipe.setGroup(group);
			return rrecipe;
		}
		return recipe;
	}

}
