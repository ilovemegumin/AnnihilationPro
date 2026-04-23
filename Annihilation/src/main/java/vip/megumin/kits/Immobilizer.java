package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public class Immobilizer extends AnniKitBase
{
	public Immobilizer()
	{
		super("Immobilizer", Material.SLIME_BALL, url("Immobilizer"),
				lore("You are the lock.", "Right-click to immobilize one enemy.", "Left-click slows enemies around you."),
				woodSword(), woodPick(), woodAxe(), named(Material.SLIME_BALL, "Immobilizer"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p == null || !namedLike(event.getItem(), "Immobilizer") || startCooldown(player, "immobilize", 30000))
			return;
		if(event.getAction().name().contains("RIGHT"))
		{
			Player target = getTargetPlayer(player, 5);
			if(target != null && isEnemy(p, target))
			{
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 10));
				target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 128));
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 10));
			}
		}
		else if(event.getAction().name().contains("LEFT"))
		{
			for(Player target : enemiesNear(player, p, 5))
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
		}
		event.setCancelled(true);
	}
}
