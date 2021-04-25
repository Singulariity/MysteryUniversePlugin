package com.mysteria.mysteryuniverse.systems.theend;

import com.google.common.collect.ImmutableSet;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class EndListeners implements Listener {

	private final ImmutableSet<EnderDragon.Phase> PHASES = new ImmutableSet.Builder<EnderDragon.Phase>()
			.add(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET)
			.add(EnderDragon.Phase.ROAR_BEFORE_ATTACK)
			.add(EnderDragon.Phase.LAND_ON_PORTAL)
			.add(EnderDragon.Phase.BREATH_ATTACK)
			.build();

	public EndListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onEnderDragonChangePhase(EnderDragonChangePhaseEvent e) {

		if (e.getCurrentPhase() == EnderDragon.Phase.LAND_ON_PORTAL && PHASES.contains(e.getNewPhase())) {

			int duration = 20 * 20;

			e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 4, true, false));
			e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0, true, false));

			new BukkitRunnable() {
				@Override
				public void run() {

					e.getEntity().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
					e.getEntity().removePotionEffect(PotionEffectType.GLOWING);

					if (PHASES.contains(e.getEntity().getPhase())) {

						e.getEntity().setPhase(EnderDragon.Phase.LEAVE_PORTAL);
					}

				}
			}.runTaskLater(MysteryUniversePlugin.getInstance(), duration);


		} else if (e.getNewPhase() != EnderDragon.Phase.BREATH_ATTACK && e.getNewPhase() != EnderDragon.Phase.ROAR_BEFORE_ATTACK && e.getNewPhase() != EnderDragon.Phase.DYING) {

			Location loc = e.getEntity().getLocation();

			Collection<Player> players = loc.getNearbyPlayers(300);

			if (players.size() == 0) return;

			double mult;
			if (players.size() == 1) mult = players.size();
			else mult = (double) players.size() / 2;

			EndManager endManager = MysteryUniversePlugin.getEndManager();

			for (Player p : players) if (endManager.getPlayerChance(p, mult)) endManager.spawnEnderman(p);

			/*
			if (Utils.chance(30)) {
				World end_world = endManager.getWorld();
				if (end_world == null) return;
				for (Player p : players) {
					end_world.strikeLightning(p.getLocation());
				}
			}
			 */

		} else if (e.getNewPhase() == EnderDragon.Phase.DYING) {

			Player killer = null;
			if (e.getEntity().getKiller() != null) killer = e.getEntity().getKiller();

			Player finalKiller = killer;
			new BukkitRunnable() {
				@Override
				public void run() {

					if (!e.getEntity().isValid()) {

						EndManager endManager = MysteryUniversePlugin.getEndManager();

						HashMap<Player, Double> xxx = endManager.getTopDamagers();

						List<Player> top_players = new ArrayList<>(xxx.keySet());
						List<Double> top_damages = new ArrayList<>(xxx.values());

						NumberFormat comma = NumberFormat.getInstance();
						comma.setGroupingUsed(true);

						Component outLine = MysteriaUtils.centeredComponent(
								Component.text("---------------------------------------------",
								NamedColor.DOWNLOAD_PROGRESS).decorate(TextDecoration.STRIKETHROUGH).decorate(TextDecoration.BOLD));
						Component line1 = MysteriaUtils.centeredComponent(
								Component.text("ENDER DRAGON DOWN!", NamedColor.TURBO).decorate(TextDecoration.BOLD));
						Component line2 = MysteriaUtils.centeredComponent(
								Component.text(finalKiller.getName(), NamedColor.DOWNLOAD_PROGRESS)
										.append(Component.text(" dealt the final blow.", NamedColor.SOARING_EAGLE)));
						Component topkiller1;
						if (top_players.size() >= 1) {
							double dmg = top_damages.get(0);
							int dealt = (int) dmg;
							topkiller1 = MysteriaUtils.centeredComponent(
									Component.text()
											.append(Component.text("1st Damager", NamedColor.RISE_N_SHINE)
													.decorate(TextDecoration.BOLD))
											.append(Component.text(" - ", NamedColor.SOARING_EAGLE))
											.append(Component.text(top_players.get(0).getName(), NamedColor.DOWNLOAD_PROGRESS))
											.append(Component.text(" - ", NamedColor.SOARING_EAGLE))
											.append(Component.text(comma.format(dealt), NamedColor.RISE_N_SHINE))
											.build());
						} else {
							topkiller1 = null;
						}
						Component topkiller2;
						if (top_players.size() >= 2) {
							double dmg = top_damages.get(1);
							int dealt = (int) dmg;
							topkiller2 = MysteriaUtils.centeredComponent(
									Component.text()
											.append(Component.text("2nd Damager", NamedColor.TURBO)
													.decorate(TextDecoration.BOLD))
											.append(Component.text(" - ", NamedColor.SOARING_EAGLE))
											.append(Component.text(top_players.get(1).getName(), NamedColor.DOWNLOAD_PROGRESS))
											.append(Component.text(" - ", NamedColor.SOARING_EAGLE))
											.append(Component.text(comma.format(dealt), NamedColor.RISE_N_SHINE))
											.build());
						} else {
							topkiller2 = null;
						}
						Component topkiller3;
						if (top_players.size() >= 3) {
							double dmg = top_damages.get(2);
							int dealt = (int) dmg;
							topkiller3 = MysteriaUtils.centeredComponent(
									Component.text()
											.append(Component.text("3rd Damager", NamedColor.CARMINE_PINK)
													.decorate(TextDecoration.BOLD))
											.append(Component.text(" - ", NamedColor.SOARING_EAGLE))
											.append(Component.text(top_players.get(2).getName(), NamedColor.DOWNLOAD_PROGRESS))
											.append(Component.text(" - ", NamedColor.SOARING_EAGLE))
											.append(Component.text(comma.format(dealt), NamedColor.RISE_N_SHINE))
											.build());
						} else {
							topkiller3 = null;
						}

						for (Player p : Bukkit.getOnlinePlayers()) {

							p.sendMessage(outLine);
							p.sendMessage(line1);
							p.sendMessage(line2);
							if (topkiller1 != null) {
								p.sendMessage(topkiller1);
							}
							if (topkiller2 != null) {
								p.sendMessage(topkiller2);
							}
							if (topkiller3 != null) {
								p.sendMessage(topkiller3);
							}
							int damage = (int) endManager.getPlayerDamage(p);
							if (damage > 0) {
								Component player_damage = Component.text()
										.append(Component.text("Your Damage: ", NamedColor.RISE_N_SHINE))
										.append(Component.text(comma.format(damage), NamedColor.DOWNLOAD_PROGRESS))
										.append(
												Component.text(" (Position #" + (top_players.indexOf(p) + 1) + ")",
														NamedColor.SOARING_EAGLE))
										.build();
								p.sendMessage(MysteriaUtils.centeredComponent(player_damage));
							}
							p.sendMessage(outLine);

							if (p.getWorld() != endManager.getWorld()) {
								p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, SoundCategory.HOSTILE, 10, 1);
							}

						}
						this.cancel();

					}

				}

			}.runTaskTimer(MysteryUniversePlugin.getInstance(), 0, 5);


		}


	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent e) {

		if (e.getEntity() instanceof EnderDragon) {
			EndManager endManager = MysteryUniversePlugin.getEndManager();

			if (e.getDamager() instanceof Player) {

				Player p = (Player) e.getDamager();

				endManager.addPlayerDamage(p, e.getFinalDamage());

				if (endManager.getPlayerChance(p, 0.3)) endManager.spawnEnderman(p);

			} else if (e.getDamager() instanceof Projectile) {
				Projectile proj = (Projectile) e.getDamager();

				if (proj.getShooter() instanceof Player) {
					Player p = (Player) proj.getShooter();

					endManager.addPlayerDamage(p, e.getFinalDamage());

					if (endManager.getPlayerChance(p, 0.3)) endManager.spawnEnderman(p);
				}

			}


		} else if (e.getEntity() instanceof EnderCrystal) {
			World end_world = MysteryUniversePlugin.getEndManager().getWorld();

			if (end_world.getEnderDragonBattle() == null || end_world.getEnderDragonBattle().getEndPortalLocation() == null) return;

			Location loc = end_world.getEnderDragonBattle().getEndPortalLocation();

			if (e.getEntity().getLocation().getY() - 1 == loc.getY()) e.setCancelled(true);

		} else if (e.getEntity() instanceof Enderman && e.getDamager() instanceof Player) {

			if (e.getEntity().getWorld() != MysteryUniversePlugin.getEndManager().getWorld()) return;

			Enderman enderman = (Enderman) e.getEntity();

			if (enderman.getTarget() != null) return;

			for (LivingEntity entity : e.getEntity().getLocation().getNearbyLivingEntities(10)) {
				if (!(entity instanceof Enderman) || entity == enderman) continue;

				Enderman nearEnderman = (Enderman) entity;

				if (nearEnderman.getTarget() != null) continue;

				nearEnderman.setTarget((Player) e.getDamager());
				nearEnderman.teleportRandomly();

			}

		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntity().getWorld() != MysteryUniversePlugin.getEndManager().getWorld()) return;


		if (e.getEntity() instanceof Enderman) {

			e.getDrops().removeIf(itemStack -> itemStack.getType() == Material.ENDER_PEARL);
			e.setDroppedExp(0);

		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		EndManager endManager = MysteryUniversePlugin.getEndManager();

		if (e.getFrom().getWorld() == endManager.getWorld()) {
			//setDamage(e.getPlayer(), 0.0);
		}
	}


	@EventHandler(ignoreCancelled = true)
	public void onPlayerPortal(PlayerPortalEvent e) {

		if (e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
			e.setCancelled(true);
			MysteryUniversePlugin.getEndManager().teleportToEnd(e.getPlayer());
		}

	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEndermanTargetEnderDragon(EntityTargetEvent e) {

		if (e.getTarget() instanceof EnderDragon && e.getEntity() instanceof Enderman) {
			e.setCancelled(true);
		}

	}

}
