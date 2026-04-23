package vip.megumin.kits;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class Hunter extends AnniKitBase
{
	private final HashSet<Location> traps = new HashSet<Location>();

	public Hunter()
	{
		super("Hunter", Material.LEASH, url("Hunter"),
				lore("You are the snare.", "Place traps to slow and trap enemies.", "Use Flurry for pressure."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.LEASH, "Trap Snare"), named(Material.SNOW_BALL, "Flurry"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void trap(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) != null && event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Trap Snare"))
		{
			if(startCooldown(player, "trap", 40000))
				return;
			Location center = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
			for(int x = -1; x <= 1; x++)
				for(int z = -1; z <= 1; z++)
					addTempBlock(center.clone().add(x, 1, z).getBlock(), Material.WEB, 80, traps);
			event.setCancelled(true);
		}
	}
}
