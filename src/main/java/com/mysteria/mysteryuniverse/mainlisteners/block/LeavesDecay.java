package com.mysteria.mysteryuniverse.mainlisteners.block;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeavesDecay implements Listener {

	public LeavesDecay() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	private void onLeavesDecay(LeavesDecayEvent e) {

		if (MysteriaUtils.chance(1)) {
			e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), CustomItem.LEAF.getItemStack());
		}

	}

}
