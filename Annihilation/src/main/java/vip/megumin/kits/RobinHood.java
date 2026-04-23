package vip.megumin.kits;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import vip.megumin.anniPro.main.AnnihilationMain;

public class RobinHood extends AnniKitBase
{
	private final HashSet<UUID> aerialGrace = new HashSet<UUID>();

	public RobinHood()
	{
		super("Robin Hood", Material.SADDLE, url("Robin_Hood"),
				lore("You are the rider.", "Summon a steed and use", "Aerial Agility to negate falls."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.SADDLE, "Steed"), named(Material.PAPER, "Aerial Agility"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void use(PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();
		if(getCurrentPlayer(player) == null || !event.getAction().name().contains("RIGHT"))
			return;
		if(namedLike(event.getItem(), "Steed"))
		{
			if(startCooldown(player, "steed", 80000))
				return;
			Horse horse = (Horse)player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
			horse.setTamed(true);
			horse.setOwner(player);
			horse.setMaxHealth(30.0);
			horse.setHealth(30.0);
			horse.setPassenger(player);
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Aerial"))
		{
			aerialGrace.add(player.getUniqueId());
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1));
			new BukkitRunnable(){
				@Override
				public void run()
				{
					aerialGrace.remove(player.getUniqueId());
				}
			}.runTaskLater(AnnihilationMain.getInstance(), 200);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void fall(EntityDamageEvent event)
	{
		if(event.getEntityType() == EntityType.PLAYER && event.getCause() == EntityDamageEvent.DamageCause.FALL && aerialGrace.contains(event.getEntity().getUniqueId()))
			event.setCancelled(true);
	}
}
