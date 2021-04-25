package com.mysteria.mysteryuniverse.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.database.Database;
import com.mysteria.mysteryuniverse.database.enums.Column;
import com.mysteria.utils.MysteriaUtils;
import org.bukkit.entity.Player;

@CommandAlias("gift|startinggift")
public class StartingGiftCommand extends BaseCommand {

	@Default
	@Description("Starting gift")
	public void onCommand(Player p) {
		Database database = MysteryUniversePlugin.getDatabase();
		Boolean isSelected = database.getBoolean(p.getUniqueId(), Column.STARTING_GIFT);

		if (isSelected == null) {
			return;
		}

		if (isSelected && !p.hasPermission("mysteryuniverse.bypass")) {
			MysteriaUtils.sendMessageRed(p, "You have already selected your starting gift.");
			return;
		}
		MysteryUniversePlugin.getStartingGiftManager().StartingGiftGUI(p);
	}

}
