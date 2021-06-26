package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.mysteria.customapi.effects.CustomEffectType;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.systems.meteorite.events.MeteoriteLandEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

public class MeteoriteLandListener implements Listener {

	public MeteoriteLandListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	private void onMeteoriteLand(MeteoriteLandEvent e) {
		Collection<Player> players = e.getLocation().getNearbyPlayers(50);
		if (players.size() == 0) {
			e.setCancelled(true);
		} else {
			for (Player p : players) {
				PotionEffect effect = CustomEffectType.CREATIVE_SHOCK.createEffect(20 * 180, 0);
				p.addPotionEffect(effect);
			}
		}
	}

}
