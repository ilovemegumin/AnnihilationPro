package vip.megumin.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ninja extends AnniKitBase
{
	private final Map<UUID, UUID> shurikens = new HashMap<UUID, UUID>();

	public Ninja()
	{
		super("Ninja", Material.FIREWORK_CHARGE, url("Ninja"),
				lore("You are the shadow.", "Use smoke, shuriken and ascension", "to escape or finish enemies."),
				goldSword(), woodPick(), woodAxe(), named(Material.SULPHUR, "Smoke Bomb"), named(Material.NETHER_STAR, "Shuriken"), named(Material.FIREWORK_CHARGE, "Masterful Ascension"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) == null || !event.getAction().name().contains("RIGHT"))
			return;
		if(namedLike(event.getItem(), "Smoke"))
		{
			if(startCooldown(player, "smoke", 40000))
				return;
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1));
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Shuriken"))
		{
			Snowball snowball = player.launchProjectile(Snowball.class);
			shurikens.put(snowball.getUniqueId(), player.getUniqueId());
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Ascension"))
		{
			if(player.hasPotionEffect(PotionEffectType.JUMP))
				player.removePotionEffect(PotionEffectType.JUMP);
			else
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void hit(ProjectileHitEvent event)
	{
		UUID owner = shurikens.remove(event.getEntity().getUniqueId());
		if(owner != null && org.bukkit.Bukkit.getPlayer(owner) != null)
			for(org.bukkit.entity.Entity entity : event.getEntity().getNearbyEntities(2, 2, 2))
				if(entity instanceof Player)
					((Player)entity).damage(4.0, org.bukkit.Bukkit.getPlayer(owner));
	}
}
