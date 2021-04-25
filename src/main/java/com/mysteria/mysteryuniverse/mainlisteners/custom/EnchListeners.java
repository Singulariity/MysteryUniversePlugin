package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.destroystokyo.paper.MaterialTags;
import com.mysteria.customapi.effects.CustomEffectType;
import com.mysteria.customapi.enchantments.CustomEnchantment;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityTypes;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.UUID;

public class EnchListeners implements Listener {

	public EnchListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	private void onItemDamage(PlayerItemDamageEvent e) {
		if (hasEnchant(e.getItem(), CustomEnchantment.UNBREAKABLE)) {
			e.setCancelled(true);
		}
		else if (hasEnchant(e.getItem(), CustomEnchantment.MELTING_CURSE)) {
			if (MysteriaUtils.chance(50)) e.setDamage(e.getDamage() * 2);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	private void onAttack(EntityDamageByEntityEvent e) {
		boolean isPiercingHit = false;
		boolean isCriticalHit = false;

		if (e.getEntity() instanceof LivingEntity && e.getDamager() instanceof LivingEntity) {
			LivingEntity damager = (LivingEntity) e.getDamager();
			LivingEntity victim = (LivingEntity) e.getEntity();

			// Decrease damage 40% if Player vs. Player
			if (damager instanceof Player && victim instanceof Player) {
				if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
						e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
					e.setDamage(e.getDamage() * 0.6);
				}
			}

			if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {

				EntityEquipment damagerEquipment = damager.getEquipment();
				if (damagerEquipment != null) {

					// PIERCING HIT PART
					if (victim.getEquipment() != null) {

						int piercing_chance = getTotalPercent(damager, CustomEnchantment.PIERCING_CHANCE);
						isPiercingHit = MysteriaUtils.chance(piercing_chance);

						if (!isPiercingHit) {
							ItemStack tool = damager.getEquipment().getItemInMainHand();
							if (MaterialTags.SWORDS.isTagged(tool)) {
								double defence = getTotalPercent(victim, CustomEnchantment.SWORD_DEFENCE);
								decreaseDamagePercent(e, defence);
							}
							else if (MaterialTags.AXES.isTagged(tool)) {
								double defence = getTotalPercent(victim, CustomEnchantment.AXE_DEFENCE);
								decreaseDamagePercent(e, defence);
							}
							else if (tool.getType() == Material.TRIDENT) {
								double defence = getTotalPercent(victim, CustomEnchantment.TRIDENT_DEFENCE);
								decreaseDamagePercent(e, defence);
							}
						} else {
							makePiercingHit(e);
						}

					}

					ItemStack tool = damagerEquipment.getItemInMainHand();

					// SUFFERING CURSE ENCHANTMENT
					if (hasEnchant(tool, CustomEnchantment.SUFFERING_CURSE)) {
						if (MysteriaUtils.chance(50)) damager.damage(1);
					}

					// FROSTBURN ENCHANTMENT
					if (hasEnchant(tool, CustomEnchantment.FROSTBURN)) {
						int level = getEnchantLevel(tool, CustomEnchantment.FROSTBURN);
						if (!victim.hasPotionEffect(CustomEffectType.FROSTBURN)) {
							PotionEffect frostburn = new PotionEffect(CustomEffectType.FROSTBURN, 10 * 20, (level == 0 ? 0 : level - 1));
							victim.addPotionEffect(frostburn);
						}
					}

					// CRITICAL HIT PART (if hit isn't piercing hit && if weapon isn't an axe or trident)
					if (!isPiercingHit && !MaterialTags.AXES.isTagged(tool) && tool.getType() != Material.TRIDENT) {

						// Getting Critical Hit Chance
						int crit_chance = getTotalPercent(damager, CustomEnchantment.CRITICAL_CHANCE);
						if (damager instanceof Player) {
							crit_chance += 5;
						}
						isCriticalHit = MysteriaUtils.chance(crit_chance);

						if (isCriticalHit) {
							// Getting Critical Hit Damage
							double crit_multiplier = 20.0 + getTotalPercent(damager, CustomEnchantment.CRITICAL_DAMAGE);

							// Make attack critical
							makeCriticalHit(e, crit_multiplier);
						}
					}

				}

			}

		}
		else if (e.getDamager() instanceof AbstractArrow) {
			AbstractArrow projectile = (AbstractArrow) e.getDamager();
			if (projectile.getShooter() instanceof LivingEntity && e.getEntity() instanceof LivingEntity) {
				LivingEntity damager = (LivingEntity) projectile.getShooter();
				LivingEntity victim = (LivingEntity) e.getEntity();

				// Decrease arrow and trident damage 30% if Player vs. Player
				if (damager instanceof Player && victim instanceof Player) {
					e.setDamage(e.getDamage() * 0.7);
				}

				// PIERCING HIT PART
				if (damager.getEquipment() != null && victim.getEquipment() != null) {

					int piercing_chance = getTotalPercent(damager, CustomEnchantment.PIERCING_CHANCE);
					isPiercingHit = MysteriaUtils.chance(piercing_chance);

					if (!isPiercingHit) {
						double defence;
						if (projectile instanceof Trident) {
							defence = getTotalPercent(victim, CustomEnchantment.TRIDENT_DEFENCE);
						} else {
							defence = getTotalPercent(victim, CustomEnchantment.ARROW_DEFENCE);
						}
						decreaseDamagePercent(e, defence);
					} else {
						makePiercingHit(e);
					}

				}

			}

		}

		if (e.getEntity() instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) e.getEntity();

			double finalDMG = e.getFinalDamage();
			DecimalFormat format = new DecimalFormat("0.0");
			String formatted = format.format(finalDMG);
			String line;
			TextColor color;
			if (finalDMG < 2) {
				color = NamedColor.SOARING_EAGLE;
			}
			else if (finalDMG < 4) {
				color = NamedColor.SILVER;
			}
			else if (finalDMG < 7) {
				color = NamedColor.BEEKEEPER;
			}
			else if (finalDMG < 12) {
				color = NamedColor.CARMINE_PINK;
			}
			else {
				color = NamedColor.PROTOSS_PYLON;
			}
			if (isPiercingHit) {
				line = "✴" + formatted + "✴";
			}
			else if (isCriticalHit) {
				line = "✦" + formatted + "✦";
				color = NamedColor.TURBO;
			}
			else {
				line = formatted;
			}

			Location loc = entity.getEyeLocation().add(
					MysteriaUtils.getRandom(-1d, 1d),
					0.3,
					MysteriaUtils.getRandom(-1d, 1d));
			if (!loc.getBlock().isPassable()) {
				loc = entity.getEyeLocation().add(0, 0.3, 0);
			}

			ArmorStand armorStand = spawnInvisibleArmorStand(loc);
			armorStand.setMarker(true);
			armorStand.setInvulnerable(true);
			armorStand.setDisabledSlots(EquipmentSlot.values());
			armorStand.setCustomNameVisible(true);
			armorStand.customName(Component.text(line, color));

			new BukkitRunnable() {
				@Override
				public void run() {
					if (armorStand.isValid()) {
						armorStand.remove();
					}
				}
			}.runTaskLater(MysteryUniversePlugin.getInstance(), 40);

		}

	}

	@Nonnull
	private ArmorStand spawnInvisibleArmorStand(@Nonnull Location loc) {
		net.minecraft.server.v1_16_R3.World w = ((CraftWorld) loc.getWorld()).getHandle();
		EntityArmorStand nmsEntity = new EntityArmorStand(EntityTypes.ARMOR_STAND, w);
		nmsEntity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		nmsEntity.setInvisible(true);
		nmsEntity.setSmall(true);
		w.addEntity(nmsEntity);
		return (ArmorStand) nmsEntity.getBukkitEntity();
	}

	@EventHandler
	private void onAxeThrow(PlayerInteractEvent e) {
		ItemStack item = e.getItem();

		if (item == null || item.getType() == Material.AIR) return;

		if (e.getAction() == Action.RIGHT_CLICK_AIR) {

			if (MaterialTags.AXES.isTagged(item) && item.containsEnchantment(CustomEnchantment.TOMAHAWK)) {
				item = item.clone();
				ItemStack item2 = item.clone();
				item2.setAmount(1);
				item.setAmount(item.getAmount() - 1);
				Player p = e.getPlayer();
				p.getInventory().setItemInMainHand(item);

				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.3f, 1);

				ArmorStand armorStand = spawnInvisibleArmorStand(p.getLocation().add(0, 1.1, 0));
				armorStand.setDisabledSlots(EquipmentSlot.values());
				armorStand.setInvulnerable(true);
				armorStand.setAI(true);
				armorStand.setCanTick(true);
				armorStand.setCanMove(true);
				armorStand.setItem(EquipmentSlot.HAND, item2);
				armorStand.setVelocity(calcVelocity(armorStand.getLocation().getYaw(), armorStand.getLocation().getPitch()));

				new BukkitRunnable() {
					int i = 0;
					final UUID owner = p.getUniqueId();
					Location axeLoc;
					@Override
					public void run() {
						if (armorStand.isValid() && i < 40) {
							if (i % 2 == 0) {
								armorStand.setRightArmPose(new EulerAngle(0, 0, 0));
							} else {
								armorStand.setRightArmPose(new EulerAngle(0, Math.toRadians(180), 0));
							}
							axeLoc = getArmTip(armorStand.getLocation(), armorStand.getRightArmPose());
							Block block = armorStand.getLocation().add(armorStand.getVelocity().normalize()).getBlock();
							Collection<LivingEntity> entities = armorStand.getLocation().getNearbyLivingEntities(0.5);
							entities.removeIf(entity -> entity.getUniqueId().equals(owner) ||
									entity.getUniqueId().equals(armorStand.getUniqueId()));
							if (block.isPassable() && !block.isLiquid() && entities.size() == 0) {
								armorStand.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, axeLoc, 1);
								i++;
								return;
							} else {
								if (!block.isLiquid()) {
									if (entities.size() == 0) {
										block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
									} else {
										for (LivingEntity entity : entities) {
											entity.damage(5, p);
										}
									}
								}
							}
						}
						if (armorStand.isValid()) {
							ItemStack drop = armorStand.getItem(EquipmentSlot.HAND);
							Item droppedItem = armorStand.getWorld().dropItem(axeLoc, drop);
							droppedItem.setOwner(owner);
							droppedItem.setCanMobPickup(false);
							droppedItem.setPickupDelay(30);
							droppedItem.setInvulnerable(true);
							armorStand.remove();
						}
						this.cancel();
					}
				}.runTaskTimer(MysteryUniversePlugin.getInstance(), 0, 1);

			}

		}
	}

	private Vector calcVelocity(final double yaw, final double pitch) {

		final double actualPower = 0.55 * 3;

		final double xVel = (-Math.sin(Math.toRadians(yaw))) * Math.cos(Math.toRadians(pitch));
		final double yVel = (-Math.sin(Math.toRadians(pitch)));
		final double zVel = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

		return new Vector(xVel, yVel, zVel).normalize().multiply(actualPower);
	}

	public Location getArmTip(Location location, EulerAngle rightArmPose) {
		// Gets shoulder location
		Location asl = location.clone();
		asl.setYaw(asl.getYaw() + 90f);
		Vector dir = asl.getDirection();
		asl.setX(asl.getX() + 5f / 16f * dir.getX());
		asl.setY(asl.getY() + 22f / 16f);
		asl.setZ(asl.getZ() + 5f / 16f * dir.getZ());
		// Get Hand Location

		Vector armDir = getDirection(rightArmPose.getY(), rightArmPose.getX(), -rightArmPose.getZ());
		armDir = rotateAroundAxisY(armDir, Math.toRadians(asl.getYaw()-90f));
		asl.setX(asl.getX() + 10f / 16f * armDir.getX());
		asl.setY(asl.getY() + 10f / 16f * armDir.getY());
		asl.setZ(asl.getZ() + 10f / 16f * armDir.getZ());
		return asl;
	}

	public Vector getDirection(Double yaw, Double pitch, Double roll) {
		Vector v = new Vector(0, -1, 0);
		v = rotateAroundAxisX(v, pitch);
		v = rotateAroundAxisY(v, yaw);
		v = rotateAroundAxisZ(v, roll);
		return v;
	}

	private Vector rotateAroundAxisX(Vector v, double angle) {
		double y, z, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		y = v.getY() * cos - v.getZ() * sin;
		z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}

	private Vector rotateAroundAxisY(Vector v, double angle) {
		angle = -angle;
		double x, z, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		x = v.getX() * cos + v.getZ() * sin;
		z = v.getX() * -sin + v.getZ() * cos;
		return v.setX(x).setZ(z);
	}

	private Vector rotateAroundAxisZ(Vector v, double angle) {
		double x, y, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		x = v.getX() * cos - v.getY() * sin;
		y = v.getX() * sin + v.getY() * cos;
		return v.setX(x).setY(y);
	}



	private boolean hasEnchant(@Nullable ItemStack itemStack, @Nullable Enchantment enchantment) {
		if (itemStack == null || itemStack.getType() == Material.AIR || enchantment == null) {
			return false;
		}
		return itemStack.getItemMeta().hasEnchant(enchantment);
	}

	private int getEnchantLevel(@Nullable ItemStack itemStack, @Nullable Enchantment enchantment) {
		if (itemStack == null || itemStack.getType() == Material.AIR || enchantment == null) {
			return 0;
		}
		return itemStack.getEnchantmentLevel(enchantment);
	}

	private int getTotalPercent(@Nonnull LivingEntity entity, @Nullable Enchantment enchantment) {
		EntityEquipment equipment = entity.getEquipment();
		if (equipment != null) {
			return getEnchantLevel(equipment.getItemInMainHand(), enchantment) +
					getEnchantLevel(equipment.getHelmet(), enchantment) +
					getEnchantLevel(equipment.getChestplate(), enchantment) +
					getEnchantLevel(equipment.getLeggings(), enchantment) +
					getEnchantLevel(equipment.getBoots(), enchantment);
		}
		return 0;
	}

	private void makeCriticalHit(@Nonnull EntityDamageByEntityEvent e, double add_percent) {
		double multiplier = add_percent / 100;
		e.setDamage(e.getDamage() * (multiplier + 1));
		World world = e.getEntity().getWorld();
		Location loc = e.getEntity().getLocation();
		world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
		world.spawnParticle(Particle.CRIT, loc, 20, 0, 1, 0);
	}

	private void makePiercingHit(@Nonnull EntityDamageByEntityEvent e) {
		World world = e.getEntity().getWorld();
		Location loc = e.getEntity().getLocation();
		world.playSound(loc, Sound.BLOCK_CHAIN_BREAK, 0.5f, 0.8f);
		if (e.getDamager() instanceof AbstractArrow) {
			AbstractArrow arrow = (AbstractArrow) e.getDamager();
			if (arrow.getShooter() instanceof Player && arrow.getShooter() != null) {
				Player damager = (Player) arrow.getShooter();
				damager.playSound(damager.getLocation(), Sound.BLOCK_CHAIN_BREAK, 0.5f, 0.8f);
			}
		}
		world.spawnParticle(Particle.CRIT_MAGIC, loc, 20, 0, 1, 0);
	}

	private void decreaseDamagePercent(@Nonnull EntityDamageByEntityEvent e, double decrease_percent) {
		e.setDamage(e.getDamage() * (1 - Math.min(decrease_percent / 100, 0.9)));
	}

}
