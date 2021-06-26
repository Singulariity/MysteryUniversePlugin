package com.mysteria.mysteryuniverse.nmsentities;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.animal.EntityTurtle;
import net.minecraft.world.entity.animal.EntityWolf;
import net.minecraft.world.entity.monster.EntitySkeleton;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

public class CustomSkeleton extends EntitySkeleton {

	public CustomSkeleton(Location loc) {
		super(EntityTypes.aB, ((CraftWorld) loc.getWorld()).getHandle());
		this.setPosition(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	protected void initPathfinder() {
		this.bO.a(3, new PathfinderGoalAvoidTarget<>(this, EntityWolf.class, 6.0F, 1.0D, 1.2D));
		this.bO.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
		this.bO.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
		this.bO.a(6, new PathfinderGoalRandomLookaround(this));
		this.bP.a(1, new PathfinderGoalHurtByTarget(this));
		this.bP.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true));
		this.bP.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, true));
		this.bP.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, true, false, EntityTurtle.bT));
	}

	@Override
	protected boolean fr() {
		return false;
	}

}
