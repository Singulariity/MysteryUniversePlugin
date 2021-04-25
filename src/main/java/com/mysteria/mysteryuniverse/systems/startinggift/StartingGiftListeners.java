package com.mysteria.mysteryuniverse.systems.startinggift;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.database.Database;
import com.mysteria.mysteryuniverse.database.enums.Column;
import com.mysteria.mysteryuniverse.systems.startinggift.events.StartingGiftSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;

public class StartingGiftListeners implements Listener {

	public StartingGiftListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	private void onStartingGiftClick(InventoryClickEvent e) {

		StartingGiftManager startingGift = MysteryUniversePlugin.getStartingGiftManager();

		if (!e.getView().title().equals(startingGift.getGUIName())) return;

		e.setCancelled(true);

		// Filled slot click check
		if (e.getCurrentItem() == null || CustomItem.checkCustomItem(e.getCurrentItem(), CustomItem.EMPTY)) return;
		if (e.getRawSlot() >= startingGift.getSize()) return;


		// CODE //
		Player p = (Player) e.getWhoClicked();

		ItemStack gift;
		if (e.getCurrentItem().getType() == Material.BARRIER) gift = null;
		else gift = e.getCurrentItem();

		Database database = MysteryUniversePlugin.getDatabase();
		database.setBoolean(p.getUniqueId(), Column.STARTING_GIFT, true);
		p.closeInventory();

		if (gift != null) p.getInventory().addItem(e.getCurrentItem());

		StartingGiftSelectEvent event = new StartingGiftSelectEvent(p, gift);
		Bukkit.getPluginManager().callEvent(event);



	}

}
