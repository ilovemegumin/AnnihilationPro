package vip.megumin.kits;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Spider extends AnniKitBase
{
	private final HashSet<Location> webs = new HashSet<Location>();

	public Spider()
	{
		super("Spider", Material.WEB, url("Spider"),
				lore("You are the web.", "Throw cobwebs and climb", "while surviving fatal falls."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.VINE, "Wall Climb"), item(Material.WEB, 5));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) == null || !event.getAction().name().contains("RIGHT"))
			return;
		if(event.getItem() != null && event.getItem().getType() == Material.WEB)
		{
			Location center = player.getTargetBlock((HashSet<Byte>)null, 8).getLocation();
			for(int x = -1; x <= 1; x++)
				for(int z = -1; z <= 1; z++)
					addTempBlock(center.clone().add(x, 0, z).getBlock(), Material.WEB, 500, webs);
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Wall Climb"))
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void fall(EntityDamageEvent event)
	{
		if(event.getEntityType() == EntityType.PLAYER && event.getCause() == EntityDamageEvent.DamageCause.FALL)
		{
			Player player = (Player)event.getEntity();
			if(getCurrentPlayer(player) != null && event.getDamage() >= player.getHealth())
			{
				event.setDamage(Math.max(0, player.getHealth() - 2));
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 0));
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0));
			}
		}
	}
}
