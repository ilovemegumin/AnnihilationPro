package vip.megumin.kits;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

public class Dasher extends AnniKitBase
{
	public Dasher()
	{
		super("Dasher", Material.ENDER_PEARL, url("Dasher"),
				lore("You are the blink.", "Sneak to dash through open space.", "Cooldown scales with distance."),
				woodSword(), woodPick(), woodAxe(), named(Material.ENDER_PEARL, "Blink"));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void dash(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();
		if(!event.isSneaking() || getCurrentPlayer(player) == null || startCooldown(player, "dash", 10000))
			return;
		Location target = player.getLocation();
		Vector direction = target.getDirection().normalize();
		for(int i = 0; i < 10; i++)
		{
			Location next = target.clone().add(direction);
			if(next.getBlock().getType().isSolid() || next.clone().add(0, 1, 0).getBlock().getType().isSolid())
				break;
			target = next;
		}
		player.teleport(target);
	}
}
