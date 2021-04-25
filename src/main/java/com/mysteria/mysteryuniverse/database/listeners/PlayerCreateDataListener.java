package com.mysteria.mysteryuniverse.database.listeners;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerCreateDataListener implements Listener {

	public PlayerCreateDataListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerJoin(PlayerJoinEvent e) {

		UUID uuid = e.getPlayer().getUniqueId();
		Database database = MysteryUniversePlugin.getDatabase();

		if (!database.hasData(uuid)) {
			database.createData(uuid);
		}

	}

}
