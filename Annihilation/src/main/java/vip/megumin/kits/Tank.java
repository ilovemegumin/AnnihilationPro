package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.main.AnnihilationMain;

public class Tank extends AnniKitBase
{
	public Tank()
	{
		super("Tank", Material.IRON_CHESTPLATE, url("Tank"),
				lore("You are the shield.", "Charge through enemies and", "blind targets you collide with."),
				stoneSword(), woodPick(), woodAxe(), woodShovel(), named(Material.IRON_CHESTPLATE, "Defensive Shield"), named(Material.PRISMARINE_SHARD, "Shield Charge"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void charge(PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();
		final AnniPlayer p = getCurrentPlayer(player);
		if(p != null && event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Shield Charge"))
		{
			if(startCooldown(player, "charge", 60000))
				return;
			player.setVelocity(player.getLocation().getDirection().normalize().multiply(1.8).setY(0.2));
			new BukkitRunnable(){
				int ticks = 0;
				@Override
				public void run()
				{
					if(ticks++ > 20 || getCurrentPlayer(player) == null)
					{
						cancel();
						return;
					}
					for(Player target : enemiesNear(player, p, 2))
					{
						target.damage(2.5, player);
						target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
					}
				}
			}.runTaskTimer(AnnihilationMain.getInstance(), 0, 2);
			event.setCancelled(true);
		}
	}
}
