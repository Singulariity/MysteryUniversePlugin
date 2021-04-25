package com.mysteria.mysteryuniverse.systems.startinggift.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StartingGiftSelectEvent extends Event {

	private final Player player;
	private final ItemStack gift;

	public StartingGiftSelectEvent(@Nonnull Player player, @Nullable ItemStack gift) {
		this.player = player;
		this.gift = gift;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public @Nonnull	HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Nonnull
	public Player getPlayer() {
		return player;
	}

	@Nullable
	public ItemStack getGift() {
		return gift;
	}


}
