package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.mysteria.customapi.items.CustomItem;
import com.mysteria.customapi.items.CustomItemUseReason;
import com.mysteria.customapi.items.events.CustomItemUseEvent;
import com.mysteria.customapi.sounds.CustomSound;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.events.CustomSmithingEvent;
import com.mysteria.mysteryuniverse.events.PlayerHarvestEvent;
import com.mysteria.mysteryuniverse.systems.meteorite.events.MeteoriteLandEvent;
import com.mysteria.mysteryuniverse.systems.startinggift.events.StartingGiftSelectEvent;
import com.mysteria.parry.events.ParryEvent;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.UUID;

public class AdvancementsListener implements Listener {

	public AdvancementsListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@SuppressWarnings("deprecation")
	public boolean giveAdvancement(Player p, String tree, String name, String... criters) {
		if (tree == null || name == null) return false;

		Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(tree, name));

		if (p == null || advancement == null || criters == null) return false;

		AdvancementProgress advancementProgress = p.getAdvancementProgress(advancement);

		if (advancementProgress.isDone()) return false;

		for (String criter : criters) {
			if (!advancementProgress.awardCriteria(criter)) return false;
		}

		return true;

	}


	@EventHandler
	public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
		Component msg = e.message();
		if (msg != null) {
			e.message(MysteriaUtils.getChatPREFIX().append(msg.color(NamedColor.SILVER)));
		}
	}

	@EventHandler
	public void onCustomSmithing(CustomSmithingEvent e) {

		CustomItem customItem = CustomItem.getCustomItem(e.getConsumed());

		if (customItem == null) return;

		switch (customItem) {
			case FAIRY_DUST -> giveAdvancement(e.getPlayer(), "story", "hey_listen", "criter1");
			case STARMETAL_INGOT -> giveAdvancement(e.getPlayer(), "story", "maximum_power", "criter1");
			case SOULSTONE -> giveAdvancement(e.getPlayer(), "story", "pitiful_soul", "criter1");
			case BLESSING_SCROLL -> giveAdvancement(e.getPlayer(), "story", "gods_blessing", "criter1");
			case BOOK_OF_CONSCIOUSNESS -> giveAdvancement(e.getPlayer(), "story", "consciousness", "criter1");
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onMeteoriteLand(MeteoriteLandEvent e) {

		for (LivingEntity entity : e.getLocation().getNearbyLivingEntities(50)) {
			if (entity instanceof Player) {
				giveAdvancement((Player) entity, "story", "from_the_sky", "criter1");
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerHarvest(PlayerHarvestEvent e) {

		giveAdvancement(e.getPlayer(), "story", "harvest_season", "criter1");

	}


	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerRest(PlayerBedEnterEvent e) {
		if (e.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;

		if (e.getPlayer().getStatistic(Statistic.TIME_SINCE_REST) > 24000 * 60) {
			giveAdvancement(e.getPlayer(), "story", "insomniac", "criter1");
		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent e) {
		giveAdvancement(e.getEntity(), "adventure", "this_is_mystery_souls", "criter1");
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCustomItemUse(CustomItemUseEvent e) {
		switch (e.getCustomItem()) {
			case ARCHANGELS_FORGIVENESS:
				if (e.getReason() == CustomItemUseReason.PLAYER_PASSIVE && e.isUsed()) {
					giveAdvancement(e.getPlayer(), "adventure", "cheat_death", "criter1");
				}
				break;
			case BLOODSTONE:
				if (e.getReason() == CustomItemUseReason.PLAYER_RIGHT_CLICK && e.isUsed()) {
					giveAdvancement(e.getPlayer(), "adventure", "bloody", "criter1");
				}
				break;
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onParry(ParryEvent e) {
		giveAdvancement(e.getWhoParried(), "adventure", "vulnerable", "criter1");
	}

	@EventHandler(ignoreCancelled = true)
	public void onStarCraftingBreak(BlockBreakEvent e) {
		if (e.getBlock().getType() != Material.RESPAWN_ANCHOR || e.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;

		giveAdvancement(e.getPlayer(), "story", "patience", "criter1");

	}

	@EventHandler(ignoreCancelled = true)
	public void onDragonDeath(EntityDeathEvent e) {
		if (!(e.getEntity() instanceof EnderDragon)) return;

		Player p = new ArrayList<>(MysteryUniversePlugin.getEndManager().getTopDamagers().keySet()).get(0);
		if (p != null && p.isOnline()) giveAdvancement(p, "the_end", "free_the_end", "criter1");

	}

	@EventHandler
	public void onChargedCreeperDeath(EntityDeathEvent e) {
		if (!(e.getEntity() instanceof Creeper) || !((Creeper) e.getEntity()).isPowered()) return;
		if (e.getEntity().getLastDamageCause() == null || e.getEntity().getLastDamageCause().getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

		giveAdvancement(e.getEntity().getKiller(), "adventure", "death_game", "criter1");
	}

	@EventHandler(ignoreCancelled = true)
	public void onVoodooDollBurn(EntityDamageEvent e) {
		if (e.getCause() != EntityDamageEvent.DamageCause.LAVA || !(e.getEntity() instanceof Item) || e.getEntity().getWorld().getEnvironment() != World.Environment.NETHER) return;

		Item item = (Item) e.getEntity();
		ItemStack itemStack = item.getItemStack();

		if (!CustomItem.checkCustomItem(itemStack, CustomItem.VOODOO_DOLL)) return;

		UUID throwerUUID = item.getThrower();

		if (throwerUUID == null) return;

		Entity thrower = Bukkit.getEntity(throwerUUID);

		if (!(thrower instanceof Player) || !((Player) thrower).isOnline()) return;

		Player p = (Player) thrower;

		if (giveAdvancement(p, "nether", "farewell_guide", "criter1")) {
			CustomSound.play(p, CustomSound.OTHER_BOSS_AWOKEN, 1, 1);
			new BukkitRunnable() {
				@Override
				public void run() {
					CustomSound.play(p, CustomSound.MUSIC_BOSS2, 1, 1);
				}
			}.runTaskLater(MysteryUniversePlugin.getInstance(), 100);
		}


	}

	@EventHandler
	public void onGiftSelect(StartingGiftSelectEvent e) {
		if (e.getGift() != null) return;

		giveAdvancement(e.getPlayer(), "adventure", "who_needs_a_gift", "criter1");

	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		giveAdvancement(e.getPlayer(), "story", "root", "criter1");
	}

}
