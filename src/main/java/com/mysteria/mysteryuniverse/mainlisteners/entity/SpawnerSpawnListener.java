package com.mysteria.mysteryuniverse.mainlisteners.entity;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SpawnerSpawnListener implements Listener {

    public SpawnerSpawnListener() {
        Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {

        CreatureSpawner s = (CreatureSpawner) e.getSpawner().getLocation().getBlock().getState();

        if (s.getMaxSpawnDelay() == 800) {
            s.setMinSpawnDelay(100);
            s.setMaxSpawnDelay(100);
            s.setSpawnCount(1);
            s.setSpawnRange(8);
            s.setMaxNearbyEntities(30);
            s.setRequiredPlayerRange(8);
            e.setCancelled(true);
        }

        if (s.getMinSpawnDelay() <= 10) {
            s.getLocation().getBlock().setType(Material.AIR);
            s.getWorld().playSound(s.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1,1);
            s.getWorld().playEffect(s.getLocation(), Effect.ENDER_SIGNAL, 0);
            s.getWorld().dropItemNaturally(s.getLocation(), new ItemStack(Material.IRON_NUGGET, 4 + new Random().nextInt(4)));

        } else {
            s.setMinSpawnDelay(s.getMinSpawnDelay() - 5);
            s.setMaxSpawnDelay(s.getMaxSpawnDelay() - 5);
            s.update();
        }

    }

}
