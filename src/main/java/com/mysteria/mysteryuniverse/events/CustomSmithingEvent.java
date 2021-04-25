package com.mysteria.mysteryuniverse.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class CustomSmithingEvent extends Event {

	private final Player player;
	private final ItemStack consumed;

	public CustomSmithingEvent(@Nonnull Player player, @Nonnull ItemStack consumed) {
		this.player = player;
		this.consumed = consumed.clone();
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

	@Nonnull
	public ItemStack getConsumed() {
		return consumed;
	}

}
