package com.mysteria.mysteryuniverse.systems.meteorite;

import com.mysteria.compass.CompassPlugin;
import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.systems.meteorite.events.MeteoriteLandEvent;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class Meteorite {

	private final Location location;
	private BukkitTask task;
	private int timerInSeconds;
	private final boolean hidden;
	private final HashMap<ItemStack, Integer> loots;
	private FallingBlock meteoriteEntity;

	public Meteorite(@Nonnull Location location, int timerInSeconds, boolean hidden, @Nonnull HashMap<ItemStack, Integer> loots) {
		this.location = location;
		this.timerInSeconds = timerInSeconds;
		this.hidden = hidden;
		this.loots = loots;
		this.meteoriteEntity = null;
		init();
	}

	public Meteorite(@Nonnull Location location, int timerInSeconds, boolean hidden) {
		this(location, timerInSeconds, hidden, MysteryUniversePlugin.getMeteoriteManager().getDefaultLoots());
	}

	private void init() {
		sendMeteoriteMessage(Component.text(" "), false);
		sendMeteoriteMessage(
				Component.text("A meteorite has been sighted!",
				NamedColor.CARMINE_PINK)
				.decorate(TextDecoration.BOLD), false);
		sendMeteoriteMessage(Component.text(" "), false);

		this.task = new BukkitRunnable() {
			@Override
			public void run() {
				if (this.isCancelled()) {
					return;
				}

				if (timerInSeconds <= 0) {
					sendMeteoriteMessage(
							Component.text("The meteorite is landing!",
							NamedColor.CARMINE_PINK), false);
					this.cancel();
					task = spawn();
					return;
				}

				if (timerInSeconds % 60 == 0 || (timerInSeconds <= 30 && (timerInSeconds % 10 == 0 || timerInSeconds <= 5))) {
					sendMeteoriteMessage(Component.text("A meteorite will land in ", NamedColor.TURBO)
									.append(Component.text(timerInSeconds, NamedColor.CARMINE_PINK))
									.append(Component.text(" second(s)!", NamedColor.TURBO)), false);
					setCompass();
				}
				if (timerInSeconds % 60 == 0) {
					if (!hidden) {
						sendMeteoriteMessage(
								Component.text("Special option is available now on the compass for the location where the meteorite will land!",
								NamedColor.TURBO), false);
					}
					sendMeteoriteMessage(
							Component.text("Meteorite Location: ", NamedColor.TURBO)
									.append(Component.text("x: " + location.getBlockX() + ", y: " + location.getBlockY() + ", z: " + location.getBlockZ(), NamedColor.SKIRRET_GREEN))
									.append(Component.text(" (Only staffs can see this message)", NamedColor.SOARING_EAGLE)), true);
				}
				timerInSeconds--;

			}
		}.runTaskTimer(MysteryUniversePlugin.getInstance(), 0, 20);
	}



	private void sendMeteoriteMessage(Component message, boolean onlyStaffs) {
		if (hidden || onlyStaffs) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("survivalplus.bypass")) {
					MysteriaUtils.sendMessage(p, message);
				}
			}
		} else {
			MysteriaUtils.broadcastMessage(message);
		}
	}

	@Nonnull
	private BukkitTask spawn() {
		Location l = location.clone();

		if (l.getBlockY() < 255) {
			int diff = 255 - l.getBlockY();
			l.setY(l.getY() + diff);
			l.setZ(l.getZ() - diff);
		}

		setMeteoriteEntity(l);

		MeteoriteManager meteoriteManager = MysteryUniversePlugin.getMeteoriteManager();

		return new BukkitRunnable() {
			final World world = l.getWorld();

			@Override
			public void run() {

				if (meteoriteEntity == null || !meteoriteEntity.isValid()) {
					cancelMeteorite();
					return;
				}

				l.setY(l.getY() - 1);
				l.setZ(l.getZ() + 1);

				setMeteoriteEntity(l);
				world.playEffect(l, Effect.MOBSPAWNER_FLAMES, 0);
				world.playSound(l, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 4, 5);

				if (meteoriteManager.checkSolidBlock(l)) {

					meteoriteEntity.remove();
					this.cancel();

					sendMeteoriteMessage(Component.text("The meteorite has been landed.", NamedColor.CARMINE_PINK), false);
					if (!hidden) {
						sendMeteoriteMessage(Component.text("Special compass option is not available anymore.", NamedColor.CARMINE_PINK), false);
						removeCompass();
					}

					MeteoriteLandEvent meteoriteLandEvent = new MeteoriteLandEvent(l);
					Bukkit.getPluginManager().callEvent(meteoriteLandEvent);

					if (meteoriteLandEvent.isCancelled()) {
						return;
					}

					world.strikeLightningEffect(l);
					TNTPrimed tntPrimed = (TNTPrimed) l.getWorld().spawnEntity(l, EntityType.PRIMED_TNT);
					tntPrimed.setYield(6F);
					tntPrimed.setFuseTicks(0);

					new BukkitRunnable() {
						@Override
						public void run() {
							if (MysteriaUtils.chance(10)) {
								world.dropItem(l, CustomItem.METEORITE_CORE.getItemStack());
							}

							l.setY(l.getY() + 1);
							for (Map.Entry<ItemStack, Integer> x : loots.entrySet()) {

								ItemStack item = x.getKey();
								Integer amount = x.getValue();

								for (int i = 0; i < amount; i++) {

									Item dropped = world.dropItem(l, item);
									ItemMeta meta = dropped.getItemStack().getItemMeta();

									NamespacedKey key = meteoriteManager.getNonStackableKey();
									meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, MysteriaUtils.getRandom(0, 10000));
									dropped.getItemStack().setItemMeta(meta);

									dropped.setVelocity(dropped.getVelocity()
											.add(new Vector(MysteriaUtils.getRandom(-3D, 3D),
													MysteriaUtils.getRandom(0.5D, 2D),
													MysteriaUtils.getRandom(-3D, 3D))
													.normalize()
													.multiply(MysteriaUtils.getRandom(0.3D, 1.2D))));
								}

							}
						}
					}.runTaskLater(MysteryUniversePlugin.getInstance(), 15);

				}

			}
		}.runTaskTimer(MysteryUniversePlugin.getInstance(), 0, 2);

	}

	private void setCompass() {
		CompassPlugin.getCompassManager().setMeteoriteLocation(location);
	}

	private void removeCompass() {
		CompassPlugin.getCompassManager().setMeteoriteLocation(null);
	}

	public void cancelMeteorite() {
		task.cancel();
		removeCompass();
	}

	private void setMeteoriteEntity(@Nonnull Location location) {
		if (meteoriteEntity != null && meteoriteEntity.isValid()) {
			meteoriteEntity.remove();
		}
		meteoriteEntity = location.getWorld().spawnFallingBlock(location, Bukkit.createBlockData(Material.MAGMA_BLOCK));
		meteoriteEntity.setGravity(false);
		meteoriteEntity.setDropItem(false);
	}


}
