package com.mysteria.mysteryuniverse.systems;

import com.mysteria.customapi.sounds.CustomSound;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import com.mysteria.utils.NamedColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class SpawnListeners implements Listener {

	private final World spawn_world = Bukkit.getWorld("spawn");
	private final HashMap<Player, BukkitTask> musicTimers = new HashMap<>();

	public SpawnListeners() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}


	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		if (e.getFrom() == spawn_world) {
			stopMusic(e.getPlayer());
		}
		else if (e.getPlayer().getWorld() == spawn_world) {
			playMusic(e.getPlayer());
		}

	}

	@EventHandler
	public void onResourcePack(PlayerResourcePackStatusEvent e) {
		Player p = e.getPlayer();
		switch (e.getStatus()) {
			case ACCEPTED:
				Component outLine = MysteriaUtils.centeredComponent(
						Component.text("---------------------------------------------", NamedColor.BEEKEEPER)
								.decorate(TextDecoration.STRIKETHROUGH).decorate(TextDecoration.BOLD));
				p.sendMessage(outLine);
				p.sendMessage(Component.space());
				p.sendMessage(MysteriaUtils.centeredComponent(
						Component.text("Thanks for accepting server resource pack!", NamedColor.DOWNLOAD_PROGRESS)));
				p.sendMessage(MysteriaUtils.centeredComponent(
						Component.text("Download has been started in background...", NamedColor.BEEKEEPER)));
				p.sendMessage(Component.space());
				p.sendMessage(outLine);
				break;
			case SUCCESSFULLY_LOADED:
				if (p.getWorld() == spawn_world) {
					playMusic(p);
				}
				break;
			case DECLINED:
				p.kick(Component.text("You have to accept server resource pack.", NamedColor.CARMINE_PINK));
				break;
			case FAILED_DOWNLOAD:
				p.kick(Component.text("An error occurred while downloading resource pack.", NamedColor.CARMINE_PINK));
				break;
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getWorld() == spawn_world) {
			stopMusic(e.getPlayer());
		}
	}



	public void playMusic(Player p) {
		stopMusic(p);
		CustomSound.play(p, CustomSound.MUSIC_GODSHOME, SoundCategory.MASTER, 1, 1);
		int musicLength = 275 * 20;
		BukkitTask runnable = new BukkitRunnable() {
			@Override
			public void run() {
				playMusic(p);
			}
		}.runTaskLater(MysteryUniversePlugin.getInstance(), musicLength);
		musicTimers.put(p, runnable);
	}

	public void stopMusic(Player p) {
		CustomSound.stop(p, CustomSound.MUSIC_GODSHOME);
		if (musicTimers.containsKey(p)) {
			musicTimers.get(p).cancel();
			musicTimers.remove(p);
		}
	}

}
