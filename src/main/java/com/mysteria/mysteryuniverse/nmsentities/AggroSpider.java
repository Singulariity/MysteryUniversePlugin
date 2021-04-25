package com.mysteria.mysteryuniverse.nmsentities;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

public class AggroSpider extends EntitySpider {

	public AggroSpider(Location loc) {
		super(EntityTypes.SPIDER, ((CraftWorld) loc.getWorld()).getHandle());
		this.setPosition(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	public void initPathfinder() {

		this.goalSelector.a(1, new PathfinderGoalFloat(this));
		this.goalSelector.a(3, new PathfinderGoalLeapAtTarget(this, 0.4F));
		this.goalSelector.a(4, new PathfinderGoalSpiderMeleeAttack(this));
		this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
		this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, true));

	}



	static class PathfinderGoalSpiderMeleeAttack extends PathfinderGoalMeleeAttack {
		public PathfinderGoalSpiderMeleeAttack(EntitySpider entityspider) {
			super(entityspider, 1.0D, true);
		}

		public boolean a() {
			return super.a() && !this.a.isVehicle();
		}

		public boolean b() {
			float f = this.a.aR();
			if (f >= 0.5F && this.a.getRandom().nextInt(100) == 0) {
				this.a.setGoalTarget(null);
				return false;
			} else {
				return super.b();
			}
		}

		protected double a(EntityLiving entityliving) {
			return 4.0F + entityliving.getWidth();
		}
	}

}
