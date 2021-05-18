package com.mysteria.mysteryuniverse.mainlisteners.entity;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class EntityDeathDropsListener implements Listener {

	public EntityDeathDropsListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent e) {

		LivingEntity victim = e.getEntity();

		if (victim.getKiller() != null) {
			if (victim.getWorld().getEnvironment() == World.Environment.NORMAL) {
				if (MysteriaUtils.chance(3)) {
					e.getDrops().add(CustomItem.FAIRY_DUST.getItemStack());
				}
			}
			switch (victim.getType()) {
				case SKELETON:
					if (checkBiome(victim.getLocation(), Biome.SOUL_SAND_VALLEY)) {
						if (MysteriaUtils.chance(35)) {
							victim.getWorld().playSound(victim.getLocation(), Sound.PARTICLE_SOUL_ESCAPE, 1, 1);
							e.getDrops().add(CustomItem.SPIRIT_ESSENCE.getItemStack());
						}
					}
					break;
				case DROWNED:
					EntityEquipment equipment = e.getEntity().getEquipment();
					if (equipment != null) {
						if (equipment.getItemInMainHand().getType() == Material.TRIDENT) {
							if (MysteriaUtils.chance(1)) {
								e.getDrops().add(CustomItem.BROKEN_TRIDENT.getItemStack());
							}
						}
					}

					break;

				case ZOMBIE:
					if (MysteriaUtils.chance(2)) {
						e.getDrops().add(CustomItem.BLOODSTONE.getItemStack());
					}
					break;

				case BLAZE:
					if (MysteriaUtils.chance(50)) {
						e.getDrops().add(CustomItem.BLAZE_FRAGMENT.getItemStack());
					}
					break;

				case WOLF:
					if (MysteriaUtils.chance(5)) {
						e.getDrops().add(CustomItem.WOLF_TOOTH.getItemStack());
					}
					break;

				case BAT:
					if (MysteriaUtils.chance(50)) {
						e.getDrops().add(CustomItem.BAT_WING.getItemStack());
					}
					break;

				case WITCH:
					e.getDrops().add(CustomItem.BLOODSTONE.getItemStack());
					if (MysteriaUtils.chance(1)) {
						e.getDrops().add(CustomItem.VOODOO_DOLL.getItemStack());
					}
					break;

				case PHANTOM:
					if (MysteriaUtils.chance(10)) {
						e.getDrops().add(CustomItem.DAYDREAM_FEATHER.getItemStack());
					}
					break;

				case ENDERMAN:
					if (e.getEntity().getWorld().getEnvironment() == World.Environment.THE_END) {
						Component endermanName = MysteryUniversePlugin.getEndManager().getEndermanName();
						Component entityName = e.getEntity().customName();
						if (entityName != null && entityName.equals(endermanName)) break;

						if (MysteriaUtils.chance(5)) {
							e.getDrops().add(CustomItem.VOID_TENDRIL.getItemStack());
						}
					}
					break;

				case PILLAGER:
					EntityEquipment pillagerEquipment = e.getEntity().getEquipment();
					if (pillagerEquipment != null) {
						if (pillagerEquipment.getItemInMainHand().getType() == Material.CROSSBOW) {
							if (MysteriaUtils.chance(4)) {
								e.getDrops().add(new ItemStack(Material.CROSSBOW));
							}
						}
					}
					break;

			}
		}

	}

	public boolean checkBiome(@Nonnull Location loc, @Nonnull Biome biome) {
		return loc.getBlock().getBiome() == biome;
	}

}
