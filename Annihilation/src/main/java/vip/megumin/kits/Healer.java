package vip.megumin.kits;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public class Healer extends AnniKitBase
{
	public Healer()
	{
		super("Healer", Material.REDSTONE, url("Healer"),
				lore("You are the cure.", "Blood Bag heals nearby allies", "or saves one ally in danger."),
				woodSword(), woodPick(), woodAxe(), named(Material.REDSTONE, "Blood Bag"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void heal(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p == null || !namedLike(event.getItem(), "Blood Bag"))
			return;
		if(event.getAction().name().contains("RIGHT"))
		{
			if(startCooldown(player, "groupheal", 15000))
				return;
			List<Player> allies = alliesNear(player, p, 6);
			for(int healed = 0; healed < 3 && !allies.isEmpty(); healed++)
			{
				Player lowest = allies.get(0);
				for(Player ally : allies)
					if(ally.getHealth() < lowest.getHealth())
						lowest = ally;
				lowest.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 2));
				allies.remove(lowest);
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
			event.setCancelled(true);
		}
		else if(event.getAction().name().contains("LEFT"))
		{
			if(startCooldown(player, "burstheal", 45000))
				return;
			Player target = getTargetPlayer(player, 10);
			if(target != null && isAlly(p, target))
				healPlayer(target, 15.0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 0));
			event.setCancelled(true);
		}
	}
}
