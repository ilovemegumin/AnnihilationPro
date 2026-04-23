package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Tinkerer extends AnniKitBase
{
	public Tinkerer()
	{
		super("Tinkerer", Material.REDSTONE_BLOCK, url("Tinkerer"),
				lore("You are the pad.", "PowerPads grant buffs", "to teammates who step on them."),
				stoneSword(), woodPick(), woodAxe(), named(Material.COAL_BLOCK, "Haste PowerPad"), named(Material.REDSTONE_BLOCK, "Speed PowerPad"), item(Material.BOOK, 10));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void pad(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) == null)
			return;
		Material below = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
		if(below == Material.REDSTONE_BLOCK)
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 900, 0));
		else if(below == Material.COAL_BLOCK)
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 900, 0));
		else if(below == Material.DIAMOND_BLOCK)
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1));
		else if(below == Material.GOLD_BLOCK)
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 300, 1));
		else if(below == Material.EMERALD_BLOCK)
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 0));
	}
}
