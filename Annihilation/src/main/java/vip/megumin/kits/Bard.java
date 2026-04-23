package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public class Bard extends AnniKitBase
{
	public Bard()
	{
		super("Bard", Material.JUKEBOX, url("Bard"),
				lore("You are the song.", "Your Buffbox grants nearby buffs", "and weakens enemies around you."),
				woodSword(), woodPick(), woodAxe(), named(Material.JUKEBOX, "Buffbox"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playSong(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p != null && event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Buffbox"))
		{
			if(startCooldown(player, "song", 15000))
				return;
			for(Player ally : alliesNear(player, p, 15))
			{
				ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0));
				ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 500, 0));
			}
			for(Player enemy : enemiesNear(player, p, 15))
			{
				enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 2));
				enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 1));
			}
			event.setCancelled(true);
		}
	}
}
