package com.mysteria.mysteryuniverse.mainlisteners.player;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerFishingListener implements Listener {

    public PlayerFishingListener() {
        Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFishing(PlayerFishEvent e) {
        Player p = e.getPlayer();

        if (p.getWorld().getName().equals("spawn") && !p.hasPermission("MysteryUniversePlugin.bypass")) {
            e.setCancelled(true);
            MysteriaUtils.sendMessageDarkRed(p, "You can't fishing in this world.");
        }

    }

}
