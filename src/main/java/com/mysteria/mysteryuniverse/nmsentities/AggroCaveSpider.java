package com.mysteria.mysteryuniverse.nmsentities;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

public class AggroCaveSpider extends EntityCaveSpider {

	public AggroCaveSpider(Location loc) {
		super(EntityTypes.CAVE_SPIDER, ((CraftWorld) loc.getWorld()).getHandle());
		this.setPosition(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	public void initPathfinder() {

		this.goalSelector.a(1, new PathfinderGoalFloat(this));
		this.goalSelector.a(3, new PathfinderGoalLeapAtTarget(this, 0.4F));
		this.goalSelector.a(4, new AggroSpider.PathfinderGoalSpiderMeleeAttack(this));
		this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
		this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
		this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, true));

	}

}
