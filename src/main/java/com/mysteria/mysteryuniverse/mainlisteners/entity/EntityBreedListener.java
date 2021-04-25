package com.mysteria.mysteryuniverse.mainlisteners.entity;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

public class EntityBreedListener implements Listener {

    public EntityBreedListener(){
        Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent e){

        e.setCancelled(true);
        ((Breedable) e.getMother()).setBreed(false);
        ((Breedable) e.getFather()).setBreed(false);

        if (e.getBreeder() instanceof Player) {
            MysteriaUtils.sendMessageDarkRed(e.getBreeder(), "Breeding is disabled.");
        }

    }

}
