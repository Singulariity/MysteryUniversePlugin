package com.mysteria.mysteryuniverse.systems.starcrafting;

import com.google.common.collect.ImmutableSet;
import com.mysteria.customapi.items.CustomItem;
import com.mysteria.customapi.sounds.CustomSound;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.systems.starcrafting.listeners.AnchorListener;
import com.mysteria.mysteryuniverse.systems.starcrafting.listeners.StarCraftingListeners;
import com.mysteria.utils.ItemBuilder;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StarCraftingManager {

	private static final List<StarCraftingRecipe> recipes = new ArrayList<>();
	public final Component name = Component.text()
			.append(Component.text("Star ", NamedColor.STEEL_PINK))
			.append(Component.text("Crafting", NamedColor.HELIOTROPE)
					.decorate(TextDecoration.BOLD))
			.build();
	public final ImmutableSet<Integer> CRAFTING_SLOTS = new ImmutableSet.Builder<Integer>()
			// Line 1
			.add(10)
			.add(11)
			.add(12)
			// Line 2
			.add(19)
			.add(20)
			.add(21)
			// Line 3
			.add(28)
			.add(29)
			.add(30)
			.build();

	public StarCraftingManager() {
		if (MysteryUniversePlugin.getStarCraftingManager() != null) {
			throw new IllegalStateException();
		}
		new StarCraftingListeners();
		new AnchorListener();
	}



	public void starCraftingGUI(@Nonnull Player p) {

		Component name = getGUIName();
		Inventory inv = Bukkit.createInventory(null, 45, name);


		ItemStack empty = ItemBuilder.builder(CustomItem.EMPTY.getItemStack())
				.adaptGUI()
				.build();

		for (int i = 0; i < 45; i++) {
			if (!CRAFTING_SLOTS.contains(i)) inv.setItem(i, empty);
		}

		CustomSound.play(p, CustomSound.GALAXY_STARCRAFTING_OPEN, 1, 1);

		p.openInventory(inv);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (!p.getOpenInventory().title().equals(name)) {
					this.cancel();
					return;
				}

				p.updateInventory();

				Inventory inv = p.getOpenInventory().getTopInventory();
				ItemStack[] contents = inv.getContents();

				//choose relevant 4x4 fraction from inventory ( this takes top left 4x4 );
				// r and c for row and column
				ItemStack[][] relevant = new ItemStack[3][3];

				for (int i = 0; i < contents.length; i++) {
					int r = i / 9;
					int c = i % 9;
					if (r > 0 && c > 0 && r < 4 && c < 4) {
						relevant[r - 1][c - 1] = contents[i];
					}
				}

				for (StarCraftingRecipe recipe: recipes) {
					if (recipe.match(relevant)) {
						ItemStack result = recipe.getResult();
						//do your stuff with result here
						inv.setItem(24, result);
						break;
					} else {
						inv.setItem(24, empty);
					}
				}
			}
		}.runTaskTimer(MysteryUniversePlugin.getInstance(), 0, 2);

	}

	public Component getGUIName() {
		return this.name;
	}

	@Nonnull
	public List<StarCraftingRecipe> getRecipes() {
		return recipes;
	}

	public static void addRecipe(@Nonnull StarCraftingRecipe recipe) {
		recipes.add(recipe);
	}

}
