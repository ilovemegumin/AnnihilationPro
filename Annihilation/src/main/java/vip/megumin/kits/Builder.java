package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;

public class Builder extends AnniKitBase
{
	public Builder()
	{
		super("Builder", Material.BRICK, url("Builder"),
				lore("You are the wall.", "Resource Drop and Replication Cache", "supply blocks for defenses."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.BOOK, "Resource Drop"), named(Material.CHEST, "Replication Cache"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) == null || !event.getAction().name().contains("RIGHT"))
			return;
		if(namedLike(event.getItem(), "Resource Drop"))
		{
			if(startCooldown(player, "drop", 90000))
				return;
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.WOOD, 64)));
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.DIRT, 48)));
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.STONE, 32)));
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.BRICK, 16)));
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.WOOL, 16)))
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Replication Cache"))
		{
			if(startCooldown(player, "cache", 45000))
				return;
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.WOOD, 64)));
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.STONE, 64)));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void xp(BlockBreakEvent event)
	{
		if(getCurrentPlayer(event.getPlayer()) != null)
			event.getPlayer().giveExp(2);
	}
}
