package com.mysteria.mysteryuniverse.mainlisteners.entity;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

public class BoatListeners implements Listener {

	public BoatListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler
	private void onBoatMove(VehicleMoveEvent e) {
		Vehicle vehicle = e.getVehicle();
		if (vehicle instanceof Boat) {
			if (vehicle.isOnGround()) {
				Material material = ((Boat) vehicle).getBoatMaterial();
				vehicle.remove();
				vehicle.getWorld().dropItem(vehicle.getLocation(), new ItemStack(Material.STICK, 2));
				vehicle.getWorld().dropItem(vehicle.getLocation(), new ItemStack(material, 3));
			}
		}
	}

}
