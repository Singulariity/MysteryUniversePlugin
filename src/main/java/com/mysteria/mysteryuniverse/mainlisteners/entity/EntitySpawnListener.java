package com.mysteria.mysteryuniverse.mainlisteners.entity;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.nmsentities.AggroCaveSpider;
import com.mysteria.mysteryuniverse.nmsentities.AggroSpider;
import com.mysteria.mysteryuniverse.nmsentities.CustomSkeleton;
import com.mysteria.mysteryuniverse.nmsentities.CustomStray;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class EntitySpawnListener implements Listener {

	public EntitySpawnListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onSpawn(CreatureSpawnEvent e) {

		if (e.getEntity() instanceof ArmorStand) return;

		switch (e.getSpawnReason()) {
			case CUSTOM:
				break;
			case EGG:
				if (e.getEntity() instanceof Chicken) e.setCancelled(true);
				break;
			case BUILD_WITHER:
				e.setCancelled(true);
				//e.getLocation().getNearbyPlayers(5).forEach(p -> p.sendMessage(Utils.coloredMessage("&8[&3MU&8] &4Withers cannot be spawned by this method.")));
				break;
			default:
				modifySpawn(e.getEntity(), e);
				break;

		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChunkLoad(ChunkLoadEvent e) {
		if (e.isNewChunk()) {
			for (Entity x : e.getChunk().getEntities()) {
				if (x instanceof LivingEntity) {
					modifySpawn((LivingEntity) x, e);
				}
			}
		}

	}



	private void modifySpawn(LivingEntity entity, Event e) {
		EntityEquipment equipment = modifyEntity(entity, e).getEquipment();
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
	}

	@SuppressWarnings("ConstantConditions")
	private LivingEntity modifyEntity(LivingEntity entity, Event e) {

		EntityType entityType = entity.getType();

		switch (entityType) {

			case ZOMBIE:
			case HUSK:
			case ZOMBIE_VILLAGER:
				Zombie zombie;

				if (entityType == EntityType.ZOMBIE && MysteriaUtils.chance(15)) {
					this.removeEntity(e, entity);
					zombie = (Zombie) entity.getLocation().getWorld().spawnEntity(entity.getLocation(), EntityType.HUSK, CreatureSpawnEvent.SpawnReason.CUSTOM);
				} else {
					zombie = (Zombie) entity;
				}

				if (MysteriaUtils.chance(15)) {
					this.giveEffect(zombie, PotionEffectType.SPEED, new Random().nextInt(2));
				}
				else if (MysteriaUtils.chance(15)) {
					if (!zombie.isAdult()) zombie.setAdult();
					zombie.getEquipment().setItemInMainHand(new ItemStack(Material.WOODEN_AXE));
				}

				if (MysteriaUtils.chance(20)) {
					this.setKnockbackResistance(zombie, new Random().nextDouble());
				}
				if (MysteriaUtils.chance(4)) {
					this.giveRandomArmor(zombie);
				}

				this.setHealth(zombie, 32.0);
				zombie.setShouldBurnInDay(false);
				return zombie;

			case SKELETON:
			case STRAY:
				Skeleton skeleton;

				this.removeEntity(e, entity);
				if (entityType == EntityType.STRAY || (entityType == EntityType.SKELETON && MysteriaUtils.chance(15))) {
					CustomStray customStray = new CustomStray(entity.getLocation());
					WorldServer world = ((CraftWorld) entity.getWorld()).getHandle();
					world.addEntity(customStray, CreatureSpawnEvent.SpawnReason.CUSTOM);

					skeleton = (Skeleton) customStray.getBukkitEntity();
				} else {
					CustomSkeleton customSkeleton = new CustomSkeleton(entity.getLocation());
					WorldServer world = ((CraftWorld) entity.getWorld()).getHandle();
					world.addEntity(customSkeleton, CreatureSpawnEvent.SpawnReason.CUSTOM);

					skeleton = (Skeleton) customSkeleton.getBukkitEntity();
				}

				EntityEquipment equipment = skeleton.getEquipment();

				int num = new Random().nextInt(100);

				if (entity.getLocation().getY() < 40 && num < 10) {

					equipment.setItemInMainHand(new ItemStack(Material.STONE_PICKAXE));
					equipment.setHelmet(new ItemStack(Material.LEATHER_HELMET));
					skeleton.customName(Component.text("Skeleton Miner", NamedColor.BLUEBERRY_SODA));

				}
				else if (num < 28) {

					if (MysteriaUtils.chance(90)) {
						ItemStack tool;
						if (num < 25) {
							tool = new ItemStack(Material.WOODEN_SWORD);
						} else {
							tool = new ItemStack(Material.STONE_SWORD);
						}
						equipment.setItemInMainHand(tool);
					} else {
						equipment.setItemInMainHand(new ItemStack(Material.STONE_SWORD));
						equipment.setItemInOffHand(new ItemStack(Material.STONE_SWORD));
					}

				}
				else {
					equipment.setItemInMainHand(new ItemStack(Material.BOW));
				}

				if (MysteriaUtils.chance(15)) {
					this.giveEffect(skeleton, PotionEffectType.SPEED, 0);
				}

				if (MysteriaUtils.chance(4)) {
					this.giveRandomArmor(skeleton);
				}

				this.setHealth(skeleton, 30.0);
				return skeleton;

			case SPIDER:
				this.removeEntity(e, entity);

				AggroSpider aggroSpider = new AggroSpider(entity.getLocation());
				WorldServer world = ((CraftWorld) entity.getWorld()).getHandle();
				world.addEntity(aggroSpider, CreatureSpawnEvent.SpawnReason.CUSTOM);

				LivingEntity spider = (LivingEntity) aggroSpider.getBukkitEntity();

				this.giveEffect(spider, PotionEffectType.SPEED, 0);
				this.setHealth(spider, 24.0);

				if (spider.getPassengers().size() == 0 && MysteriaUtils.chance(5)) {
					Bukkit.getScheduler().runTask(MysteryUniversePlugin.getInstance(), () -> {
						Skeleton passanger = (Skeleton) entity.getLocation().getWorld().spawnEntity(entity.getLocation(), EntityType.SKELETON, CreatureSpawnEvent.SpawnReason.CUSTOM);
						passanger.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
						spider.addPassenger(passanger);
					});
				}
				return spider;

			case CAVE_SPIDER:
				this.removeEntity(e, entity);

				AggroCaveSpider aggroCaveSpider = new AggroCaveSpider(entity.getLocation());
				WorldServer w = ((CraftWorld) entity.getWorld()).getHandle();
				w.addEntity(aggroCaveSpider, CreatureSpawnEvent.SpawnReason.CUSTOM);

				LivingEntity caveSpider = (LivingEntity) aggroCaveSpider.getBukkitEntity();

				this.giveEffect(caveSpider, PotionEffectType.SPEED, 0);
				this.setHealth(caveSpider, 16.0);
				return caveSpider;

			case CREEPER:
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
				this.setHealth(creeper, 28.0);

				return creeper;

			case PHANTOM:
				Phantom phantom = (Phantom) entity;

				phantom.setSize(MysteriaUtils.getRandom(1, 4));
				this.setHealth(phantom, 26.0);
				this.giveEffect(phantom, PotionEffectType.SPEED, 0);
				return phantom;

			case DROWNED:
				Drowned drowned = (Drowned) entity;

				if (MysteriaUtils.chance(5)) {
					this.giveEffect(drowned, PotionEffectType.INVISIBILITY, 0);
					drowned.setSilent(true);
				}
				else if (MysteriaUtils.chance(4)) {
					this.giveRandomArmor(drowned);
				}

				this.setHealth(drowned, 32.0);
				drowned.setShouldBurnInDay(false);
				return drowned;

			case VINDICATOR:
			case PILLAGER:
			case EVOKER:
			case ILLUSIONER:

				this.setHealth(entity, 36.0);
				entity.setCanPickupItems(false);
				return entity;

			case ZOMBIFIED_PIGLIN:
			case PIGLIN:

				this.setHealth(entity, 40.0);
				this.giveEffect(entity, PotionEffectType.JUMP, 0);
				return entity;

			case WITCH:
				this.setHealth(entity, 50.0);
				return entity;

			case GUARDIAN:
				this.giveEffect(entity, PotionEffectType.SPEED, 0);
				if (MysteriaUtils.chance(5)) {
					this.giveEffect(entity, PotionEffectType.INVISIBILITY, 0);
					entity.setSilent(true);
				}
				return entity;

			case ELDER_GUARDIAN:
				this.setHealth(entity, 240.0);
				return entity;

			default:

				if (entity instanceof Animals) {

					((Breedable) entity).setBreed(false);

					switch (entity.getType()) {
						case CHICKEN:
						case OCELOT:
						case CAT:
						case PARROT:
						case FOX:
							this.setHealth(entity, 16.0);
							return entity;

						case RABBIT:
							Rabbit rabbit = (Rabbit) entity;
							if (MysteriaUtils.chance(3)) {
								rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);
								this.setHealth(entity, 30.0);
								this.giveEffect(rabbit, PotionEffectType.JUMP, 1);
							} else {
								this.setHealth(rabbit, 12.0);
							}
							return rabbit;

						case POLAR_BEAR:
						case PANDA:
							this.setHealth(entity, 60.0);
							return entity;

						case COW:
						case MUSHROOM_COW:
						case SHEEP:
						case WOLF:
						case PIG:
						case DOLPHIN:
						case SQUID:
							this.setHealth(entity, 24.0);
							return entity;
						case TURTLE:
							this.setHealth(entity, 40.0);
							return entity;
						default:
							break;

					}

				}
				else if (entity instanceof Fish) {
					this.setHealth(entity, 10);
				}
				return entity;


		}

	}


	@SuppressWarnings("ConstantConditions")
	private void giveRandomArmor(LivingEntity entity) {
		EntityEquipment equipment = entity.getEquipment();

		if (equipment == null) return;

		String material;
		switch (new Random().nextInt(6) + 1) {
			case 6:
				material = "NETHERITE";
				break;
			case 5:
				material = "DIAMOND";
				break;
			case 4:
				material = "IRON";
				break;
			case 3:
				material = "CHAINMAIL";
				break;
			case 2:
				material = "GOLDEN";
				break;
			case 1:
			default:
				material = "LEATHER";
				break;
		}

		equipment.setHelmet(new ItemStack(Material.getMaterial(material + "_HELMET")));
		equipment.setChestplate(new ItemStack(Material.getMaterial(material + "_CHESTPLATE")));
		if (MysteriaUtils.chance(40)) {
			equipment.setLeggings(new ItemStack(Material.getMaterial(material + "_LEGGINGS")));
			equipment.setBoots(new ItemStack(Material.getMaterial(material + "_BOOTS")));
		}

	}

	private void setKnockbackResistance(LivingEntity entity, double value) {
		AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
		if (attribute == null) return;

		attribute.setBaseValue(value);
	}

	private void setHealth(LivingEntity entity, double newHealth) {
		new BukkitRunnable() {
			@Override
			public void run() {
				AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				if (attribute == null) return;

				attribute.setBaseValue(newHealth);
				entity.setHealth(newHealth);
			}
		}.runTaskLater(MysteryUniversePlugin.getInstance(), 2);
	}

	private void giveEffect(LivingEntity entity, PotionEffectType type, int amplifier) {
		entity.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, true, false));
	}

	private void removeEntity(Event e, LivingEntity entity) {
		if (e instanceof Cancellable) {
			((Cancellable) e).setCancelled(true);
		}
		else entity.remove();
	}


}
