package vip.megumin.kits;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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

public class RiftWalker extends AnniKitBase
{
	public RiftWalker()
	{
		super("Rift Walker", Material.BLAZE_ROD, url("Rift_Walker"),
				lore("You are the rift.", "Open a delayed return rift", "to your team's spawn."),
				woodSword(), woodPick(), woodAxe(), named(Material.BLAZE_ROD, "Rift Rod"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void rift(PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();
		final AnniPlayer p = getCurrentPlayer(player);
		if(p != null && event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Rift"))
		{
			if(startCooldown(player, "rift", 30000))
				return;
			final Location start = player.getLocation();
			player.sendMessage(ChatColor.GREEN + "Rift opening in 10 seconds.");
			new BukkitRunnable(){
				@Override
				public void run()
				{
					if(getCurrentPlayer(player) == null || player.getLocation().distanceSquared(start) > 9 || p.getTeam() == null)
						return;
					Location spawn = p.getTeam().getRandomSpawn();
					if(spawn != null)
					{
						player.teleport(spawn);
						player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
					}
				}
			}.runTaskLater(AnnihilationMain.getInstance(), 200);
			event.setCancelled(true);
		}
	}
}
