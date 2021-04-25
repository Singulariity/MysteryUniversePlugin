package com.mysteria.mysteryuniverse.mainlisteners.entity;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SheepListeners implements Listener {

	private final List<UUID> ALLOWED = new ArrayList<>();

	public SheepListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onFeedSheep(PlayerInteractAtEntityEvent e) {
		if (!(e.getRightClicked() instanceof Sheep)) return;

		Sheep sheep = (Sheep) e.getRightClicked();
		if (!sheep.isSheared() || ALLOWED.contains(sheep.getUniqueId())) return;

		ItemStack hold = e.getPlayer().getInventory().getItemInMainHand();


		if (hold.getType() == Material.WHEAT) {
			e.setCancelled(true);
			hold.setAmount(hold.getAmount() - 1);
			ALLOWED.add(sheep.getUniqueId());
			sheep.getWorld().spawnParticle(Particle.HEART, sheep.getLocation().clone().add(0, 1, 0), 1);
			sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.3F, 2F);
		}


	}

	@EventHandler(ignoreCancelled = true)
	public void onWoolRegrow(SheepRegrowWoolEvent e) {

		UUID uuid = e.getEntity().getUniqueId();

		if (ALLOWED.contains(uuid)) {
			ALLOWED.remove(uuid);
		} else {
			e.setCancelled(true);
		}

	}

}
