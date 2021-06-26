package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.customapi.items.CustomItemType;
import com.mysteria.customapi.items.CustomItemUseReason;
import com.mysteria.customapi.items.events.CustomItemUseEvent;
import com.mysteria.customapi.sounds.CustomSound;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.systems.theend.EndManager;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import tr.badactive.casemanager.case_utils.Case;
import tr.badactive.casemanager.case_utils.CaseItem;
import tr.badactive.casemanager.libraries.gui.CGUI;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomItemUseListener implements Listener {

	private final Map<UUID, Long> COOLDOWNS = new HashMap<>();
	private final Map<String, Case> CASES = new HashMap<>();

	public CustomItemUseListener() {
		Case crate = new Case("Present");
		putItem(crate, CustomItem.FLINT_AND_COAL.getItemStack(), 60);
		putItem(crate, new ItemStack(Material.WOODEN_SWORD), 60);
		putItem(crate, new ItemStack(Material.WOODEN_PICKAXE), 60);
		putItem(crate, new ItemStack(Material.WOODEN_AXE), 60);
		putItem(crate, new ItemStack(Material.WOODEN_SHOVEL), 60);
		putItem(crate, new ItemStack(Material.WOODEN_HOE), 60);
		putItem(crate, new ItemStack(Material.STONE_SWORD), 60);
		putItem(crate, new ItemStack(Material.STONE_PICKAXE), 60);
		putItem(crate, new ItemStack(Material.STONE_AXE), 60);
		putItem(crate, new ItemStack(Material.STONE_SHOVEL), 60);
		putItem(crate, new ItemStack(Material.STONE_HOE), 60);
		putItem(crate, new ItemStack(Material.COAL, 3), 60);
		putItem(crate, CustomItem.BLOODSTONE.getItemStack(), 60);
		putItem(crate, CustomItem.BLOOD_BOTTLE.getItemStack(), 60);
		putItem(crate, new ItemStack(Material.IRON_INGOT, 2), 17);
		putItem(crate, new ItemStack(Material.GOLD_INGOT, 2), 17);
		putItem(crate, CustomItem.RADIOACTIVE_COAL.getItemStack(), 17);
		putItem(crate, CustomItem.FAIRY_DUST.getItemStack(2), 17);
		putItem(crate, CustomItem.SOULSTONE.getItemStack(), 17);
		putItem(crate, CustomItem.WOLF_TOOTH.getItemStack(), 17);
		putItem(crate, CustomItem.VOODOO_DOLL.getItemStack(), 17);
		putItem(crate, CustomItem.TOME.getItemStack(), 6);
		putItem(crate, CustomItem.RING_OF_SACRIFICE.getItemStack(), 6);
		putItem(crate, CustomItem.BLESSING_SCROLL.getItemStack(), 6);
		putItem(crate, CustomItem.BOOK_OF_CONSCIOUSNESS.getItemStack(), 6);
		putItem(crate, CustomItem.ARCHANGELS_FORGIVENESS.getItemStack(), 6);
		CASES.put(crate.getName(), crate);


		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCustomItemUse(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack item = e.getItem();
		CustomItem customItem = CustomItem.getCustomItem(item);
		if (customItem == null) return;

		Player p = e.getPlayer();

		if (!MysteriaUtils.checkCooldown(COOLDOWNS.getOrDefault(p.getUniqueId(), 0L))) {
			MysteriaUtils.sendMessage(p, Component.text()
					.append(Component.text("You must wait "))
					.append(Component.text("5 seconds ", NamedColor.TURBO))
					.append(Component.text("between item usages."))
					.colorIfAbsent(NamedColor.CARMINE_PINK)
					.build());
			return;
		}

		if (customItem.getType() == CustomItemType.ACTIVE) {
			CustomItemUseEvent event = new CustomItemUseEvent(p, customItem, item, CustomItemUseReason.PLAYER_RIGHT_CLICK);
			Bukkit.getPluginManager().callEvent(event);

			setCooldown(p);
		}

	}

	private void setCooldown(@Nonnull Player p) {
		COOLDOWNS.put(p.getUniqueId(), MysteriaUtils.createCooldown(5));
	}


	@EventHandler(ignoreCancelled = true)
	private void onCustomItemUse(CustomItemUseEvent e) {
		Player p = e.getPlayer();

		switch (e.getCustomItem()) {
			case BLOODSTONE:

				AttributeInstance att = p.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				if (att == null) break;
				double maxHealth = att.getBaseValue();

				if (maxHealth + 2 > 24) {
					MysteriaUtils.sendMessageRed(p, "You already have 12 hearts.");
					break;
				}

				if (p.getHealth() < (maxHealth / 2)) {
					MysteriaUtils.sendMessageRed(p, "Your health must be higher than half.");
					break;
				}

				att.setBaseValue(maxHealth + 2);
				CustomSound.play(p.getLocation(), CustomSound.ITEM_BLOODSTONE, 1, 1);
				p.setHealth(1);
				e.getItem().setAmount(e.getItem().getAmount() - 1);
				e.setUsed(true);

				break;
			case HEART_OF_VOID:
				EndManager endManager = MysteryUniversePlugin.getEndManager();
				if (e.getPlayer().getWorld() == endManager.getWorld()) {
					if (endManager.respawnDragon(e.getPlayer())) {
						e.getItem().setAmount(e.getItem().getAmount() - 1);
						e.setUsed(true);
					}
				}
				break;
			case FORBIDDEN_BOOK:
			case BOOK_OF_REGRESSION:
				break;
			case PRESENT:
				if (openCase(e.getPlayer(), "Present")) {
					e.getItem().setAmount(e.getItem().getAmount() - 1);
					e.setUsed(true);
				}
				break;
		}

	}

	private void putItem(@Nonnull Case crate, @Nonnull ItemStack itemStack) {
		putItem(crate, itemStack, 1);
	}

	private void putItem(@Nonnull Case crate, @Nonnull ItemStack itemStack, int weight) {
		crate.addItem(new CaseItem(itemStack, weight));
	}

	private boolean openCase(@Nonnull Player p, @Nonnull String name) {
		Case crate = CASES.getOrDefault(name, null);
		if (crate != null) {
			CGUI cgui = new CGUI(crate.getName(), crate, p);
			return cgui.show();
		} else {
			return false;
		}
	}

}
