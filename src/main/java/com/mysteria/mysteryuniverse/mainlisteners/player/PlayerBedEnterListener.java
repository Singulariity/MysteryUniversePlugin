package com.mysteria.mysteryuniverse.mainlisteners.player;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import io.papermc.paper.event.player.PlayerBedFailEnterEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerBedEnterListener implements Listener {

	public PlayerBedEnterListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBedFail(PlayerBedFailEnterEvent e) {
		if (e.getFailReason() == PlayerBedFailEnterEvent.FailReason.NOT_POSSIBLE_HERE) {
			e.setWillExplode(false);
			MysteriaUtils.sendMessageDarkRed(e.getPlayer(), "You can't rest at here.");
		}
	}

}
