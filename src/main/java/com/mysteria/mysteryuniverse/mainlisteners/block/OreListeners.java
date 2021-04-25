package com.mysteria.mysteryuniverse.mainlisteners.block;

import com.destroystokyo.paper.MaterialTags;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class OreListeners implements Listener {

	public OreListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onOrePlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();

		if (isOre(e.getBlockPlaced().getType()) && !p.hasPermission("survivalplus.bypass")) {
			e.setCancelled(true);
			MysteriaUtils.sendMessageDarkRed(p, "Ores cannot be placed.");
		}

	}

	@EventHandler(ignoreCancelled = true)
	private void onOreDropExp(BlockExpEvent e) {
		if (isOre(e.getBlock().getType())) e.setExpToDrop(0);
	}


	private boolean isOre(Material material) {
		return MaterialTags.ORES.isTagged(material) || material == Material.EMERALD_BLOCK;
	}

}
