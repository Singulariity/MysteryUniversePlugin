package com.mysteria.mysteryuniverse.systems.starcrafting.listeners;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.systems.starcrafting.StarCraftingManager;
import com.mysteria.mysteryuniverse.systems.starcrafting.StarCraftingRecipe;
import com.mysteria.utils.MysteriaUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarCraftingListeners implements Listener {

	public StarCraftingListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	private void onStarCraftingClick(InventoryClickEvent e) {

		StarCraftingManager starCrafting = MysteryUniversePlugin.getStarCraftingManager();

		if (!e.getView().title().equals(starCrafting.getGUIName())) {
			return;
		}

		// Filled slot click check
		if (e.getCurrentItem() != null && CustomItem.checkCustomItem(e.getCurrentItem(), CustomItem.EMPTY)) {
			e.setCancelled(true);
			return;
		}

		// Result slot check
		if (e.getRawSlot() != 24) return;



		// CODE //
		e.setCancelled(true);

		Player p = (Player) e.getWhoClicked();

		if (p.getInventory().firstEmpty() == -1) {
			MysteriaUtils.sendMessageDarkRed(p, "Your inventory is full!");
			return;
		}

		Inventory inv = e.getInventory();
		ItemStack[] contents = inv.getContents();
		ItemStack[][] relevant = new ItemStack[3][3];

		Map<Integer, ItemStack> itemsinRecipe = new HashMap<>();
		List<Integer> relevantSlots = new ArrayList<>();

		for (int i = 0; i < contents.length; i++) {
			int r = i / 9;
			int c = i % 9;
			if (r > 0 && c > 0 && r < 4 && c < 4) {
				relevant[r - 1][c - 1] = contents[i];
				relevantSlots.add(i);
			}
		}

		for (StarCraftingRecipe recipe: starCrafting.getRecipes()) {
			if (!recipe.match(relevant)) continue;

			for (int i = 0; i < contents.length; i++) {
				int r = i / 9;
				int c = i % 9;
				if (r > 0 && c > 0 && r < 4 && c < 4) {
					itemsinRecipe.put(i, recipe.getItemStack(r - 1, c - 1));
				}
			}

			for (Integer id : relevantSlots) {
				ItemStack inGUI = inv.getItem(id);
				ItemStack inRecipe = itemsinRecipe.get(id);
				if (inGUI != null && inRecipe != null) {
					int newAmount = inGUI.getAmount() - inRecipe.getAmount();
					if (newAmount < 0) newAmount = 0;
					inGUI.setAmount(newAmount);
					inv.setItem(id, inGUI);

				}
			}
			p.getInventory().addItem(recipe.getResult());
			p.playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 2);

			break;
		}



	}



	@EventHandler
	private void onClose(InventoryCloseEvent e) {

		StarCraftingManager starCrafting = MysteryUniversePlugin.getStarCraftingManager();
		Component name = starCrafting.getGUIName();

		if (!e.getView().title().equals(name)) return;

		Inventory inv = e.getView().getTopInventory();

		for (int slot : starCrafting.CRAFTING_SLOTS) {
			ItemStack item = inv.getItem(slot);
			if (item == null) continue;

			if (e.getPlayer().getInventory().firstEmpty() == -1) {
				e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item);
			} else {
				e.getPlayer().getInventory().addItem(item);
			}
		}


	}


}
