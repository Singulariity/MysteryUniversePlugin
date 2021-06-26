package com.mysteria.mysteryuniverse.systems.meteorite;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Random;

public class MeteoriteManager {

	private final NamespacedKey nonStackableKey;

	public MeteoriteManager() {
		if (MysteryUniversePlugin.getMeteoriteManager() != null) {
			throw new IllegalStateException();
		}
		new MeteoriteListeners();
		nonStackableKey = new NamespacedKey(MysteryUniversePlugin.getInstance(), "Non-stackable");

		new BukkitRunnable() {
			@Override
			public void run() {

				FileConfiguration config = MysteryUniversePlugin.getInstance().getConfig();

				if (!config.getBoolean("meteorite.enabled")) return;

				if (config.getBoolean("meteorite.min_players_enabled") && Bukkit.getOnlinePlayers().size() < config.getInt("meteorite.min_players")) return;

				ZoneId id = ZoneId.of("Europe/Istanbul");
				ZonedDateTime dt = ZonedDateTime.now(id);
				int minute = dt.getMinute();

				if (minute == 0) {
					spawnNaturalMeteorite(20 * 610, false);
				}

			}
		}.runTaskTimer(MysteryUniversePlugin.getInstance(), 0, 1200);

	}

	public HashMap<ItemStack, Integer> getDefaultLoots() {
		HashMap<ItemStack, Integer> loots = new HashMap<>();

		loots.put(CustomItem.METEORITE_FRAGMENT.getItemStack(), MysteriaUtils.getRandom(12, 20));

		loots.put(new ItemStack(Material.BREAD), 4);
		loots.put(new ItemStack(Material.GOLDEN_APPLE), 2);
		loots.put(new ItemStack(Material.EXPERIENCE_BOTTLE), 3);
		loots.put(new ItemStack(Material.EMERALD), 3);
		loots.put(new ItemStack(Material.WHEAT), 13);

		loots.put(CustomItem.DIAMOND_SHARD.getItemStack(3), MysteriaUtils.getRandom(8, 13));

		loots.put(CustomItem.OBSIDIAN_SHARD.getItemStack(3), MysteriaUtils.getRandom(10, 13));

		loots.put(new ItemStack(Material.CHARCOAL), MysteriaUtils.getRandom(8, 13));

		loots.put(new ItemStack(Material.IRON_INGOT), MysteriaUtils.getRandom(3, 6));

		loots.put(new ItemStack(Material.GOLD_INGOT), MysteriaUtils.getRandom(2, 4));

		loots.put(new ItemStack(Material.GUNPOWDER), MysteriaUtils.getRandom(15, 30));

		return loots;
	}

	public void spawnNaturalMeteorite(int timerInSeconds, boolean hidden) {
		World world = getConfigWorld();
		if (world == null) {
			MysteryUniversePlugin.getInstance().getLogger().warning("Meteorite world not found!");
		} else {

			FileConfiguration config = MysteryUniversePlugin.getInstance().getConfig();
			Location loc = new Location(world, 0, 256 , 0);

			int max_X = (int) (loc.getX()) + config.getInt("meteorite.max.x");
			int min_X = (int) (loc.getX()) + config.getInt("meteorite.min.x");

			int max_Z = (int) (loc.getX()) + config.getInt("meteorite.max.z");
			int min_Z = (int) (loc.getX()) + config.getInt("meteorite.min.z");

			loc.setX(new Random().nextInt(max_X - min_X) + min_X);
			loc.setZ(new Random().nextInt(max_Z - min_Z) + min_Z);

			new Meteorite(getLandLocation(loc), timerInSeconds, hidden);
		}
	}

	@Nullable
	private World getConfigWorld() {
		String worldStr = MysteryUniversePlugin.getInstance().getConfig().getString("meteorite.world");
		World world;
		if (worldStr == null) {
			world = null;
		} else {
			world = Bukkit.getWorld(worldStr);
		}
		return world;
	}

	@Nonnull
	private Location getLandLocation(@Nonnull Location location) {
		Location l = location.clone();

		for (int i = 0; i < 256; i++) {
			l.setY(l.getY() - 1);

			if (checkSolidBlock(l)) {
				break;
			}
		}
		return l;
	}


	public boolean checkSolidBlock(@Nonnull Location location) {
		Block block = location.getBlock();

		return !Tag.LEAVES.isTagged(block.getType()) && !block.isPassable();
	}

	@Nonnull
	public NamespacedKey getNonStackableKey() {
		return nonStackableKey;
	}
}
