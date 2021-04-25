package com.mysteria.mysteryuniverse.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.command.CommandSender;

@CommandAlias("mreload")
public class ReloadCommand extends BaseCommand {

	@Default
	@CommandPermission("mysteryuniverse.reload")
	@Description("Reloads the plugin.")
	public void onCommand(CommandSender sender) {
		MysteryUniversePlugin.getInstance().reloadConfig();
		MysteriaUtils.sendMessageGreen(sender, "Plugin reloaded.");
	}

}
