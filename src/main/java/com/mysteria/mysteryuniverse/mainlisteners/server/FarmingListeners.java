package com.mysteria.mysteryuniverse.mainlisteners.server;

import com.google.common.collect.ImmutableSet;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.events.PlayerHarvestEvent;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FarmingListeners implements Listener {

	public FarmingListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	private final List<UUID> breakItems = new ArrayList<>();

	private final ImmutableSet<Material> CROPS = new ImmutableSet.Builder<Material>()
			.add(Material.CARROTS)
			.add(Material.POTATOES)
			.add(Material.BEETROOTS)
			.add(Material.MELON_STEM)
			.add(Material.ATTACHED_MELON_STEM)
			.add(Material.PUMPKIN_STEM)
			.add(Material.ATTACHED_PUMPKIN_STEM)
			.add(Material.WHEAT)
			.add(Material.GRASS)
			.add(Material.MELON)
			.add(Material.PUMPKIN)
			.add(Material.CARVED_PUMPKIN)
			.build();
	private final ImmutableSet<Material> DROPS = new ImmutableSet.Builder<Material>()
			.add(Material.CARROT)
			.add(Material.POTATO)
			.add(Material.BEETROOT)
			.add(Material.BEETROOT_SEEDS)
			.add(Material.MELON_SEEDS)
			.add(Material.PUMPKIN_SEEDS)
			.add(Material.WHEAT_SEEDS)
			.add(Material.WHEAT)
			.add(Material.MELON_SLICE)
			.add(Material.PUMPKIN)
			.add(Material.CARVED_PUMPKIN)
			.build();
	private final ImmutableSet<Material> DISABLE_PISTON = new ImmutableSet.Builder<Material>()
			.add(Material.MELON)
			.add(Material.PUMPKIN)
			.add(Material.CARVED_PUMPKIN)
			.add(Material.SUGAR_CANE)
			.build();
	private final ImmutableSet<Material> HOES = new ImmutableSet.Builder<Material>()
			.add(Material.WOODEN_HOE)
			.add(Material.STONE_HOE)
			.add(Material.IRON_HOE)
			.add(Material.GOLDEN_HOE)
			.add(Material.DIAMOND_HOE)
			.add(Material.NETHERITE_HOE)
			.build();


	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDropSpawn(ItemSpawnEvent e) {
		Item item = e.getEntity();

		if (!DROPS.contains(item.getItemStack().getType()) || item.getThrower() != null) return;
		if (e.getEntity().getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM || breakItems.contains(item.getUniqueId())) return;

		e.setCancelled(true);

	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onCropBreakWithHoe(BlockDropItemEvent e) {
		if (
				(!CROPS.contains(e.getBlockState().getType()) && e.getBlockState().getBlock() instanceof Container) ||
						e.getItems().isEmpty() ||
						!HOES.contains(e.getPlayer().getInventory().getItemInMainHand().getType())
		) return;

		PlayerHarvestEvent playerHarvestEvent = new PlayerHarvestEvent(e.getPlayer(), e.getPlayer().getInventory().getItemInMainHand(), e.getBlockState());
		Bukkit.getPluginManager().callEvent(playerHarvestEvent);

		if (playerHarvestEvent.isToolDamage()) {
			damageHoe(e.getPlayer());
		}

		for (Item item : e.getItems()) bypassDrop(item);

	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent e) {
		for (Block block : e.getBlocks()) {
			if (DISABLE_PISTON.contains(block.getType())) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent e) {

		if (!CROPS.contains(e.getNewState().getType())) return;

		boolean melon_pumpkin = e.getNewState().getType() == Material.MELON || e.getNewState().getType() == Material.PUMPKIN;

		int chance = 7;
		if (melon_pumpkin) chance += 5;

		if (MysteriaUtils.chance(chance)) {
			if (melon_pumpkin) {
				List<Block> blocks = new ArrayList<>();
				blocks.add(e.getBlock().getRelative(BlockFace.NORTH));
				blocks.add(e.getBlock().getRelative(BlockFace.EAST));
				blocks.add(e.getBlock().getRelative(BlockFace.WEST));
				blocks.add(e.getBlock().getRelative(BlockFace.SOUTH));
				for (Block block : blocks) {
					if (block.getType() != Material.MELON_STEM && block.getType() != Material.PUMPKIN_STEM) continue;
					block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
					block.setType(Material.AIR);
					break;
				}
			}
			e.setCancelled(true);
			e.getBlock().getWorld().playEffect(e.getBlock().getLocation(), Effect.STEP_SOUND, e.getNewState().getType());
			e.getBlock().setType(Material.AIR);
		}

	}

	private void bypassDrop(@Nonnull Item item) {
		breakItems.add(item.getUniqueId());
		new BukkitRunnable() {
			@Override
			public void run() {
				breakItems.remove(item.getUniqueId());
			}
		}.runTaskLater(MysteryUniversePlugin.getInstance(), 5);
	}

	private void damageHoe(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		PlayerItemDamageEvent event = new PlayerItemDamageEvent(p, tool, 4);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		Damageable meta = (Damageable) tool.getItemMeta();
		meta.setDamage(meta.getDamage() + event.getDamage());
		if (meta.getDamage() >= tool.getType().getMaxDurability()) {
			p.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
			p.getInventory().setItemInMainHand(null);
		} else {
			tool.setItemMeta((ItemMeta) meta);
		}
	}


}
