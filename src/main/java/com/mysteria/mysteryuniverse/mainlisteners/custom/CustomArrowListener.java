package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.mainlisteners.custom.tasks.HomingArrowTask;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CustomArrowListener implements Listener {

	public CustomArrowListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onCustomArrowShoot(EntityShootBowEvent e) {

		if (!(e.getEntity() instanceof Player) || e.getConsumable() == null) return;

		if (CustomItem.checkCustomItem(e.getConsumable(), CustomItem.FLAMMABLE_ARROW)) {

			Arrow arrow = (Arrow) e.getProjectile();
			arrow.setFireTicks(300);

			arrowParticle(arrow, Particle.SMOKE_LARGE);

		} else if (CustomItem.checkCustomItem(e.getConsumable(), CustomItem.HOMING_ARROW)) {

			if (e.getForce() < 1) return;

			LivingEntity p = e.getEntity();

			//double minAngle = 6.283185307179586D;
			double minAngle = 0.4D;
			Entity minEntity = null;
			for (Entity entity : p.getNearbyEntities(64.0D, 64.0D, 64.0D)) {
				if ((p.hasLineOfSight(entity)) && ((entity instanceof LivingEntity) && !(entity instanceof Enderman)) && (!entity.isDead())) {
					Vector toTarget = entity.getLocation().toVector().clone().subtract(p.getLocation().toVector());
					double angle = e.getProjectile().getVelocity().angle(toTarget);
					if (angle < minAngle) {
						minAngle = angle;
						minEntity = entity;
					}
				}
			}
			if (minEntity != null) {
				new HomingArrowTask((Arrow) e.getProjectile(), (LivingEntity) minEntity);
			}


		} else if (CustomItem.checkCustomItem(e.getConsumable(), CustomItem.PRISMARINE_ARROW)) {
			Arrow arrowFirst = e.getEntity().getWorld().spawnArrow(e.getEntity().getLocation().clone().add(0,1.5,0), rotateVector(e.getProjectile().getVelocity(), 0.1), e.getForce() * 2, 0f);
			Arrow arrowSecond = e.getEntity().getWorld().spawnArrow(e.getEntity().getLocation().clone().add(0,1.5,0), rotateVector(e.getProjectile().getVelocity(), -0.1), e.getForce() * 2,0f);
			arrowFirst.setShooter(e.getEntity());
			arrowSecond.setShooter(e.getEntity());
			arrowFirst.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
			arrowSecond.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);

		} else if (CustomItem.checkCustomItem(e.getConsumable(), CustomItem.LAPIS_ARROW)) {
			Arrow arrow = (Arrow) e.getProjectile();
			arrow.setPierceLevel(5);
			arrowParticle(arrow, Particle.CRIT);
		}


	}

	public Vector rotateVector(Vector vector, double whatAngle) {
		double sin = Math.sin(whatAngle);
		double cos = Math.cos(whatAngle);
		double x = vector.getX() * cos + vector.getZ() * sin;
		double z = vector.getX() * -sin + vector.getZ() * cos;

		return vector.setX(x).setZ(z);
	}

	public void arrowParticle(Arrow arrow, Particle particle) {

		new BukkitRunnable() {
			@Override
			public void run() {

				if (arrow.isOnGround() || arrow.isDead()) {
					cancel();
					return;
				}

				arrow.getWorld().spawnParticle(particle, arrow.getLocation(), 1, 0, 0, 0, 0);

			}
		}.runTaskTimer(MysteryUniversePlugin.getInstance(), 5, 1);

	}

}
