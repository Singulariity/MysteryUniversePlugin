package com.mysteria.mysteryuniverse.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("recipe|showrecipe")
public class RecipeCommand extends BaseCommand {

	@Default
	@CommandCompletion("@item @nothing")
	@Syntax("<item name>")
	@Description("Shows the recipe.")
	public void onCommand(Player p, String[] args) {

		if (args.length != 1) {
			MysteriaUtils.sendMessage(p, Component.text()
					.append(Component.text("Usage:", NamedColor.TURBO))
					.append(Component.text(" /recipe <item name>", NamedColor.SOARING_EAGLE))
					.build());
			return;
		}

		ItemStack itemStack;
		try {
			CustomItem customItem = CustomItem.valueOf(args[0].toUpperCase());
			itemStack = customItem.getItemStack();
			if (MysteryUniversePlugin.getRecipeManager().RecipeGUI(p, itemStack)) {
				return;
			}
		} catch (IllegalArgumentException ignored) {
			itemStack = null;
		}

		if (itemStack == null) {
			Material material = Material.getMaterial(args[0].toUpperCase());
			if (material != null) {
				itemStack = new ItemStack(material);
			}
		}

		if (itemStack != null) {
			if (MysteryUniversePlugin.getRecipeManager().RecipeGUI(p, itemStack)) {
				return;
			}
		}

		MysteriaUtils.sendMessageRed(p, "No recipe found for this item.");
	}

}
