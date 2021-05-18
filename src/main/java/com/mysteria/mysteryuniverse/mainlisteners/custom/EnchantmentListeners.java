package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.google.common.collect.ImmutableSet;
import com.mysteria.customapi.enchantments.CustomEnchantment;
import com.mysteria.customapi.itemmanager.ItemInfo;
import com.mysteria.customapi.itemmanager.containers.EnchantmentContainer;
import com.mysteria.customapi.itemmanager.containers.ItemTagContainer;
import com.mysteria.customapi.items.CustomItem;
import com.mysteria.customapi.itemtags.ItemTag;
import com.mysteria.mysteryuniverse.events.CustomSmithingEvent;
import com.mysteria.utils.ItemBuilder;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.MysteriaUtilsPlugin;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//@SuppressWarnings("all")
public class EnchantmentListeners implements Listener {

	public EnchantmentListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteriaUtilsPlugin.getInstance());
	}

	private final ImmutableSet<Enchantment> FORBIDDEN_ENCHANTS = new ImmutableSet.Builder<Enchantment>()
			.add(Enchantment.SILK_TOUCH)
			.add(Enchantment.ARROW_INFINITE)
			.add(Enchantment.PROTECTION_EXPLOSIONS)
			.add(Enchantment.PROTECTION_ENVIRONMENTAL)
			.add(Enchantment.PROTECTION_FIRE)
			.add(Enchantment.PROTECTION_PROJECTILE)
			.add(Enchantment.PROTECTION_FALL)
			.add(Enchantment.ARROW_FIRE)
			.add(Enchantment.LOOT_BONUS_BLOCKS)
			.add(Enchantment.LOOT_BONUS_MOBS)
			.add(Enchantment.MENDING)
			.add(Enchantment.MULTISHOT)
			.add(Enchantment.PIERCING)
			.add(Enchantment.ARROW_DAMAGE)
			.add(Enchantment.RIPTIDE)
			.add(Enchantment.DAMAGE_ALL)
			.build();

	@EventHandler(ignoreCancelled = true)
	private void onEnchant(EnchantItemEvent e) {

		ItemStack itemStack = e.getItem();

		e.getEnchantsToAdd().clear();

		ItemInfo itemInfo = ItemInfo.get(itemStack);
		EnchantmentContainer enchContainer = itemInfo.getEnchantmentContainer();
		ItemTagContainer itemTagContainer = itemInfo.getItemTagContainer();
		if (enchContainer.hasAny()) {
			e.setCancelled(true);
			MysteriaUtils.sendMessageDarkRed(e.getEnchanter(), "This item already enchanted.");
			return;
		}

		ArrayList<Enchantment> enchantments = getAvailableEnchants(itemStack, false);
		int curseCount = 0;
		boolean isBlessed = itemTagContainer.hasTag(ItemTag.BLESSED);
		if (isBlessed) {
			itemTagContainer.removeTag(ItemTag.BLESSED);
			itemTagContainer.update();
		}
		boolean isAscended = enchContainer.isAscended();
		if (!isAscended) {
			isAscended = e.getExpLevelCost() >= 30;
			enchContainer.setAscended(isAscended);
			enchContainer.update();
		}

		for (int i = 0; i < enchContainer.getLimit(); i++) {
			if (enchantments.size() == 0) break;

			Enchantment selected = enchantments.get(MysteriaUtils.getRandom(0, enchantments.size() - 1));
			enchantments.remove(selected);

			boolean passed = true;
			for (Enchantment control : e.getEnchantsToAdd().keySet()) {
				if (control.conflictsWith(selected) || selected.conflictsWith(control)) {
					passed = false;
					break;
				}
			}

			if (selected.isCursed()) {
				if (curseCount >= 2 || isBlessed) {
					passed = false;
				}
				curseCount++;
			}

			if (passed) {
				int min = selected.getStartLevel();
				int max = (int) (((double) selected.getMaxLevel() / 3) * Math.ceil((double) e.getExpLevelCost() / 10));
				max = Math.max(max, selected.getStartLevel());
				max = Math.min(max, selected.getMaxLevel());
				e.getEnchantsToAdd().put(selected, MysteriaUtils.getRandom(min, max));
			} else {
				i--;
			}

		}

		if (e.getEnchantsToAdd().size() == 0) {
			e.setCancelled(true);
			MysteriaUtils.sendMessageDarkRed(e.getEnchanter(), "This item cannot be enchanted.");
			return;
		}

		if (CustomItem.checkCustomItem(itemStack, CustomItem.BOOK_OF_ETERNITY)) {
			ItemStack book = new ItemStack(Material.BOOK);
			itemStack.setItemMeta(book.getItemMeta());
		}

		if (e.getEnchanter().getGameMode() != GameMode.CREATIVE) {

			int reduce = e.getExpLevelCost() - (e.whichButton() + 1);
			new BukkitRunnable() {
				@Override
				public void run() {
					e.getEnchanter().setLevel(e.getEnchanter().getLevel() - reduce);

					EnchantingInventory inv = (EnchantingInventory) e.getInventory();
					if (inv.getItem().getType() == Material.ENCHANTED_BOOK) {
						ItemStack enchBook = CustomItem.BOOK_OF_ETERNITY.getItemStack();
						enchBook.addUnsafeEnchantments(e.getEnchantsToAdd());
						EnchantmentContainer enchBookEnchContainer = EnchantmentContainer.get(enchBook);
						enchBookEnchContainer.setLimit(enchContainer.getLimit());
						enchBookEnchContainer.setAscended(enchContainer.isAscended());
						enchBookEnchContainer.update();
						inv.setItem(enchBook);
					}

				}
			}.runTaskLater(MysteriaUtilsPlugin.getInstance(), 1);
		}

	}

	@EventHandler(ignoreCancelled = true)
	private void onPrepareEnchant(PrepareItemEnchantEvent e) {

		if (e.getItem().getType() == Material.BOOK && !CustomItem.checkCustomItem(e.getItem(), CustomItem.BOOK_OF_ETERNITY)) {
			e.setCancelled(true);
			return;
		}

		for (EnchantmentOffer offer : e.getOffers()) {
			if (offer != null) {
				offer.setEnchantment(Enchantment.LURE);
				offer.setEnchantmentLevel(1);
			}
		}

	}


	@EventHandler(ignoreCancelled = true)
	private void onEntityDeath(EntityDeathEvent e) {

		// Remove enchantments of drops
		for (ItemStack drop : e.getDrops()) {
			if (drop.getEnchantmentLevel(Enchantment.LURE) == 5) continue;
			for (Enchantment ench : drop.getEnchantments().keySet()) {
				if (FORBIDDEN_ENCHANTS.contains(ench)) {
					drop.removeEnchantment(ench);
				}
			}
		}

	}


	@EventHandler
	public void onPrepareResult(PrepareResultEvent e) {

		//if (e.getView().getPlayer().hasPermission("survivalplus.bypass")) return;

		if (e.getInventory() instanceof AnvilInventory) {
			if (e.getResult() == null) return;

			AnvilInventory anvil = (AnvilInventory) e.getInventory();

			if (isModified(anvil.getFirstItem(), false) ||
					isModified(anvil.getSecondItem(), false) ||
					isModified(e.getResult(), false)) {
				e.setResult(null);
			} else {
				ItemStack first = anvil.getFirstItem();
				ItemStack result = e.getResult();
				if (first != null && first.getType() != Material.AIR &&
						result != null && result.getType() != Material.AIR) {
					if (first.containsEnchantment(CustomEnchantment.REPAIRING_CURSE)) {
						e.setResult(null);
					} else {
						result = result.clone();
						reEnchant(result, first.getEnchantments());
						e.setResult(result);
					}
				}
			}



		}
		else if (e.getInventory() instanceof GrindstoneInventory) {
			if (e.getResult() == null) return;

			GrindstoneInventory grindstone = (GrindstoneInventory) e.getInventory();

			if (isModified(grindstone.getUpperItem(), true) ||
					isModified(grindstone.getLowerItem(), true) ||
					isModified(e.getResult(), true)) {
				e.setResult(null);
			}

		}
		else if (e.getInventory() instanceof SmithingInventory) {

			SmithingInventory smithing = (SmithingInventory) e.getInventory();

			ItemStack input = smithing.getInputEquipment();
			ItemStack second = smithing.getInputMineral();

			if (input == null || input.getType() == Material.AIR ||
					input.getAmount() > 1 ||
					second == null || second.getType() == Material.AIR) {
				return;
			}

			ItemStack resultItem = input.clone();

			if (CustomItem.checkCustomItem(second, CustomItem.BOOK_OF_ETERNITY)) {
				if (second.getEnchantments().size() == 0) return;
				//if (input.getEnchantments().size() > 0) return;

				EnchantmentContainer enchContainerSecond = EnchantmentContainer.get(second);
				ItemInfo resultInfo = ItemInfo.get(resultItem);
				EnchantmentContainer enchContainerResult = resultInfo.getEnchantmentContainer();
				ItemTagContainer itemTagContainer = resultInfo.getItemTagContainer();
				boolean isBlessed = itemTagContainer.hasTag(ItemTag.BLESSED);

				Map<Enchantment, Integer> toAdd = new HashMap<>();

				int emptySlot = enchContainerResult.getLimit() - enchContainerResult.getEnchs().size();
				boolean book_of_eternity = CustomItem.checkCustomItem(input, CustomItem.BOOK_OF_ETERNITY);
				for (Map.Entry<Enchantment, Integer> entry : enchContainerSecond.getEnchs().entrySet()) {
					if (emptySlot <= 0) break;

					if (book_of_eternity || entry.getKey().canEnchantItem(input)) {
						if (isConflicts(input, entry.getKey()) || (entry.getKey().isCursed() && isBlessed)) {
							continue;
						}
						emptySlot--;
						toAdd.put(entry.getKey(), entry.getValue());
					}
				}

				if (toAdd.size() > 0) {
					itemTagContainer.removeTag(ItemTag.BLESSED);
					itemTagContainer.update();
					if (!enchContainerResult.isAscended()) {
						enchContainerResult.setAscended(enchContainerSecond.isAscended());
						enchContainerResult.update();
					}
					resultItem.addUnsafeEnchantments(toAdd);
					e.setResult(resultItem);
				}

			}
			else if (CustomItem.checkCustomItem(second, CustomItem.FAIRY_DUST)) {
				if (!EnchantmentTarget.BREAKABLE.includes(input) &&
						!CustomItem.checkCustomItem(input, CustomItem.BOOK_OF_ETERNITY)) {
					return;
				}

				EnchantmentContainer container = EnchantmentContainer.get(resultItem);

				if (container.getLimit() < 4) {
					container.setLimit(container.getLimit() + 1);
					container.update();
					e.setResult(resultItem);
				}

			}
			else if (CustomItem.checkCustomItem(second, CustomItem.STARMETAL_INGOT)) {
				if (!EnchantmentTarget.BREAKABLE.includes(input) &&
						!CustomItem.checkCustomItem(input, CustomItem.BOOK_OF_ETERNITY)) {
					return;
				}

				EnchantmentContainer container = EnchantmentContainer.get(resultItem);

				if (container.getLimit() == 4) {
					container.setLimit(5);
					container.update();
					e.setResult(resultItem);
				}

			}
			else if (CustomItem.checkCustomItem(second, CustomItem.SOULSTONE)) {

				EnchantmentContainer container = EnchantmentContainer.get(resultItem);

				if (container.hasAny()) {
					if (container.hasEnch(CustomEnchantment.ALDOUS_CURSE)) {
						container.removeEnch(CustomEnchantment.ALDOUS_CURSE);
					} else {
						container.clearEnchs();
					}
					container.update();
					e.setResult(resultItem);
				}

			}
			else if (CustomItem.checkCustomItem(second, CustomItem.BLESSING_SCROLL)) {
				if (!EnchantmentTarget.BREAKABLE.includes(input) &&
						!CustomItem.checkCustomItem(input, CustomItem.BOOK_OF_ETERNITY)) {
					return;
				}

				ItemTagContainer container = ItemInfo.get(resultItem).getItemTagContainer();

				if (!container.hasTag(ItemTag.BLESSED)) {
					container.addTag(ItemTag.BLESSED);
					container.update();
					e.setResult(resultItem);
				}

			}
			else if (CustomItem.checkCustomItem(second, CustomItem.BOOK_OF_CONSCIOUSNESS)) {

				EnchantmentContainer container = EnchantmentContainer.get(resultItem);

				if (container.hasAny() && container.isAscended()) {
					ItemStack question_mark = ItemBuilder.builder(CustomItem.QUESTION_MARK.getItemStack())
							.adaptGUI()
							.name(Component.text("???", NamedColor.SOARING_EAGLE)
									.decoration(TextDecoration.ITALIC, false))
							.build();
					e.setResult(question_mark);
				}
			}

		}

	}


	@EventHandler(ignoreCancelled = true)
	private void onInventoryClick(InventoryClickEvent e) {
		if (e.getClickedInventory() instanceof SmithingInventory) {
			if (e.getRawSlot() != 2) return;
			if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) return;

			SmithingInventory inv = (SmithingInventory) e.getClickedInventory();

			if (inv.getResult() == null || inv.getResult().getType() == Material.AIR) return;
			if (inv.getInputEquipment() == null || inv.getInputEquipment().getType() == Material.AIR) return;
			if (inv.getInputMineral() == null || inv.getInputMineral().getType() == Material.AIR) return;

			CustomSmithingEvent event = new CustomSmithingEvent((Player) e.getWhoClicked(), inv.getInputMineral());
			Bukkit.getPluginManager().callEvent(event);

			ItemStack result = inv.getResult().clone();
			if (CustomItem.checkCustomItem(result, CustomItem.QUESTION_MARK)) {
				ItemStack input = inv.getInputEquipment().clone();

				ItemInfo inputInfo = ItemInfo.get(input);
				ItemTagContainer itemTagContainer = inputInfo.getItemTagContainer();
				int limit = input.getEnchantments().size();

				ArrayList<Enchantment> available = getAvailableEnchants(input, false);
				Map<Enchantment, Integer> toAdd = new HashMap<>();
				int curseCount = 0;
				boolean isBlessed = itemTagContainer.hasTag(ItemTag.BLESSED);
				if (isBlessed) {
					itemTagContainer.removeTag(ItemTag.BLESSED);
					itemTagContainer.update();
				}

				for (int i = 0; i < limit; i++) {
					if (available.size() == 0) break;

					Enchantment selected = available.get(MysteriaUtils.getRandom(0, available.size() - 1));
					available.remove(selected);

					boolean passed = true;
					for (Enchantment control : toAdd.keySet()) {
						if (control.conflictsWith(selected) || selected.conflictsWith(control)) {
							passed = false;
							break;
						}
					}

					if (selected.isCursed()) {
						if (curseCount >= 2 || isBlessed) {
							passed = false;
						}
						curseCount++;
					}

					if (passed) {
						toAdd.put(selected, MysteriaUtils.getRandom(selected.getStartLevel(), selected.getMaxLevel()));
					} else {
						i--;
					}

				}
				reEnchant(input, toAdd);
				result = input;
			}

			inv.setInputEquipment(null);
			inv.getInputMineral().setAmount(inv.getInputMineral().getAmount() - 1);
			inv.setInputMineral(inv.getInputMineral());
			inv.setResult(null);
			e.getWhoClicked().setItemOnCursor(result);
			e.getWhoClicked().getWorld().playSound(e.getWhoClicked().getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1, 1);

		}
	}




	/**
	 *
	 * @return true if item is modified
	 */
	private boolean isModified(ItemStack itemStack, boolean checkEnchants) {

		if (itemStack == null) return false;

		ItemMeta meta = itemStack.getItemMeta();
		return CustomItem.isCustomItem(itemStack) || meta.hasDisplayName() || (checkEnchants && meta.hasEnchants()) || meta instanceof EnchantmentStorageMeta;

	}

	private ArrayList<Enchantment> getAvailableEnchants(@Nonnull ItemStack itemStack, boolean includeTreasure) {
		ArrayList<Enchantment> enchantments = new ArrayList<>(Arrays.asList(Enchantment.values()));

		if (CustomItem.checkCustomItem(itemStack, CustomItem.BOOK_OF_ETERNITY)) {
			enchantments.removeIf(ench ->
					FORBIDDEN_ENCHANTS.contains(ench) ||
							(!includeTreasure && ench.isTreasure() && !ench.isCursed())
			);
		} else {
			enchantments.removeIf(ench ->
					!ench.canEnchantItem(itemStack) ||
							FORBIDDEN_ENCHANTS.contains(ench) ||
							(!includeTreasure && ench.isTreasure() && !ench.isCursed())
			);
		}
		return enchantments;
	}

	private void reEnchant(@Nonnull ItemStack itemStack, @Nonnull Map<Enchantment, Integer> enchantments) {
		for (Enchantment ench : itemStack.getEnchantments().keySet()) {
			itemStack.removeEnchantment(ench);
		}
		itemStack.addUnsafeEnchantments(enchantments);
	}

	private boolean isConflicts(@Nonnull ItemStack itemStack, @Nonnull Enchantment enchantment) {
		for (Enchantment ench : itemStack.getEnchantments().keySet()) {
			if (ench.conflictsWith(enchantment) || enchantment.conflictsWith(ench)) {
				return true;
			}
		}
		return false;
	}

}
