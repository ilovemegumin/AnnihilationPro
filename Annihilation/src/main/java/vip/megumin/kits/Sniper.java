package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

public class Sniper extends AnniKitBase
{
	public Sniper()
	{
		super("Sniper", Material.ARROW, url("Sniper"),
				lore("You are the shot.", "Your bow fires fast and flat", "for long-range picks."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.BOW, "Compound Bow"), item(Material.ARROW, 32));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void shoot(ProjectileLaunchEvent event)
	{
		if(event.getEntity() instanceof Arrow)
		{
			ProjectileSource source = event.getEntity().getShooter();
			if(source instanceof Player && getCurrentPlayer((Player)source) != null)
				event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(3.0));
		}
	}
}
