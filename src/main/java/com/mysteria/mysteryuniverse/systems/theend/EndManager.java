package com.mysteria.mysteryuniverse.systems.theend;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EnderDragonBattle;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.v1_16_R3.boss.CraftDragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class EndManager {

	private final HashMap<Player, Double> damages = new HashMap<>();
	private final Component endermanName = Component.text("???", Style.style(NamedTextColor.BLACK, TextDecoration.OBFUSCATED));
	private final World end_world;
	private final DragonBattle dragonBattle;
	private final String bossBarTitle = MysteriaUtils.legacyColoredText("&5&lEnder Dragon");

	public EndManager() {
		if (MysteryUniversePlugin.getEndManager() != null) {
			throw new IllegalStateException();
		}
		end_world = Bukkit.getWorld("world_the_end");
		if (end_world == null ||
				end_world.getEnvironment() != World.Environment.THE_END ||
				end_world.getEnderDragonBattle() == null) {
			MysteryUniversePlugin.getInstance().getLogger().warning("End world not found.");
			Bukkit.getServer().shutdown();
			throw new IllegalStateException();
		}
		dragonBattle = end_world.getEnderDragonBattle();
		try {
			Field handle = ((CraftDragonBattle) dragonBattle).getClass().getDeclaredField("handle");
			handle.setAccessible(true);
			EnderDragonBattle enderDragonBattle = (EnderDragonBattle) handle.get(dragonBattle);
			enderDragonBattle.exitPortalLocation = new BlockPosition(0, -10, 0);
		} catch (Exception ignored) {
			MysteryUniversePlugin.getInstance().getLogger().warning("ENDER DRAGON PORTAL COULDN'T SET!");
		}
		new EndListeners();
		new BukkitRunnable() {
			@Override
			public void run() {
				resetDragon(false);
			}
		}.runTaskTimer(MysteryUniversePlugin.getInstance(), 600, 200);
	}

	public Component getEndermanName() {
		return endermanName;
	}

	@Nonnull
	public World getWorld() {
		return end_world;
	}

	@Nonnull
	public DragonBattle getDragonBattle() {
		return dragonBattle;
	}

	public void teleportToEnd(@Nonnull Player p) {
		Block block;
		do {
			block = end_world.getHighestBlockAt(MysteriaUtils.getRandom(-100, 100), MysteriaUtils.getRandom(-100, 100));
		} while (block.getY() < 5 || block.getType() != Material.END_STONE);


		p.teleportAsync(block.getLocation().add(0.5, 1, 0.5), PlayerTeleportEvent.TeleportCause.PLUGIN);

	}

	public double getPlayerDamage(@Nonnull Player p) {
		return damages.getOrDefault(p, 0D);
	}

	/**
	 * @param includeAll include offline and non end island players
	 * @return total damage dealt to dragon
	 */
	public double getTotalPlayerDamage(boolean includeAll) {
		double damage_total = 0;
		for (Map.Entry<Player, Double> x : damages.entrySet()) {
			if (!includeAll) {
				Player p = x.getKey();
				if (!p.isOnline() || p.getWorld() != this.getWorld()) {
					continue;
				}
			}
			damage_total += x.getValue();
		}

		return damage_total;
	}

	public void setPlayerDamage(@Nonnull Player p, double d) {
		if (d > 0) {
			damages.put(p, d);
		} else {
			damages.remove(p);
		}
	}

	public void addPlayerDamage(@Nonnull Player p, double d) {
		if (damages.containsKey(p)) {
			damages.replace(p, damages.get(p) + d);
		} else {
			damages.put(p, d);
		}
		if (damages.get(p) < 0) damages.replace(p, 0D);
	}

	public boolean respawnDragon(@Nullable Player summoner) {

		DragonBattle db = getDragonBattle();

		if (db.getEndPortalLocation() == null ||
				db.getEnderDragon() != null ||
				db.getRespawnPhase() != DragonBattle.RespawnPhase.NONE) return false;

		Location loc = db.getEndPortalLocation().clone();
		loc.set(loc.getX() + 0.5, loc.getY(), loc.getZ() + 0.5);

		getWorld().spawnEntity(loc.clone().add(3, 1, 0), EntityType.ENDER_CRYSTAL, CreatureSpawnEvent.SpawnReason.CUSTOM);
		getWorld().spawnEntity(loc.clone().add(-3, 1, 0), EntityType.ENDER_CRYSTAL, CreatureSpawnEvent.SpawnReason.CUSTOM);
		getWorld().spawnEntity(loc.clone().add(0, 1, 3), EntityType.ENDER_CRYSTAL, CreatureSpawnEvent.SpawnReason.CUSTOM);
		getWorld().spawnEntity(loc.clone().add(0, 1, -3), EntityType.ENDER_CRYSTAL, CreatureSpawnEvent.SpawnReason.CUSTOM);
		db.initiateRespawn();

		new BukkitRunnable() {
			@Override
			public void run() {
				if (db.getRespawnPhase() != DragonBattle.RespawnPhase.NONE) return;

				new BukkitRunnable() {
					@Override
					public void run() {
						EnderDragon dragon = getDragonBattle().getEnderDragon();
						if (dragon != null) {
							resetDragon(true);
						}
					}
				}.runTaskLater(MysteryUniversePlugin.getInstance(), 40);

				Component outerLine = MysteriaUtils.centeredComponent(Component.text("---------------------------------------------",
						NamedColor.DOWNLOAD_PROGRESS).decorate(TextDecoration.STRIKETHROUGH).decorate(TextDecoration.BOLD));
				Component line1 = MysteriaUtils.centeredComponent(Component.text("ENDER DRAGON HAS BEEN SUMMONED AGAIN!",
						NamedColor.TURBO).decorate(TextDecoration.BOLD));
				Component line2;
				if (summoner != null) {
					line2 = MysteriaUtils.centeredComponent(
							Component.text()
									.append(Component.text("Summoned by ", NamedColor.SOARING_EAGLE))
									.append(Component.text(summoner.getName(), NamedColor.CARMINE_PINK))
									.build()
					);
				} else {
					line2 = null;
				}

				for (Player p : Bukkit.getOnlinePlayers()) {
					p.sendMessage(outerLine);
					p.sendMessage(Component.space());
					p.sendMessage(Component.space());
					p.sendMessage(line1);
					if (line2 != null) {
						p.sendMessage(line2);
					} else {
						p.sendMessage(Component.space());
					}
					p.sendMessage(Component.space());
					p.sendMessage(Component.space());
					p.sendMessage(outerLine);
					p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, SoundCategory.HOSTILE, 10, 1);
				}
				this.cancel();

			}
		}.runTaskTimer(MysteryUniversePlugin.getInstance(), 20, 20);

		return true;



	}

	public boolean resetDragon(boolean forceReset) {

		DragonBattle db = getDragonBattle();
		EnderDragon dragon = db.getEnderDragon();

		if (dragon == null) return false;

		if (db.getBossBar().getTitle().equals(bossBarTitle) &&
				(!forceReset && dragon.getLocation().getNearbyPlayers(300).size() != 0)) return false;

		AttributeInstance attributeMaxHealth = dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (attributeMaxHealth == null) return false;

		damages.clear();

		db.getBossBar().setTitle(bossBarTitle);

		attributeMaxHealth.setBaseValue(1000);
		dragon.setHealth(1000);

		for (PotionEffect potionEffect : dragon.getActivePotionEffects()) {
			dragon.removePotionEffect(potionEffect.getType());
		}
		dragon.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1726272000, 0, true, false));

		return true;

	}

	public int getPlayerChanceInt(@Nonnull Player p) {
		double damage_total = getTotalPlayerDamage(false);

		if (damage_total < 1) return 0;

		double damage_p = getPlayerDamage(p);

		return (((int) damage_p / (int) damage_total) * 100);
	}

	public boolean getPlayerChance(@Nonnull Player p, double multiplier) {
		int chance = (int) (getPlayerChanceInt(p) * multiplier);

		return (new Random().nextInt(100) < chance);
	}

	public HashMap<Player, Double> getTopDamagers() {

		// Create a list from elements of HashMap
		List<Map.Entry<Player, Double>> list = new LinkedList<>(damages.entrySet());

		// Sort the list
		list.sort(Map.Entry.comparingByValue());
		Collections.reverse(list);

		// put data from sorted list to hashmap
		HashMap<Player, Double> temp = new LinkedHashMap<>();
		for (Map.Entry<Player, Double> x : list) {
			temp.put(x.getKey(), x.getValue());
		}

		return temp;
	}


	public void spawnEnderman(@Nonnull Player p) {
		Enderman mob = (Enderman) p.getWorld().spawnEntity(p.getLocation(), EntityType.ENDERMAN, CreatureSpawnEvent.SpawnReason.CUSTOM);
		mob.customName(endermanName);
		mob.setHealth(15);
		mob.teleportRandomly();
		mob.setTarget(p);
	}


}
