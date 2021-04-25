package com.mysteria.mysteryuniverse.systems.meteorite;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MeteoriteListeners implements Listener {

	public MeteoriteListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPickupItem(EntityPickupItemEvent e) {

		if (e.getEntity() instanceof Player) {

			ItemMeta meta = e.getItem().getItemStack().getItemMeta();
			NamespacedKey key = MysteryUniversePlugin.getMeteoriteManager().getNonStackableKey();

			if (meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER) != null) {
				meta.getPersistentDataContainer().remove(key);
			}
			e.getItem().getItemStack().setItemMeta(meta);

		}
	}

}
