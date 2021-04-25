package com.mysteria.mysteryuniverse.mainlisteners.server;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ExplosionPrimeListener implements Listener {

    public ExplosionPrimeListener() {
        Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent e) {

        Block b = e.getEntity().getLocation().getBlock();

        if (b.getType() == Material.WATER) {
            b.setType(Material.AIR);
            new BukkitRunnable() {
                @Override
                public void run() {
                    b.setType(Material.WATER);
                }
            }.runTaskLater(MysteryUniversePlugin.getInstance(), 1);
        }
    }
}
