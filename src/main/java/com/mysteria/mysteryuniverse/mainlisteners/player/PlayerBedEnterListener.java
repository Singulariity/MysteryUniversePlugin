package com.mysteria.mysteryuniverse.mainlisteners.player;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class PlayerBedEnterListener implements Listener {

	public PlayerBedEnterListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBedEnter(PlayerBedEnterEvent e) {

		if (e.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE) {
			MysteriaUtils.sendMessageDarkRed(e.getPlayer(), "You can't rest at here.");
			e.setCancelled(true);
		}

	}

}
