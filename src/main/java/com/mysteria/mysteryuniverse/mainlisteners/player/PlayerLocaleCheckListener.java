package com.mysteria.mysteryuniverse.mainlisteners.player;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerLocaleCheckListener implements Listener {

	public PlayerLocaleCheckListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {

		Player p = e.getPlayer();

		new BukkitRunnable() {
			@Override
			public void run() {
				if (p.isOnline()) {
					if (!p.getLocale().equalsIgnoreCase("en_us") &&
							!p.getLocale().equalsIgnoreCase("tr_tr")) {
						MysteriaUtils.sendMessage(p, Component.text()
								.append(Component.text("Please change your game language to "))
								.append(Component.text("en_US", NamedColor.TURBO))
								.append(Component.text(" or "))
								.append(Component.text("tr_TR", NamedColor.TURBO))
								.append(Component.text(". Otherwise game language will be affected."))
								.colorIfAbsent(NamedColor.CARMINE_PINK)
								.build());
					}
				}
			}
		}.runTaskLater(MysteryUniversePlugin.getInstance(), 20 * 5);

	}

}
