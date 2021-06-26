package com.mysteria.mysteryuniverse.mainlisteners.player;

import com.mysteria.customapi.effects.CustomEffectType;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerSpecificDamageListener implements Listener {

	public PlayerSpecificDamageListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerFallDamage(EntityDamageEvent e) {

		if (e.getEntity() instanceof Player) {

			Player victim = (Player) e.getEntity();

			switch (e.getCause()) {
				case FALL -> {
					if (victim.hasPotionEffect(CustomEffectType.BROKEN_LEG) && MysteriaUtils.chance(60)) {
						victim.addPotionEffect(new PotionEffect(CustomEffectType.BLEED, 8 * 20, 0));
					}

					if (e.getFinalDamage() > 5) {
						if (victim.getHealth() > e.getFinalDamage()) {
							victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300 * 20,2));
							victim.addPotionEffect(new PotionEffect(CustomEffectType.BROKEN_LEG, 300 * 20, 0));
							victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_SKELETON_DEATH, 2,1);
							MysteriaUtils.sendMessageDarkRed(victim, "You fell from too high! Your leg is broken.");
						}
					}
				}
				case DROWNING -> e.setDamage(e.getDamage() * MysteriaUtils.getRandom(2, 3));
				case STARVATION, FREEZE -> e.setDamage(MysteriaUtils.getRandom(1, 4));

			}
		}

	}


}
