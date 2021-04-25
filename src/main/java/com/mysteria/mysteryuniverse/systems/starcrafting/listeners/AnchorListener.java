package com.mysteria.mysteryuniverse.systems.starcrafting.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;

public class AnchorListener implements Listener {

	public AnchorListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onRespawnAnchorClick(PlayerInteractEvent e) {
		if (
				e.getAction() != Action.RIGHT_CLICK_BLOCK
				|| e.getClickedBlock() == null
				|| e.getPlayer().isSneaking()
				|| e.getClickedBlock().getType() != Material.RESPAWN_ANCHOR
		) return;

		e.setCancelled(true);
		MysteryUniversePlugin.getStarCraftingManager().starCraftingGUI(e.getPlayer());

	}


	@EventHandler(ignoreCancelled = true)
	public void onRespawnAnchorPlace(BlockPlaceEvent e) {
		if (e.getBlockPlaced().getType() != Material.RESPAWN_ANCHOR) return;

		RespawnAnchor anchor = (RespawnAnchor) e.getBlockPlaced().getBlockData();
		anchor.setCharges(2);
		e.getBlockPlaced().setBlockData(anchor);
	}

	@EventHandler(ignoreCancelled = true)
	public void onRespawnAnchorBreak(BlockBreakEvent e) {
		if (e.getBlock().getType() != Material.RESPAWN_ANCHOR) return;

		e.setDropItems(false);

		if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
			e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(Material.RESPAWN_ANCHOR));
		}
	}


}
