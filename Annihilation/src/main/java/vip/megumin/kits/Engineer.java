package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Engineer extends AnniKitBase
{
	public Engineer()
	{
		super("Engineer", Material.TNT, url("Engineer"),
				lore("You are the blast.", "Bunker Buster clears defenses", "and Martyrdom punishes attackers."),
				stoneSword(), named(Material.TNT, "Bunker Buster"), named(Material.BLAZE_ROD, "Evertool"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void bomb(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) != null && event.getAction().name().contains("RIGHT") && event.getItem() != null && event.getItem().getType() == Material.TNT)
		{
			if(startCooldown(player, "bomb", 30000))
				return;
			player.getWorld().createExplosion(player.getLocation().add(player.getLocation().getDirection().normalize().multiply(3)), 2.2F, false);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void martyr(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		if(getCurrentPlayer(player) != null)
			player.getWorld().createExplosion(player.getLocation(), 1.0F, false);
	}
}
