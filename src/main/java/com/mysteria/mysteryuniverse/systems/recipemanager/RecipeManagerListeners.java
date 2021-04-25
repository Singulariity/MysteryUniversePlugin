package com.mysteria.mysteryuniverse.systems.recipemanager;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RecipeManagerListeners implements Listener {

	public RecipeManagerListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	private void onClick(InventoryClickEvent e) {

		RecipeManager recipeManager = MysteryUniversePlugin.getRecipeManager();
		String playerView = MysteriaUtils.translateToString(e.getView().title());
		String GUIName = MysteriaUtils.translateToString(recipeManager.getGUIName());
		if (playerView.contains(GUIName)) {
			e.setCancelled(true);

			if (recipeManager.getSize() > e.getRawSlot() && e.getRawSlot() != 24) {
				if (e.getWhoClicked() instanceof Player) {
					Player p = (Player) e.getWhoClicked();
					//p.closeInventory();

					ItemStack clicked = e.getCurrentItem();
					if (clicked == null || clicked.getType() == Material.AIR ||
							CustomItem.checkCustomItem(clicked, CustomItem.EMPTY)) {
						return;
					}

					if (recipeManager.RecipeGUI(p, clicked)) {
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
					}
				}
			}
		}

	}

}
