package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.google.common.collect.ImmutableSet;
import com.mysteria.customapi.itemmanager.ItemInfo;
import com.mysteria.customapi.itemmanager.containers.ItemTagContainer;
import com.mysteria.customapi.items.CustomItem;
import com.mysteria.customapi.items.CustomItemUseReason;
import com.mysteria.customapi.items.events.CustomItemUseEvent;
import com.mysteria.customapi.itemtags.ItemTag;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlayerDeathKeepListener implements Listener {

	public PlayerDeathKeepListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	private final ImmutableSet<Material> ACCEPTABLE = new ImmutableSet.Builder<Material>()
			.add(Material.AIR)
			.add(Material.TALL_GRASS)
			.add(Material.WATER)
			.add(Material.LAVA)
			.add(Material.FERN)
			.add(Material.LARGE_FERN)
			.add(Material.DEAD_BUSH)
			.add(Material.GRASS)
			.build();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent e) {

		e.setShouldDropExperience(false);
		e.setKeepLevel(true);

		// Checks keep on death items
		for (Iterator<ItemStack> iterator = e.getDrops().iterator(); iterator.hasNext(); ) {
			ItemStack drop = iterator.next();

			ItemTagContainer itemTagContainer = ItemInfo.get(drop).getItemTagContainer();
			if (itemTagContainer.hasTag(ItemTag.KEEP_ON_DEATH)) {
				iterator.remove();
				e.getItemsToKeep().add(drop);
			}

		}

		ItemStack archangels_forgiveness = CustomItem.ARCHANGELS_FORGIVENESS.getItemStack();
		ItemStack ring_of_sacrifice = CustomItem.RING_OF_SACRIFICE.getItemStack();
		Player p = e.getEntity();

		if (p.getInventory().containsAtLeast(archangels_forgiveness, 1)) {

			CustomItemUseEvent event = new CustomItemUseEvent(p, CustomItem.ARCHANGELS_FORGIVENESS, new ItemStack(archangels_forgiveness), CustomItemUseReason.PLAYER_PASSIVE);
			event.setUsed(true);
			Bukkit.getPluginManager().callEvent(event);

			p.getInventory().removeItem(archangels_forgiveness);
			e.getDrops().clear();
			e.setKeepInventory(true);

			MysteriaUtils.sendMessage(p, Component.text()
					.append(Component.text("You have been forgiven by Archangel! ", NamedColor.SKIRRET_GREEN))
					.append(MysteriaUtils.showItemComponent(archangels_forgiveness))
					.append(Component.text(" successfully used and you didn't lose any items.", NamedColor.SKIRRET_GREEN))
					.build());

		} else if (p.getInventory().containsAtLeast(ring_of_sacrifice, 1)) {

			List<ItemStack> drops = new ArrayList<>(e.getDrops());

			for (Iterator<ItemStack> iterator = drops.iterator(); iterator.hasNext(); ) {
				ItemStack drop = iterator.next();
				if (drop.isSimilar(ring_of_sacrifice)) {
					int amount = drop.getAmount() - 1;
					if (amount <= 0) iterator.remove();
					else drop.setAmount(amount);
					break;
				}
			}

			Location loc = p.getLocation().clone();
			Block block1 = loc.getBlock();
			Block block2 = null;

			boolean success = true;

			if (drops.size() > 27) {
				block2 = loc.add(1, 0, 0).getBlock();
			}

			if (!ACCEPTABLE.contains(block1.getType()) || (block2 != null && !ACCEPTABLE.contains(block2.getType())) ) {
				success = false;
				loop:
				for (int x = -3; x <= 3; x++) {
					for (int y = 0; y <= 2; y++) {
						for (int z = -3; z <= 3; z++) {
							if (ACCEPTABLE.contains(block1.getRelative(x, y, z).getType())) {
								if (block2 != null) {
									if (ACCEPTABLE.contains(block1.getRelative(x + 1, y, z).getType())) {
										block2 = block1.getRelative(x + 1, y, z);
										block1 = block1.getRelative(x, y, z);
										success = true;
										break loop;
									}
								} else {
									block1 = block1.getRelative(x, y, z);
									success = true;
									break loop;
								}
							}
						}
					}
				}
			}

			if (!success) {
				MysteriaUtils.sendMessage(p, Component.text()
						.append(Component.text("There was no space for the death chest! ", NamedColor.CARMINE_PINK))
						.append(MysteriaUtils.showItemComponent(ring_of_sacrifice))
						.append(Component.text(" could not be used.", NamedColor.CARMINE_PINK))
						.build());
				return;
			}

			block1.setType(Material.CHEST);
			Chest chest1 = (Chest) block1.getState();
			Inventory inv;

			RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
			com.sk89q.worldedit.util.Location blockLoc = BukkitAdapter.adapt(block1.getLocation());
			com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(block1.getWorld());
			boolean result = true;
			if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(WorldGuardPlugin.inst().wrapPlayer(p), world)) {
				result = query.testState(blockLoc, WorldGuardPlugin.inst().wrapPlayer(p), Flags.BUILD);
			}

			if (block2 != null) {
				blockLoc = BukkitAdapter.adapt(block2.getLocation());
				if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(WorldGuardPlugin.inst().wrapPlayer(p), world)) {
					result = query.testState(blockLoc, WorldGuardPlugin.inst().wrapPlayer(p), Flags.BUILD);
				}
			}
			if (!result) {
				MysteriaUtils.sendMessage(p, Component.text()
						.append(Component.text("You don't have permission to build at this location. ",
								NamedColor.HARLEY_DAVIDSON_ORANGE))
						.append(MysteriaUtils.showItemComponent(ring_of_sacrifice))
						.append(Component.text(" could not be used.", NamedColor.HARLEY_DAVIDSON_ORANGE))
						.build());
				return;
			}

			if (block2 != null) {

				block2.setType(Material.CHEST);
				Chest chest2 = (Chest) block2.getState();

				org.bukkit.block.data.type.Chest chestData1 = (org.bukkit.block.data.type.Chest) chest1.getBlockData();
				org.bukkit.block.data.type.Chest chestData2 = (org.bukkit.block.data.type.Chest) chest2.getBlockData();

				chestData1.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
				block1.setBlockData(chestData1, true);
				chestData2.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
				block2.setBlockData(chestData2, true);

				Chest chest = (Chest) block1.getState();
				DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
				if (doubleChest != null) {
					inv = doubleChest.getInventory();
				} else {
					inv = chest1.getInventory();
				}
			} else {
				inv = chest1.getInventory();
			}

			new BukkitRunnable() {
				@Override
				public void run() {

					inv.setContents(drops.toArray(new ItemStack[0]));

				}
			}.runTaskLaterAsynchronously(MysteryUniversePlugin.getInstance(), 10);

			CustomItemUseEvent event = new CustomItemUseEvent(p, CustomItem.RING_OF_SACRIFICE, new ItemStack(ring_of_sacrifice), CustomItemUseReason.PLAYER_PASSIVE);
			event.setUsed(true);
			Bukkit.getPluginManager().callEvent(event);

			MysteriaUtils.sendMessage(p, Component.text()
					.append(MysteriaUtils.showItemComponent(ring_of_sacrifice))
					.append(Component.text(" successfully used! Your death chest has been placed.", NamedColor.SKIRRET_GREEN))
					.build());

			e.getDrops().clear();

		}



	}


}
