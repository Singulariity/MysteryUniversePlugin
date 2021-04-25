package com.mysteria.mysteryuniverse.mainlisteners.custom;

import com.mysteria.customapi.effects.CustomEffect;
import com.mysteria.customapi.effects.CustomEffectType;
import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.utils.MysteriaUtils;
import net.minecraft.server.v1_16_R3.MobEffectInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.Arrays;

public class WitchPotionSplashListener implements Listener {

	public WitchPotionSplashListener() {
		Bukkit.getPluginManager().registerEvents(this, MysteryUniversePlugin.getInstance());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onPotionSplash(PotionSplashEvent e) {

		ProjectileSource source = e.getEntity().getShooter();

		 if (source instanceof Witch) {

			if (MysteriaUtils.chance(30)) {
				ArrayList<PotionEffectType> effectTypes = new ArrayList<>(Arrays.asList(PotionEffectType.values()));
				effectTypes.removeIf(type -> !(type instanceof CustomEffect) ||
						((CustomEffect) type).getInfo() == MobEffectInfo.BENEFICIAL);

				PotionEffectType type = effectTypes.get(effectTypes.size() - 1);
				int duration = type == CustomEffectType.DOOM ? 1080 * 20 : 10 * 20;
				PotionEffect effect = new PotionEffect(type, duration, 0);

				for (LivingEntity entity : e.getAffectedEntities()) {
					if (entity instanceof Player) {
						entity.addPotionEffect(effect);
					}
				}

			}

		}


	}

}
