package vip.megumin.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public class Mercenary extends AnniKitBase
{
	private final Map<UUID, Long> marks = new HashMap<UUID, Long>();

	public Mercenary()
	{
		super("Mercenary", Material.SKULL_ITEM, url("Mercenary"),
				lore("You are the mark.", "Mark enemies so they take", "extra pressure from your attacks."),
				woodSword(), woodPick(), woodAxe(), named(Material.SKULL_ITEM, "Mark"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void mark(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p != null && event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Mark"))
		{
			Player target = getTargetPlayer(player, 20);
			if(target != null && isEnemy(p, target))
			{
				marks.put(target.getUniqueId(), System.currentTimeMillis() + 30000);
				target.sendMessage(ChatColor.RED + "You have been marked!");
				player.sendMessage(ChatColor.GREEN + "Marked " + target.getName() + ".");
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void bonus(EntityDamageByEntityEvent event)
	{
		Player player = getDamagingPlayer(event);
		if(player != null && getCurrentPlayer(player) != null && event.getEntity() instanceof Player)
		{
			Long until = marks.get(event.getEntity().getUniqueId());
			if(until != null && until.longValue() > System.currentTimeMillis())
				event.setDamage(event.getDamage() + 1.0);
		}
	}
}
