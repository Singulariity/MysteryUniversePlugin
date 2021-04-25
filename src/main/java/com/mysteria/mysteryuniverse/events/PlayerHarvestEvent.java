package com.mysteria.mysteryuniverse.events;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class PlayerHarvestEvent extends Event {

	private final Player player;
	private final ItemStack tool;
	private final BlockState blockState;
	private boolean toolDamage;

	public PlayerHarvestEvent(@Nonnull Player player, @Nonnull ItemStack tool, @Nonnull BlockState blockState) {
		this.player = player;
		this.tool = tool;
		this.blockState = blockState;
		this.toolDamage = true;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public @Nonnull
	HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public Player getPlayer() {
		return player;
	}


	/**
	 * Gets a copy of the item the player is currently holding
	 * in their main hand.
	 *
	 * @return the currently held item
	 */
	public ItemStack getTool() {
		return tool;
	}

	public boolean isToolDamage() {
		return toolDamage;
	}

	public void setToolDamage(boolean toolDamage) {
		this.toolDamage = toolDamage;
	}

	public BlockState getBlockState() {
		return blockState;
	}



}
