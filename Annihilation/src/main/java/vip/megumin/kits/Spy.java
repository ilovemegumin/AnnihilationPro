package vip.megumin.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import vip.megumin.anniPro.main.AnnihilationMain;

public class Spy extends AnniKitBase
{
	private final Map<UUID, ItemStack[]> hiddenArmor = new HashMap<UUID, ItemStack[]>();
	private final Map<UUID, Location> vanishStart = new HashMap<UUID, Location>();

	public Spy()
	{
		super("Spy", Material.POTION, url("Spy"),
				lore("You are the vanish.", "Sneak and stand still to hide", "including your armor."),
				goldSword(), woodPick(), woodAxe(), named(Material.POTION, "Flee"));
	}

	@Override
	public void cleanup(Player player)
	{
		reveal(player);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void sneak(PlayerToggleSneakEvent event)
	{
		final Player player = event.getPlayer();
		if(getCurrentPlayer(player) == null)
			return;
		if(event.isSneaking())
		{
			vanishStart.put(player.getUniqueId(), player.getLocation());
			new BukkitRunnable(){
				@Override
				public void run()
				{
					if(vanishStart.containsKey(player.getUniqueId()) && player.isSneaking() && getCurrentPlayer(player) != null)
					{
						hiddenArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());
						player.getInventory().setArmorContents(null);
						player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
					}
				}
			}.runTaskLater(AnnihilationMain.getInstance(), 40);
		}
		else
			reveal(player);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void move(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Location start = vanishStart.get(player.getUniqueId());
		if(getCurrentPlayer(player) != null && start != null && event.getTo().distanceSquared(start) > 9)
			reveal(player);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void damage(EntityDamageByEntityEvent event)
	{
		Player damager = getDamagingPlayer(event);
		if(damager != null && getCurrentPlayer(damager) != null && event.getEntity() instanceof Player && isBehind(damager, (Player)event.getEntity()))
			event.setDamage(event.getDamage() + 1.0);
		if(event.getEntity() instanceof Player && getCurrentPlayer((Player)event.getEntity()) != null)
			reveal((Player)event.getEntity());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void breakBlock(BlockBreakEvent event)
	{
		if(getCurrentPlayer(event.getPlayer()) != null)
			reveal(event.getPlayer());
	}

	private void reveal(Player player)
	{
		if(player == null)
			return;
		ItemStack[] armor = hiddenArmor.remove(player.getUniqueId());
		if(armor != null)
			player.getInventory().setArmorContents(armor);
		vanishStart.remove(player.getUniqueId());
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
	}
}
