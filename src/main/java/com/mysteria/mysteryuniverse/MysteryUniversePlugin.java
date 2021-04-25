package com.mysteria.mysteryuniverse;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import com.mysteria.customapi.items.CustomItem;
import com.mysteria.mysteryuniverse.commands.RecipeCommand;
import com.mysteria.mysteryuniverse.commands.ReloadCommand;
import com.mysteria.mysteryuniverse.commands.SpawnMeteoriteCommand;
import com.mysteria.mysteryuniverse.commands.StartingGiftCommand;
import com.mysteria.mysteryuniverse.database.Database;
import com.mysteria.mysteryuniverse.database.listeners.PlayerCreateDataListener;
import com.mysteria.mysteryuniverse.mainlisteners.custom.*;
import com.mysteria.mysteryuniverse.mainlisteners.entity.*;
import com.mysteria.mysteryuniverse.mainlisteners.player.*;
import com.mysteria.mysteryuniverse.mainlisteners.server.ExplosionPrimeListener;
import com.mysteria.mysteryuniverse.mainlisteners.server.FarmingListeners;
import com.mysteria.mysteryuniverse.mainlisteners.server.FurnaceBurnListener;
import com.mysteria.mysteryuniverse.systems.SpawnListeners;
import com.mysteria.mysteryuniverse.systems.meteorite.MeteoriteManager;
import com.mysteria.mysteryuniverse.systems.recipemanager.RecipeManager;
import com.mysteria.mysteryuniverse.systems.starcrafting.StarCraftingManager;
import com.mysteria.mysteryuniverse.systems.starcrafting.StarCraftingRecipe;
import com.mysteria.mysteryuniverse.systems.startinggift.StartingGiftManager;
import com.mysteria.mysteryuniverse.systems.theend.EndManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

public final class MysteryUniversePlugin extends JavaPlugin {

	private static MysteryUniversePlugin instance;
	private static Database database;
	private static Connection connection;
	private static StartingGiftManager startingGiftManager;
	private static StarCraftingManager starCraftingManager;
	private static MeteoriteManager meteoriteManager;
	private static RecipeManager recipeManager;
	private static EndManager endManager;
	private static PaperCommandManager commandManager;

	public MysteryUniversePlugin() {
		if (instance != null) throw new IllegalStateException();
		instance = this;
	}

	@Override
	public void onEnable() {

		saveDefaultConfig();
		setupDatabase();

		registerManagers();

		registerListeners();

		registerCommands();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void setupDatabase() {
		HikariConfig config = new HikariConfig();

		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		try {
			String url = "jdbc:sqlite:./" + this.getDataFolder() + "/sanity_data.db";

			config.setJdbcUrl(url);
			config.setUsername("mysteria");

			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

			HikariDataSource ds = new HikariDataSource(config);

			connection = ds.getConnection();
			Statement st = connection.createStatement();
			st.execute("CREATE TABLE IF NOT EXISTS player_data (" +
					"PLAYER UUID NOT NULL PRIMARY KEY, " +
					"COOLDOWN_MILK BIGINT NOT NULL DEFAULT '0', " +
					"COOLDOWN_STEW BIGINT NOT NULL DEFAULT '0', " +
					"STARTING_GIFT BOOLEAN NOT NULL DEFAULT '0')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		database = new Database();
	}

	private void registerManagers() {
		commandManager = new PaperCommandManager(getInstance());
		startingGiftManager = new StartingGiftManager();
		starCraftingManager = new StarCraftingManager();
		meteoriteManager = new MeteoriteManager();
		recipeManager = new RecipeManager();
		endManager = new EndManager();
	}

	private void registerListeners() {
		// Spawn
		new SpawnListeners();
		// Database
		new PlayerCreateDataListener();
		// Block
		new AcquireTitleListeners();
		new AdvancementsListener();
		new CustomArrowListener();
		new CustomItemUseListener();
		new EnchantmentListeners();
		new EnchListeners();
		new PlayerDeathKeepListener();
		new WitchPotionSplashListener();
		// Entity
		new CreeperTargetPlayerListener();
		new DisableEntityPortalListener();
		new EntityBreedListener();
		new EntityDeathDropsListener();
		new EntitySpawnListener();
		new SheepListeners();
		new SpawnerSpawnListener();
		// Player
		new PlayerBedEnterListener();
		new PlayerFallDamageListener();
		new PlayerFishingListener();
		new PlayerInteractEntityListener();
		new PlayerItemConsumeListener();
		new PlayerLocaleCheckListener();
		new PlayerPoisonListener();
		// Server
		new ExplosionPrimeListener();
		new FarmingListeners();
		new FurnaceBurnListener();
	}

	private void registerCommandCompletions() {
		ArrayList<String> itemNames = new ArrayList<>();
		for (StarCraftingRecipe recipe : getStarCraftingManager().getRecipes()) {
			CustomItem customItem = CustomItem.getCustomItem(recipe.getResult());
			if (customItem == null) {
				itemNames.add(recipe.getResult().getType().toString().toLowerCase());
			} else {
				itemNames.add(customItem.toString().toLowerCase());
			}
		}
		Iterator<Recipe> it = Bukkit.recipeIterator();
		while (it.hasNext()) {
			Recipe recipe = it.next();
			if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe) {
				CustomItem customItem = CustomItem.getCustomItem(recipe.getResult());
				String name;
				if (customItem == null) {
					name = recipe.getResult().getType().toString().toLowerCase();
				} else {
					name = customItem.toString().toLowerCase();
				}
				if (!itemNames.contains(name)) {
					itemNames.add(name);
				}
			}
		}
		getCommandManager().getCommandCompletions().registerAsyncCompletion("item", c -> itemNames);
		getCommandManager().getCommandCompletions().registerAsyncCompletion("meteorite1st", c ->
				ImmutableList.of("60", "120", "180", "240", "300", "600"));
		getCommandManager().getCommandCompletions().registerAsyncCompletion("meteorite2nd", c ->
				ImmutableList.of("true", "false"));
		getCommandManager().getCommandCompletions().registerAsyncCompletion("meteorite3rd", c ->
				ImmutableList.of("here", "random"));
	}

	private void registerCommands() {
		registerCommandCompletions();
		getCommandManager().registerCommand(new RecipeCommand());
		getCommandManager().registerCommand(new ReloadCommand());
		getCommandManager().registerCommand(new SpawnMeteoriteCommand());
		getCommandManager().registerCommand(new StartingGiftCommand());
	}

	public static MysteryUniversePlugin getInstance() {
		if (instance == null) throw new IllegalStateException();
		return instance;
	}

	public static Database getDatabase() {
		return database;
	}

	public static Connection getConnection() {
		return connection;
	}

	public static PaperCommandManager getCommandManager() {
		return commandManager;
	}

	public static StartingGiftManager getStartingGiftManager() {
		return startingGiftManager;
	}

	public static StarCraftingManager getStarCraftingManager() {
		return starCraftingManager;
	}

	public static MeteoriteManager getMeteoriteManager() {
		return meteoriteManager;
	}

	public static RecipeManager getRecipeManager() {
		return recipeManager;
	}

	public static EndManager getEndManager() {
		return endManager;
	}
}
