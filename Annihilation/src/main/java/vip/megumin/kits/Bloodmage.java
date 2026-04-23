package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public class Bloodmage extends AnniKitBase
{
	public Bloodmage()
	{
		super("Bloodmage", Material.FERMENTED_SPIDER_EYE, url("Bloodmage"),
				lore("You are the blood.", "Poison enemies through combat", "and corrupt nearby rushers."),
				stoneSword(), woodPick(), woodAxe(), named(Material.FERMENTED_SPIDER_EYE, "Corrupt"), named(Material.NETHERRACK, "Bloodcursed Terraform"));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void poison(EntityDamageByEntityEvent event)
	{
		Player damager = getDamagingPlayer(event);
		if(damager != null && event.getEntity() instanceof Player && getCurrentPlayer(damager) != null && random.nextInt(100) < 25)
			((Player)event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void cast(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p == null || !event.getAction().name().contains("RIGHT"))
			return;
		if(namedLike(event.getItem(), "Corrupt"))
		{
			if(startCooldown(player, "corrupt", 60000))
				return;
			for(Player target : enemiesNear(player, p, 4))
			{
				target.damage(3.0, player);
				target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
			}
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Terraform"))
		{
			if(startCooldown(player, "terraform", 120000))
				return;
			for(Player target : enemiesNear(player, p, 8))
			{
				target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
				target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 2));
			}
			event.setCancelled(true);
		}
	}
}
