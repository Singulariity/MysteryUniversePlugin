package com.mysteria.mysteryuniverse.mainlisteners.player;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerPoisonListener implements Listener {

	public PlayerPoisonListener(){
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}


	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageByEntityEvent e) {

		if (e.getEntity() instanceof Player && e.getFinalDamage() > 0) {
			Player p = (Player) e.getEntity();
			Entity damager = e.getDamager();

			if (damager instanceof Zombie || damager.getType() == EntityType.SPIDER) {
				if (MysteriaUtils.chance(20)) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80,1));
				}
			}

		}


	}


}
