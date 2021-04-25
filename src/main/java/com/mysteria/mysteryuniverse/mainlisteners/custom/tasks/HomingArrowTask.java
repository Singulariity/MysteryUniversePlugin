package com.mysteria.mysteryuniverse.mainlisteners.custom.tasks;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class HomingArrowTask extends BukkitRunnable {

	Arrow arrow;
	LivingEntity target;
	int timer;

	public HomingArrowTask(Arrow arrow, LivingEntity target) {
		this.arrow = arrow;
		this.target = target;
		this.timer = 0;
		runTaskTimer(MysteryUniversePlugin.getInstance(), 1L, 1L);
	}

	public void run() {
		Vector newVelocity;
		double speed = this.arrow.getVelocity().length();
		if (
				this.timer >= 600 || this.arrow.isOnGround() || this.arrow.isDead() || this.target.isDead() ||
						(
								((target instanceof Player) && (((Player) target).isBlocking()))
										&&
										(arrow.getWorld() == target.getWorld() && 5 > arrow.getLocation().distance(target.getLocation()))
						)
		) {
			cancel();
			return;
		}
		Vector toTarget = this.target.getLocation().clone().add(new Vector(0.0D, 0.5D, 0.0D))
				.subtract(this.arrow.getLocation()).toVector();
		Vector dirVelocity = this.arrow.getVelocity().clone().normalize();
		Vector dirToTarget = toTarget.clone().normalize();
		double angle = dirVelocity.angle(dirToTarget);
		double newSpeed = 0.9D * speed + 0.14D;
		if (this.target instanceof Player &&
				this.arrow.getLocation().distance(this.target.getLocation()) < 8.0D) {
			Player player = (Player) this.target;
			if (player.isBlocking())
				newSpeed = speed * 0.6D;
		}
		if (angle < 0.12D) {
			newVelocity = dirVelocity.clone().multiply(newSpeed);
		} else {
			Vector newDir = dirVelocity.clone().multiply((angle - 0.12D) / angle)
					.add(dirToTarget.clone().multiply(0.12D / angle));
			newDir.normalize();
			newVelocity = newDir.clone().multiply(newSpeed);
		}
		this.arrow.setVelocity(newVelocity.add(new Vector(0.0D, 0.03D, 0.0D)));
		this.arrow.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, this.arrow.getLocation(), 1, 0, 0, 0, 0);
		//this.arrow.getWorld().playEffect(this.arrow.getLocation(), Effect.SMOKE, 0);
		timer++;
	}
}