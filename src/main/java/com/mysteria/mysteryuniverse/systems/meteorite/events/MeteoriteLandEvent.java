package com.mysteria.mysteryuniverse.systems.meteorite.events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class MeteoriteLandEvent extends Event implements Cancellable {

	private final Location location;
	private boolean isCancelled;

	public MeteoriteLandEvent(@Nonnull Location location) {
		this.location = location;
		this.isCancelled = false;
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
	public Location getLocation() {
		return location;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}


}
