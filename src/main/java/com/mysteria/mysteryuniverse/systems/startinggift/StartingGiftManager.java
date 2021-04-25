package com.mysteria.mysteryuniverse.systems.startinggift;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.customapi.itemtags.ItemTag;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.ItemBuilder;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class StartingGiftManager {

	private final Component name = Component.text("Select Your Starting Gift", NamedTextColor.DARK_GRAY);
	private final int size = 36;

	public StartingGiftManager() {
		if (MysteryUniversePlugin.getStartingGiftManager() != null) {
			throw new IllegalStateException();
		}
		new StartingGiftListeners();
	}

	public void StartingGiftGUI(Player p) {

		Component name = getGUIName();
		Inventory inv = Bukkit.createInventory(null, size, name);

		ItemStack empty = ItemBuilder.builder(CustomItem.EMPTY.getItemStack())
				.adaptGUI()
				.build();

		ItemStack barrier = ItemBuilder.builder(Material.BARRIER)
				.name(Component.text("NONE", NamedColor.HARLEY_DAVIDSON_ORANGE).decoration(TextDecoration.BOLD, true))
				.lore(
						Component.text(" "),
						Component.text("Click to refuse", NamedTextColor.GRAY),
						Component.text("starting gift", NamedTextColor.GRAY))
				.adaptGUI()
				.build();

		ItemStack bloodstone = ItemBuilder.builder(CustomItem.BLOODSTONE.getItemStack())
				.amount(2)
				.addTags(ItemTag.KEEP_ON_DEATH)
				.build();

		inv.setItem(10, CustomItem.ARCHANGELS_FORGIVENESS.getItemStack());
		inv.setItem(11, CustomItem.FLINT_AND_COAL.getItemStack(2));
		inv.setItem(12, bloodstone);
		inv.setItem(13, new ItemStack(Material.STONE_SWORD));
		inv.setItem(14, CustomItem.HATCHET.getItemStack());
		inv.setItem(15, new ItemStack(Material.STONE_HOE));
		inv.setItem(16, new ItemStack(Material.COOKED_BEEF, 4));
		inv.setItem(31, barrier);

		for (int i = 0; i < size; i++) {
			if (inv.getItem(i) == null) inv.setItem(i, empty);
		}

		p.openInventory(inv);

	}

	public Component getGUIName() {
		return name;
	}

	public int getSize() {
		return size;
	}
}
