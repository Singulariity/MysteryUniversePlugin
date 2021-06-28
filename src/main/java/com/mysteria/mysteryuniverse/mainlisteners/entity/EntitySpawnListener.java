package com.mysteria.mysteryuniverse.mainlisteners.entity;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.nmsentities.CustomSkeleton;
import com.mysteria.mysteryuniverse.nmsentities.CustomStray;
import com.mysteria.mysteryuniverse.nmsentities.NMSUtils;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.monster.EntitySkeletonAbstract;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class EntitySpawnListener implements Listener {

	public EntitySpawnListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onSpawn(CreatureSpawnEvent e) {

		if (e.getEntity() instanceof ArmorStand) return;

		switch (e.getSpawnReason()) {
			case CUSTOM -> {
			}
			case EGG -> {
				if (e.getEntity() instanceof Chicken) e.setCancelled(true);
			}
			case BUILD_WITHER -> e.setCancelled(true);
			default -> modifySpawn(e.getEntity(), e);
		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkLoad(ChunkLoadEvent e) {
		if (e.isNewChunk()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (e.getChunk().isLoaded()) {
						for (Entity x : e.getChunk().getEntities()) {
							if (x instanceof LivingEntity && !(x instanceof Player)) {
								modifySpawn((LivingEntity) x, e);
							}
						}
					}
				}
			}.runTaskLater(MysteryUniversePlugin.getInstance(), 20);
		}

	}



	private void modifySpawn(@Nonnull LivingEntity entity, @Nonnull Event e) {
		LivingEntity finalEntity = modifyEntity(entity, e);
		EntityEquipment equipment = finalEntity.getEquipment();
		if (equipment != null) {
			equipment.setHelmetDropChance(0);
			equipment.setChestplateDropChance(0);
			equipment.setLeggingsDropChance(0);
			equipment.setBootsDropChance(0);
			if (equipment.getItemInMainHand().getType() != Material.TRIDENT) {
				equipment.setItemInMainHandDropChance(0);
			}
			if (equipment.getItemInOffHand().getType() != Material.NAUTILUS_SHELL) {
				equipment.setItemInOffHandDropChance(0);
			}
		}
		NMSUtils.setPathFinders(finalEntity);
	}

	@SuppressWarnings("ConstantConditions")
	@Nonnull
	private LivingEntity modifyEntity(@Nonnull LivingEntity entity, @Nonnull Event e) {
		EntityType entityType = entity.getType();

		switch (entityType) {
			case ZOMBIE, HUSK, ZOMBIE_VILLAGER -> {
				Zombie zombie;
				if (entityType == EntityType.ZOMBIE && MysteriaUtils.chance(15)) {
					this.removeEntity(e, entity);
					zombie = (Zombie) entity.getLocation().getWorld().spawnEntity(entity.getLocation(), EntityType.HUSK, CreatureSpawnEvent.SpawnReason.CUSTOM);
				} else {
					zombie = (Zombie) entity;
				}

				EntityBuilder.builder(zombie)
						.setHealth(32)
						.setKnockbackResistance(new Random().nextDouble(), 20)
						.giveArmor(4)
						.giveEffect(PotionEffectType.SPEED, MysteriaUtils.getRandom(0, 1), 15)
						.setMainHand(Material.WOODEN_AXE, 15);
				zombie.setShouldBurnInDay(false);

				return zombie;
			}
			case SKELETON, STRAY -> {
				this.removeEntity(e, entity);
				EntitySkeletonAbstract skeletonNMS;
				if (entityType == EntityType.STRAY || (entityType == EntityType.SKELETON && MysteriaUtils.chance(15))) {
					skeletonNMS = new CustomStray(entity.getLocation());
				} else {
					skeletonNMS = new CustomSkeleton(entity.getLocation());
				}
				((CraftWorld) entity.getWorld()).getHandle().addEntity(skeletonNMS, CreatureSpawnEvent.SpawnReason.CUSTOM);
				AbstractSkeleton skeleton = (AbstractSkeleton) skeletonNMS.getBukkitEntity();

				EntityBuilder builder = EntityBuilder.builder(skeleton)
						.setHealth(30)
						.giveEffect(PotionEffectType.SPEED, 0, 15)
						.giveArmor(4);

				int num = new Random().nextInt(100);
				if (entity.getLocation().getY() < 40 && num < 10) {

					builder.setMainHand(Material.STONE_PICKAXE)
							.setHelmet(Material.LEATHER_HELMET)
							.setName(Component.text("Skeleton Miner", NamedColor.BLUEBERRY_SODA));

				} else if (num < 28) {

					if (MysteriaUtils.chance(90)) {
						Material tool = num < 25 ? Material.WOODEN_SWORD : Material.STONE_SWORD;
						builder.setMainHand(tool);
					} else {
						builder.setMainHand(Material.STONE_SWORD)
								.setOffHand(Material.STONE_SWORD);
					}

				} else {
					builder.setMainHand(Material.BOW);
				}
				return skeleton;
			}
			case SPIDER -> {
				EntityBuilder.builder(entity)
						.giveEffect(PotionEffectType.SPEED, 0)
						.setHealth(24);

				if (entity.getPassengers().size() == 0 && MysteriaUtils.chance(5)) {
					new BukkitRunnable() {
						@Override
						public void run() {
							Skeleton passenger = (Skeleton) entity.getLocation().getWorld()
									.spawnEntity(entity.getLocation(), EntityType.SKELETON, CreatureSpawnEvent.SpawnReason.NATURAL);
							entity.addPassenger(passenger);
						}
					}.runTaskLater(MysteryUniversePlugin.getInstance(), 1);
				}
			}
			case CAVE_SPIDER -> EntityBuilder.builder(entity)
					.giveEffect(PotionEffectType.SPEED, 0)
					.setHealth(16);
			case CREEPER -> {
				Creeper creeper = (Creeper) entity;

				// Default Fuse Ticks: 30
				// Setting new fuse ticks between 15 - 30 (included)
				int newFuseTicks = new Random().nextInt(16) + 15;
				creeper.setMaxFuseTicks(newFuseTicks);

				// Default Explosion Radius: 3
				// Setting new explosion radius between 3 - 5
				int newExplosionRadius = new Random().nextInt(2) + 3;
				creeper.setExplosionRadius(newExplosionRadius);

				// Adding 3% chance to being powered creeper
				if (MysteriaUtils.chance(3)) creeper.setPowered(true);

				// Default Max Health: 20
				// Setting max health to 28
				EntityBuilder.builder(creeper)
						.setHealth(28);
			}
			case PHANTOM -> {
				Phantom phantom = (Phantom) entity;
				phantom.setSize(MysteriaUtils.getRandom(1, 4));

				EntityBuilder.builder(phantom)
						.setHealth(26)
						.giveEffect(PotionEffectType.SPEED, 0);
			}
			case DROWNED -> {
				Drowned drowned = (Drowned) entity;
				drowned.setShouldBurnInDay(false);

				EntityBuilder builder = EntityBuilder.builder(drowned)
						.setHealth(32);

				if (MysteriaUtils.chance(5)) {
					builder.giveEffect(PotionEffectType.INVISIBILITY, 0);
					drowned.setSilent(true);
				} else if (MysteriaUtils.chance(4)) {
					builder.giveArmor(100);
				}
			}
			case VINDICATOR, PILLAGER, EVOKER, ILLUSIONER -> {
				entity.setCanPickupItems(false);
				EntityBuilder.builder(entity)
						.setHealth(36);
			}
			case ZOMBIFIED_PIGLIN, PIGLIN -> EntityBuilder.builder(entity)
					.setHealth(40)
					.giveEffect(PotionEffectType.JUMP, 0);
			case WITCH -> EntityBuilder.builder(entity)
					.setHealth(50);
			case GUARDIAN -> {
				EntityBuilder builder = EntityBuilder.builder(entity)
						.giveEffect(PotionEffectType.SPEED, 0);

				if (MysteriaUtils.chance(5)) {
					builder.giveEffect(PotionEffectType.INVISIBILITY, 0);
					entity.setSilent(true);
				}
			}
			case ELDER_GUARDIAN -> EntityBuilder.builder(entity)
					.setHealth(240);
			default -> {
				if (entity instanceof Animals) {

					((Breedable) entity).setBreed(false);

					switch (entity.getType()) {
						case CHICKEN,
								OCELOT,
								CAT,
								PARROT,
								FOX -> EntityBuilder.builder(entity)
								.setHealth(16);
						case RABBIT -> {
							Rabbit rabbit = (Rabbit) entity;
							EntityBuilder builder = EntityBuilder.builder(rabbit);
							if (MysteriaUtils.chance(3)) {
								rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
								builder.setHealth(30)
										.giveEffect(PotionEffectType.JUMP, 2);
							} else {
								builder.setHealth(12);
							}
						}
						case POLAR_BEAR,
								PANDA -> EntityBuilder.builder(entity)
								.setHealth(60);
						case TURTLE -> EntityBuilder.builder(entity)
								.setHealth(40);
						case COW,
								MUSHROOM_COW,
								SHEEP,
								WOLF,
								PIG,
								DOLPHIN,
								SQUID,
								GLOW_SQUID -> EntityBuilder.builder(entity)
								.setHealth(24);
					}

				} else if (entity instanceof Fish) {
					EntityBuilder.builder(entity)
							.setHealth(10);
				}
			}
		}

		return entity;
	}





	private void removeEntity(@Nonnull Event e, @Nonnull LivingEntity entity) {
		if (e instanceof Cancellable) {
			((Cancellable) e).setCancelled(true);
		}
		else entity.remove();
	}

	@SuppressWarnings("unused")
	private static class EntityBuilder {

		private final LivingEntity entity;

		private EntityBuilder(@Nonnull LivingEntity entity) {
			this.entity = entity;
		}

		public static EntityBuilder builder(@Nonnull LivingEntity entity) {
			return new EntityBuilder(entity);
		}

		@Nonnull
		public LivingEntity getEntity() {
			return entity;
		}

		@Nonnull
		public EntityBuilder giveEffect(@Nonnull PotionEffectType type, int amplifier) {
			return giveEffect(type, amplifier, 100);
		}

		@Nonnull
		public EntityBuilder giveEffect(@Nonnull PotionEffectType type, int amplifier, int chance) {
			if (MysteriaUtils.chance(chance)) {
				entity.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, true, false));
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setHealth(double newHealth) {
			new BukkitRunnable() {
				@Override
				@SuppressWarnings("ConstantConditions")
				public void run() {
					AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
					attribute.setBaseValue(newHealth);
					entity.setHealth(newHealth);
				}
			}.runTaskLater(MysteryUniversePlugin.getInstance(), 2);
			return this;
		}

		@Nonnull
		@SuppressWarnings("ConstantConditions")
		public EntityBuilder setKnockbackResistance(double value, int chance) {
			if (MysteriaUtils.chance(chance)) {
				AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
				if (attribute == null) {
					entity.registerAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
				}
				attribute.setBaseValue(value);
			}
			return this;
		}

		@Nonnull
		@SuppressWarnings("ConstantConditions")
		public EntityBuilder giveArmor(int chance) {
			if (MysteriaUtils.chance(chance)) {
				EntityEquipment equipment = entity.getEquipment();

				if (equipment != null) {
					String material = switch (new Random().nextInt(6) + 1) {
						case 6 -> "NETHERITE";
						case 5 -> "DIAMOND";
						case 4 -> "IRON";
						case 3 -> "CHAINMAIL";
						case 2 -> "GOLDEN";
						default -> "LEATHER";
					};

					equipment.setHelmet(new ItemStack(Material.getMaterial(material + "_HELMET")));
					equipment.setChestplate(new ItemStack(Material.getMaterial(material + "_CHESTPLATE")));
					if (MysteriaUtils.chance(40)) {
						equipment.setLeggings(new ItemStack(Material.getMaterial(material + "_LEGGINGS")));
						equipment.setBoots(new ItemStack(Material.getMaterial(material + "_BOOTS")));
					}
				}
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setMainHand(@Nonnull Material material) {
			return setMainHand(material, 100);
		}

		@Nonnull
		public EntityBuilder setMainHand(@Nonnull Material material, int chance) {
			if (MysteriaUtils.chance(chance)) {
				return setMainHand(new ItemStack(material));
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setMainHand(@Nonnull ItemStack item) {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment != null) {
				equipment.setItemInMainHand(item);
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setOffHand(@Nonnull Material material) {
			return setOffHand(new ItemStack(material));
		}

		@Nonnull
		public EntityBuilder setOffHand(@Nonnull ItemStack item) {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment != null) {
				equipment.setItemInOffHand(item);
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setHelmet(@Nonnull Material material) {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment != null) {
				equipment.setHelmet(new ItemStack(material));
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setChestplate(@Nonnull Material material) {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment != null) {
				equipment.setChestplate(new ItemStack(material));
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setLeggings(@Nonnull Material material) {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment != null) {
				equipment.setLeggings(new ItemStack(material));
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setBoots(@Nonnull Material material) {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment != null) {
				equipment.setBoots(new ItemStack(material));
			}
			return this;
		}

		@Nonnull
		public EntityBuilder setName(@Nullable Component name) {
			entity.customName(name);
			return this;
		}



	}


}
