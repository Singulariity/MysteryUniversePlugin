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

public class PlayerInteractEntityListener implements Listener {

	public PlayerInteractEntityListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {

		Entity clickedEntity = e.getRightClicked();

		if (clickedEntity instanceof Villager) {
			if (!e.getPlayer().hasPermission("MysteryUniversePlugin.bypass")) e.setCancelled(true);

		}
		else if (clickedEntity instanceof Cow) {

			Player p = e.getPlayer();
			Material pTool = p.getInventory().getItemInMainHand().getType();

			Database database = MysteryUniversePlugin.getDatabase();

			if (pTool == Material.BUCKET) {
				Long cooldown = database.getLong(p.getUniqueId(), Column.COOLDOWN_MILK);
				cooldown = cooldown != null ? cooldown : 0;

				if (MysteriaUtils.checkCooldown(cooldown)) {
					database.setLong(p.getUniqueId(), Column.COOLDOWN_MILK, MysteriaUtils.createCooldown(3600 * 4));
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
			else if (pTool == Material.BOWL && clickedEntity.getType() == EntityType.MUSHROOM_COW) {
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
