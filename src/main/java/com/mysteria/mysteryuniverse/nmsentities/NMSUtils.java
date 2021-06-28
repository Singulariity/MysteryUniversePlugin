package com.mysteria.mysteryuniverse.nmsentities;

import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalHurtByTarget;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.EntitySkeletonAbstract;
import net.minecraft.world.entity.monster.EntitySpider;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.entity.*;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NMSUtils {

	public static void setPathFinders(@Nullable LivingEntity entity) {
		if (entity == null) return;

		PathfinderGoalSelector goalSelector;
		PathfinderGoalSelector targetSelector;

		switch (entity.getType()) {
			case COD, SALMON -> {
				setAttributes(entity, 1D, 6D);
				EntityFish entityFish = ((CraftFish) entity).getHandle();
				goalSelector = entityFish.bO;
				targetSelector = entityFish.bP;
				goalSelector.a();
				targetSelector.a();
				goalSelector.a(0, new PathfinderGoalPanic(entityFish, 1.25D));
				goalSelector.a(4, new PathfinderGoalRandomSwim(entityFish));
				goalSelector.a(5, new PathfinderGoalFishSchool((EntityFishSchool) entityFish));
				goalSelector.a(6, new PathfinderGoalLeapAtTarget(entityFish, 0.4F));
				goalSelector.a(7, new PathfinderGoalMeleeAttack(entityFish, 1.3D, false));
				targetSelector.a(0, (new PathfinderGoalHurtByTarget(entityFish)).a(new Class[0]));
			}
			case PIG -> {
				setAttributes(entity, 2D, 6D);
				EntityPig entityPig = ((CraftPig) entity).getHandle();
				goalSelector = entityPig.bO;
				targetSelector = entityPig.bP;
				goalSelector.a(0, new PathfinderGoalLeapAtTarget(entityPig, 0.4F));
				goalSelector.a(1, new PathfinderGoalMeleeAttack(entityPig, 1.2D, false));
				targetSelector.a(0, (new PathfinderGoalHurtByTarget(entityPig)).a(new Class[0]));
			}
			case COW, MUSHROOM_COW -> {
				setAttributes(entity, 3D, 6D);
				EntityCow entityCow = ((CraftCow) entity).getHandle();
				goalSelector = entityCow.bO;
				targetSelector = entityCow.bP;
				goalSelector.a(0, new PathfinderGoalLeapAtTarget(entityCow, 0.4F));
				goalSelector.a(1, new PathfinderGoalMeleeAttack(entityCow, 1.2D, false));
				targetSelector.a(0, (new PathfinderGoalHurtByTarget(entityCow)).a(new Class[0]));
			}
			case SHEEP -> {
				setAttributes(entity, 3D, 6D);
				EntitySheep entitySheep = ((CraftSheep) entity).getHandle();
				goalSelector = entitySheep.bO;
				targetSelector = entitySheep.bP;
				goalSelector.a(0, new PathfinderGoalLeapAtTarget(entitySheep, 0.4F));
				goalSelector.a(1, new PathfinderGoalMeleeAttack(entitySheep, 1.2D, false));
				targetSelector.a(0, (new PathfinderGoalHurtByTarget(entitySheep)).a(new Class[0]));
			}
			case CHICKEN -> {
				setAttributes(entity, 1D, 6D);
				EntityChicken entityChicken = ((CraftChicken) entity).getHandle();
				goalSelector = entityChicken.bO;
				targetSelector = entityChicken.bP;
				goalSelector.a(0, new PathfinderGoalMeleeAttack(entityChicken, 1.3D, false));
				targetSelector.a(0, (new PathfinderGoalHurtByTarget(entityChicken)).a(new Class[0]));
			}
			case SPIDER, CAVE_SPIDER -> {
				EntitySpider entitySpider = ((CraftSpider) entity).getHandle();
				goalSelector = entitySpider.bO;
				targetSelector = entitySpider.bP;
				goalSelector.a();
				targetSelector.a();
				goalSelector.a(1, new PathfinderGoalFloat(entitySpider));
				goalSelector.a(3, new PathfinderGoalLeapAtTarget(entitySpider, 0.4F));
				goalSelector.a(4, new PathfinderGoalSpiderMeleeAttack(entitySpider));
				goalSelector.a(5, new PathfinderGoalRandomStrollLand(entitySpider, 0.8D));
				goalSelector.a(6, new PathfinderGoalLookAtPlayer(entitySpider, EntityHuman.class, 8.0F));
				goalSelector.a(6, new PathfinderGoalRandomLookaround(entitySpider));
				targetSelector.a(1, new PathfinderGoalHurtByTarget(entitySpider));
				targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(entitySpider, EntityHuman.class, true));
				targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(entitySpider, EntityIronGolem.class, true));
			}
			case SQUID, GLOW_SQUID -> {
				setAttributes(entity, 3D, 6D);
				EntitySquid entitySquid = ((CraftSquid) entity).getHandle();
				goalSelector = entitySquid.bO;
				targetSelector = entitySquid.bP;
				goalSelector.a(0, new PathfinderGoalLeapAtTarget(entitySquid, 0.4F));
				goalSelector.a(1, new PathfinderGoalMeleeAttack(entitySquid, 1.2D, false));
				targetSelector.a(0, new PathfinderGoalHurtByTarget(entitySquid));
			}
			case SKELETON, STRAY -> {
				EntitySkeletonAbstract entitySkeleton = (EntitySkeletonAbstract) ((CraftAbstractSkeleton) entity).getHandle();
				goalSelector = entitySkeleton.bO;
				targetSelector = entitySkeleton.bP;
				goalSelector.a();
				targetSelector.a();
				goalSelector.a(3, new PathfinderGoalAvoidTarget<>(entitySkeleton, EntityWolf.class, 6.0F, 1.0D, 1.2D));
				goalSelector.a(5, new PathfinderGoalRandomStrollLand(entitySkeleton, 1.0D));
				goalSelector.a(6, new PathfinderGoalLookAtPlayer(entitySkeleton, EntityHuman.class, 8.0F));
				goalSelector.a(6, new PathfinderGoalRandomLookaround(entitySkeleton));
				targetSelector.a(1, new PathfinderGoalHurtByTarget(entitySkeleton));
				targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(entitySkeleton, EntityHuman.class, true));
				targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(entitySkeleton, EntityIronGolem.class, true));
				targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(entitySkeleton, EntityTurtle.class, 10, true, false, EntityTurtle.bT));
			}
		}
	}

	private static void setAttributes(@Nonnull LivingEntity entity) {
		setAttributes(entity, null, null);
	}

	@SuppressWarnings("ConstantConditions")
	private static void setAttributes(@Nonnull LivingEntity entity, @Nullable Double attack_damage, @Nullable Double follow_range) {
		entity.registerAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
		if (attack_damage != null) {
			entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attack_damage);
		}
		entity.registerAttribute(Attribute.GENERIC_FOLLOW_RANGE);
		if (follow_range != null) {
			entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(follow_range);
		}
	}



	public static class PathfinderGoalRandomSwim extends net.minecraft.world.entity.ai.goal.PathfinderGoalRandomSwim {

		public PathfinderGoalRandomSwim(EntityFish entityfish) {
			super(entityfish, 1.0D, 40);
		}

		public boolean a() {
			return super.a();
		}
	}

	public static class PathfinderGoalSpiderMeleeAttack extends PathfinderGoalMeleeAttack {
		public PathfinderGoalSpiderMeleeAttack(EntitySpider entityspider) {
			super(entityspider, 1.0D, true);
		}

		public boolean a() {
			return super.a() && !this.a.isVehicle();
		}

		public boolean b() {
			float f = this.a.aY();
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
