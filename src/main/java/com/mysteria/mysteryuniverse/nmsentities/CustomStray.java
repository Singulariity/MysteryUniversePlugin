package com.mysteria.mysteryuniverse.nmsentities;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntitySkeletonStray;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

public class CustomStray extends EntitySkeletonStray {

	public CustomStray(Location loc) {
		super(EntityTypes.aK, ((CraftWorld) loc.getWorld()).getHandle());
		this.setPosition(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	protected boolean fr() {
		return false;
	}

}