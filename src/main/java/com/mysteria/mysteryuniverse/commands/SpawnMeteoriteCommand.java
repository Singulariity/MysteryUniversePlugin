package com.mysteria.mysteryuniverse.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.systems.meteorite.Meteorite;
import org.bukkit.entity.Player;

@CommandAlias("spawnmeteorite|meteorite")
public class SpawnMeteoriteCommand extends BaseCommand {

	@Default
	@CommandPermission("mysteryuniverse.spawnmeteorite")
	@CommandCompletion("@meteorite1st @meteorite2nd @meteorite3rd @nothing")
	@Syntax("<timer> <ishidden> <random/here>")
	@Description("Spawns a meteorite")
	public void onCommand(Player p, String[] args) {
		if (args.length == 3) {
			int timer;
			boolean hidden;
			try {
				timer = Integer.parseInt(args[1]);
			} catch (NumberFormatException ignored) {
				return;
			}
			if (args[2].equalsIgnoreCase("false")) {
				hidden = false;
			} else if (args[2].equalsIgnoreCase("true")) {
				hidden = true;
			} else {
				return;
			}

			if (args[0].equalsIgnoreCase("random")) {
				MysteryUniversePlugin.getMeteoriteManager().spawnNaturalMeteorite(timer, hidden);
			} else if (args[0].equalsIgnoreCase("here")) {
				new Meteorite(p.getLocation(), timer, hidden);
			}
		}
	}

}
