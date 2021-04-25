package com.mysteria.mysteryuniverse.mainlisteners.entity;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CreeperTargetPlayerListener implements Listener {

	public CreeperTargetPlayerListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCreeperTargetPlayer(EntityTargetEvent e) {

		if (e.getEntity() instanceof Creeper && e.getTarget() instanceof Player) {

			Creeper creeper = (Creeper) e.getEntity();
			Player p = (Player) e.getTarget();

			new BukkitRunnable() {

				int i = 0;

				@Override
				public void run() {

					if (!creeper.isValid() || creeper.getTarget() == null) this.cancel();
					if (i > 30) {
						if (creeper.getTarget() == p) i = 0;
						else this.cancel();
					}


					double distance = creeper.getLocation().distance(p.getLocation());
					if (distance < 3) {

						Location loc = creeper.getLocation();
						Location to = p.getLocation();
						double x = loc.getX() - to.getX();
						double y = loc.getY() - to.getY() - 2;
						double z = loc.getZ() - to.getZ();
						Vector velocity = new Vector(x, y, z).normalize().multiply(-0.6);
						creeper.setVelocity(velocity);

						creeper.ignite();
						this.cancel();

					} else i++;

				}
			}.runTaskTimer(MysteryUniversePlugin.getInstance(), 0, 10);

		}
	}
}
