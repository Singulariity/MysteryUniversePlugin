package com.mysteria.mysteryuniverse.mainlisteners.block;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.ImmutableSet;
import com.mysteria.customapi.items.CustomItem;
import com.mysteria.customapi.sounds.CustomSound;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class BlockDropItem implements Listener {

	public BlockDropItem() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	private final ImmutableSet<Material> LOGS = new ImmutableSet.Builder<Material>()
			.add(Material.ACACIA_LOG)
			.add(Material.BIRCH_LOG)
			.add(Material.DARK_OAK_LOG)
			.add(Material.JUNGLE_LOG)
			.add(Material.OAK_LOG)
			.add(Material.SPRUCE_LOG)
			.add(Material.WARPED_STEM)
			.add(Material.CRIMSON_STEM)
			.add(Material.STRIPPED_ACACIA_LOG)
			.add(Material.STRIPPED_BIRCH_LOG)
			.add(Material.STRIPPED_DARK_OAK_LOG)
			.add(Material.STRIPPED_JUNGLE_LOG)
			.add(Material.STRIPPED_OAK_LOG)
			.add(Material.STRIPPED_SPRUCE_LOG)
			.add(Material.STRIPPED_WARPED_STEM)
			.add(Material.STRIPPED_CRIMSON_STEM)
			.build();

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockDropItem(BlockDropItemEvent e) {

		Player p = e.getPlayer();
		BlockState b = e.getBlockState();
		ItemStack tool = p.getInventory().getItemInMainHand();

		if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

		if (LOGS.contains(b.getType())) {
			if (e.getItems().isEmpty()) return;

			if (tool.getType() != Material.AIR && (MaterialTags.AXES.isTagged(tool) || CustomItem.checkCustomItem(tool, CustomItem.HATCHET))) return;

			e.getItems().clear();
			drop(b, new ItemStack(Material.STICK, MysteriaUtils.getRandom(2, 3)));
			return;

		}

		switch (b.getType()) {
//			case CAMPFIRE -> {
//				if (e.getItems().isEmpty()) return;
//				e.getItems().clear();
//
//
//
//				drop(b, new ItemStack(Material.STICK, MysteriaUtils.getRandom(1, 2)));
//				drop(b, new ItemStack(Material.OAK_PLANKS, MysteriaUtils.getRandom(2, 4)));
//			}
//			case CRAFTING_TABLE -> {
//				if (e.getItems().isEmpty()) return;
//				e.getItems().clear();
//				drop(b, new ItemStack(Material.OAK_PLANKS, MysteriaUtils.getRandom(2, 3)));
//			}
			case GRAVEL -> {
				if (e.getItems().isEmpty()) return;
				e.getItems().clear();

				if (p.isSneaking()) {
					drop(b, Material.GRAVEL);
					return;
				}

				int num = MysteriaUtils.getRandom(1, 1000);

				// Adding chance with shovel breaks
				int modifier = switch (tool.getType()) {
					case WOODEN_SHOVEL -> 10;
					case STONE_SHOVEL -> 20;
					case GOLDEN_SHOVEL -> 40;
					case DIAMOND_SHOVEL -> 30;
					case NETHERITE_SHOVEL -> 50;
					default -> 0;
				};

				if (num <= (100 + modifier)) {
					drop(b, Material.FLINT);
				}
			}
			case COAL_ORE,
					DEEPSLATE_COAL_ORE -> {
				if (e.getItems().isEmpty()) return;

				if (MysteriaUtils.chance(1)) {
					e.getItems().clear();
					drop(b, CustomItem.RADIOACTIVE_COAL.getItemStack());
				}
			}
//			case COPPER_ORE,
//					DEEPSLATE_COPPER_ORE -> {
//				if (e.getItems().isEmpty()) return;
//				e.getItems().clear();
//				drop(b, Material.RAW_COPPER);
//			}
//			case LAPIS_ORE,
//					DEEPSLATE_LAPIS_ORE,
//					REDSTONE_ORE,
//					DEEPSLATE_REDSTONE_ORE,
//					NETHER_GOLD_ORE,
//					DIAMOND_ORE,
//					DEEPSLATE_DIAMOND_ORE -> {
//				if (e.getItems().isEmpty()) return;
//				e.getItems().clear();
//				drop(b, e.getBlockState().getType());
//			}
			case IRON_ORE,
					DEEPSLATE_IRON_ORE -> {
				if (e.getItems().isEmpty()) return;
				if (tool.getType() == Material.STONE_PICKAXE && !CustomItem.checkCustomItem(tool, CustomItem.COPPER_PICKAXE)) {
					e.getItems().clear();
				}
			}
			case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> {
				e.getItems().clear();
				if (tool.getType() != Material.GOLDEN_PICKAXE) return;
				drop(b, e.getBlockState().getType());
				CustomSound.play(e.getBlock().getLocation(), CustomSound.GALAXY_STARDUST_ORE_BREAK, 1, 1);
			}
			case OBSIDIAN -> {
				if (e.getItems().isEmpty()) return;
				e.getItems().clear();
				ItemStack obsidian_shards = CustomItem.OBSIDIAN_SHARD.getItemStack(MysteriaUtils.getRandom(1, 2));
				drop(b, obsidian_shards);
			}
			case EMERALD_BLOCK -> {
				e.getItems().clear();
				if (tool.getType() != Material.NETHERITE_PICKAXE) return;
				drop(b, e.getBlockState().getType());
			}
		}

	}


	private void drop(@Nonnull BlockState b, @Nonnull ItemStack itemStack) {
		b.getWorld().dropItemNaturally(b.getLocation(), itemStack);
	}

	private void drop(@Nonnull BlockState b, @Nonnull Material material) {
		b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(material));
	}

}
