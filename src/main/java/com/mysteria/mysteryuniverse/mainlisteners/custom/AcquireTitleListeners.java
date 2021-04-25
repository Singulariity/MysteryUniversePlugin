package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.titles.PlayerTitlesPlugin;
import com.mysteria.titles.enums.Title;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class AcquireTitleListeners implements Listener {

	public AcquireTitleListeners() {
		Bukkit.getPluginManager().registerEvents(this,  MysteryUniversePlugin.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	private void onAdvancementDone(PlayerAdvancementDoneEvent e) {

		Title title = null;
		switch (e.getAdvancement().getKey().toString()) {



			case "story:hatchet":
				title = Title.PUNK;
				break;
			case "story:camping":
			case "story:stone_age":
				title = Title.CAVEMAN;
				break;
			case "story:acquire_hardware":
				title = Title.BLACKSMITH;
				break;
			case "story:not_today_thank_you":
				title = Title.LEGIONNAIRE;
				break;
			case "story:path_to_the_nether":
				title = Title.HELL_PASSENGER;
				break;
			case "story:eye_spy":
				title = Title.VOID_FOLLOWER;
				break;
			case "story:librarian":
				title = Title.LIBRARIAN;
				break;
			case "story:suit_up":
				title = Title.MAN_AT_ARMS;
				break;
			case "story:star_crafting":
				title = Title.STARMIST;
				break;
			case "story:diamonds":
				title = Title.GORGEOUS;
				break;
			case "story:diamond_guy":
				title = Title.DIAMOND_GUY;
				break;
			case "story:time_to_tracking":
				title = Title.MAGNETIC;
				break;
			case "story:insomniac":
				title = Title.INSOMNIAC;
				break;
			case "story:enchanter":
				title = Title.ENCHANTER;
				break;
			case "story:hey_listen":
				title = Title.FAIRY;
				break;
			case "story:pitiful_soul":
				title = Title.SOULLESS;
				break;
			case "story:gods_blessing":
				title = Title.BLESSED;
				break;
			case "story:consciousness":
				title = Title.ASCENDED;
				break;




			case "nether:farewell_guide":
				title = Title.THE_GUIDE;
				break;
			case "nether:dimensional_collapse":
				title = Title.INTERDIMENSIONAL_EXPLORER;
				break;
			case "nether:war_pigs":
				title = Title.DEADMAN;
				break;
			case "nether:cover_me_in_debris":
				title = Title.DEMON_LORD;
				break;
			case "nether:a_terrible_fortress":
				title = Title.INTRUDER;
				break;
			case "nether:black_knight":
				title = Title.BLACK_KNIGHT;
				break;
			case "nether:local_brewery":
				title = Title.ALCHEMIST;
				break;
			case "nether:oh_shiny":
				title = Title.MERCHANT;
				break;
			case "nether:what_a_waste":
				title = Title.WORTHY;
				break;
			case "nether:hot_tourist_destinations":
				title = Title.TOURIST;
				break;
			case "nether:pathfinder":
				title = Title.PATHFINDER;
				break;



			case "adventure:root":
				title = Title.ADVENTURER;
				break;
			case "adventure:this_is_mystery_souls":
				title = Title.UNDEAD;
				break;
			case "adventure:bloody":
				title = Title.VAMPIRE;
				break;
			case "adventure:vulnerable":
				title = Title.DUELLER;
				break;
			case "adventure:death_game":
				title = Title.FEARLESS;
				break;
			case "adventure:ol_betsy":
				title = Title.ARCHER;
				break;
			case "adventure:whos_the_pillager_now":
				title = Title.PILLAGER;
				break;
			case "adventure:cheat_death":
				title = Title.CHEATER;
				break;
			case "adventure:postmortal":
				title = Title.POSTMORTAL;
				break;
			case "adventure:traveler":
				title = Title.TRAVELER;
				break;
			case "adventure:sniper":
				title = Title.SNIPER;
				break;
			case "adventure:who_needs_a_gift":
				title = Title.UNFORGIVEN;
				break;




			case "the_end:root":
				break;
			case "the_end:free_the_end":
				title = Title.DRAGON_SLAYER;
				break;




			case "ocean:unfurl_the_sails":
				title = Title.SAILOR;
				break;
			case "ocean:weapon_of_the_sea":
				title = Title.ATLANTEAN;
				break;
			case "ocean:friends":
				title = Title.FRIENDLY;
				break;
			case "ocean:x_marks_the_spot":
				title = Title.TREASURE_HUNTER;
				break;
			case "ocean:betrayal_of_trust":
				title = Title.TRAITOR;
				break;
			case "ocean:regret":
				title = Title.PREDATOR;
				break;
		}

		if (title != null) {
			PlayerTitlesPlugin.getTitleManager().addTitle(e.getPlayer(), title);
		}

	}

}
