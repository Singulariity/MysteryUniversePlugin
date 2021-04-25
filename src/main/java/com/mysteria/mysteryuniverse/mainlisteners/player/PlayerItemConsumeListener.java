package com.mysteria.mysteryuniverse.mainlisteners.player;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.ImmutableSet;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerItemConsumeListener implements Listener {

	public PlayerItemConsumeListener(){
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	private final ImmutableSet<Material> FOODS = new ImmutableSet.Builder<Material>()
			.add(Material.BEEF)
			.add(Material.PORKCHOP)
			.add(Material.CHICKEN)
			.add(Material.RABBIT)
			.add(Material.MUTTON)
			.build();

	@EventHandler(ignoreCancelled = true)
	public void onEat(PlayerItemConsumeEvent e) {

		Player p = e.getPlayer();
		Material type = e.getItem().getType();

		if (FOODS.contains(type) || MaterialTags.RAW_FISH.isTagged(type)) {

			if (MysteriaUtils.chance(10)) return;

			p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 400, 0));
			p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 300, 0));
			MysteriaUtils.sendMessageDarkRed(p, "You're poisoned for eating raw food!");

		}


	}

}
