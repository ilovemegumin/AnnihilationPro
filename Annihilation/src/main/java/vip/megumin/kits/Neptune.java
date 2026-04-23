package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public class Neptune extends AnniKitBase
{
	public Neptune()
	{
		super("Neptune", Material.WATER_BUCKET, url("Neptune"),
				lore("You are the tide.", "Water grants speed and breath.", "Tidebringer slows nearby enemies."),
				stoneSword(), woodPick(), woodAxe(), named(Material.WATER_BUCKET, "Tidebringer"), named(Material.PACKED_ICE, "Ground Freeze"), item(Material.WATER_LILY));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void water(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) != null && (player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.STATIONARY_WATER))
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 200, 0));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void tide(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p != null && event.getAction().name().contains("RIGHT") && (namedLike(event.getItem(), "Tidebringer") || namedLike(event.getItem(), "Ground Freeze")))
		{
			if(startCooldown(player, "tide", 30000))
				return;
			player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 600, 0));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
			for(Player target : enemiesNear(player, p, 6))
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
			event.setCancelled(true);
		}
	}
}
