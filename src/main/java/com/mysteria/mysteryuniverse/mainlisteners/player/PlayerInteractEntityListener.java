package com.mysteria.mysteryuniverse.mainlisteners.player;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.database.Database;
import com.mysteria.mysteryuniverse.database.enums.Column;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import javax.annotation.Nonnull;

public class PlayerInteractEntityListener implements Listener {

	public PlayerInteractEntityListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {

		Entity clickedEntity = e.getRightClicked();
		Player p = e.getPlayer();

		switch (clickedEntity.getType()) {
			case VILLAGER -> {
				if (!e.getPlayer().hasPermission("MysteryUniversePlugin.bypass")) {
					e.setCancelled(true);
				}
			}
			case COW, GOAT -> {
				if (p.getInventory().getItemInMainHand().getType() == Material.BUCKET) {
					boolean canMilk = canMilk(p);
					if (!canMilk) {
						e.setCancelled(true);
					}
				}
			}
			case MUSHROOM_COW -> {
				switch (p.getInventory().getItemInMainHand().getType()) {
					case BUCKET -> {
						boolean canMilk = canMilk(p);
						if (!canMilk) {
							e.setCancelled(true);
						}
					}
					case BOWL -> {
						Database database = MysteryUniversePlugin.getDatabase();
						Long cooldown = database.getLong(e.getPlayer().getUniqueId(), Column.COOLDOWN_STEW);
						cooldown = cooldown != null ? cooldown : 0;

						if (MysteriaUtils.checkCooldown(cooldown)) {
							database.setLong(p.getUniqueId(), Column.COOLDOWN_STEW, MysteriaUtils.createCooldown(3600));
						} else {
							e.setCancelled(true);
							MysteriaUtils.sendMessage(p, Component.text()
									.append(Component.text("You can do this again in "))
									.append(Component.text(MysteriaUtils.cooldownString(cooldown), NamedColor.TURBO))
									.append(Component.text("."))
									.colorIfAbsent(NamedColor.CARMINE_PINK)
									.build());
						}
					}
				}
			}
		}
	}

	private boolean canMilk(@Nonnull Player p) {
		Database database = MysteryUniversePlugin.getDatabase();
		Long cooldown = database.getLong(p.getUniqueId(), Column.COOLDOWN_MILK);
		cooldown = cooldown != null ? cooldown : 0;

		if (MysteriaUtils.checkCooldown(cooldown)) {
			database.setLong(p.getUniqueId(), Column.COOLDOWN_MILK, MysteriaUtils.createCooldown(3600 * 4));
			return true;
		} else {
			MysteriaUtils.sendMessage(p, Component.text()
					.append(Component.text("You can do this again in "))
					.append(Component.text(MysteriaUtils.cooldownString(cooldown), NamedColor.TURBO))
					.append(Component.text("."))
					.colorIfAbsent(NamedColor.CARMINE_PINK)
					.build());
		}
		return false;
	}


}
