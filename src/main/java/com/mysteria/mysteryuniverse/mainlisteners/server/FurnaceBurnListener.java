package com.mysteria.mysteryuniverse.mainlisteners.server;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.TileEntityFurnace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.block.data.Directional;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;

public class FurnaceBurnListener implements Listener {

	public FurnaceBurnListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onFurnaceUnLit(BlockPhysicsEvent e) {

		if (e.getBlock().getState() instanceof Furnace) {
			Furnace furnace = (Furnace) e.getBlock().getState();

			if (furnace.getBlockData() instanceof org.bukkit.block.data.type.Furnace) {
				org.bukkit.block.data.type.Furnace furnaceData = (org.bukkit.block.data.type.Furnace) furnace.getBlockData();

				if (!furnaceData.isLit()) {
					FurnaceInventory inv = furnace.getInventory();

					ItemStack fuel = inv.getFuel();
					if (fuel != null && fuel.getType() != Material.AIR) {
						short fuelTime = Short.parseShort(String.valueOf(getFuelTime(fuel, e.getBlock().getType())));
						if (fuelTime != 0) {
							if (fuel.getType() == Material.LAVA_BUCKET) {
								fuel.setType(Material.BUCKET);
							} else {
								fuel.setAmount(fuel.getAmount() - 1);
							}
							furnace.setBurnTime(fuelTime);
							furnace.update();
							inv.setFuel(fuel);
						}
					}
				}

			}

		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onFurnaceFuelBurn(FurnaceBurnEvent e) {

		Block block = e.getBlock();
		Furnace blockState = (Furnace) block.getState();

		if (!((org.bukkit.block.data.type.Furnace) blockState.getBlockData()).isLit()) {
			if (CustomItem.checkCustomItem(e.getFuel(), CustomItem.FLINT_AND_COAL)) {
				return;
			}
			e.setCancelled(true);
			ItemStack fuel = e.getFuel();
			blockState.getInventory().setFuel(null);
			Directional directional = (Directional) block.getBlockData();
			blockState.getWorld().dropItem(block.getRelative(directional.getFacing()).getLocation().add(0.5, 0.3, 0.5), fuel).setVelocity(new Vector());
			blockState.getWorld().playSound(blockState.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 2);

			for (HumanEntity humanEntity : blockState.getInventory().getViewers()) {
				MysteriaUtils.sendMessageRed(humanEntity, "You have to lit the furnace before using.");
				Component component = Component.text("Put a ", NamedColor.CARMINE_PINK)
						.append(MysteriaUtils.showItemComponent(CustomItem.FLINT_AND_COAL.getItemStack()))
						.append(Component.text(" to fuel slot, like a normal fuel.", NamedColor.CARMINE_PINK));
				MysteriaUtils.sendMessage(humanEntity, component);
			}
			return;
		}

		int fuelTime = getFuelTime(e.getFuel(), block.getType());
		e.setBurnTime(fuelTime);

	}


	private int getFuelTime(@Nonnull ItemStack itemStack, Material furnaceType) {
		CustomItem customItem = CustomItem.getCustomItem(itemStack);
		int fuelTime;

		if (customItem == null) {
			if (itemStack.getType() == Material.LAVA_BUCKET) {
				fuelTime = getFuelTime(new ItemStack(Material.COAL), furnaceType) * 3;
			} else {
				Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
				fuelTime = TileEntityFurnace.f().getOrDefault(item, 0);
			}
		} else {
			fuelTime = switch (customItem) {
				case RADIOACTIVE_COAL -> getFuelTime(new ItemStack(Material.COAL), furnaceType) * 4;
				case FLINT_AND_COAL -> getFuelTime(new ItemStack(Material.COAL), furnaceType) * 2;
				default -> getFuelTime(new ItemStack(Material.COAL), furnaceType);
			};
		}
		return furnaceType == Material.FURNACE ? fuelTime : (fuelTime / 2);
	}


}
