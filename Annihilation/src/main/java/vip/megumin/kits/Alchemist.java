package vip.megumin.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;

public class Alchemist extends AnniKitBase
{
	private final Map<UUID, Location> stands = new HashMap<UUID, Location>();
	private final Map<Location, UUID> owners = new HashMap<Location, UUID>();

	public Alchemist()
	{
		super("Alchemist", Material.BREWING_STAND_ITEM, url("Alchemist"),
				lore("You are the brew.", "Use your private stand and tome", "to prepare stronger potion rushes."),
				woodSword(), woodPick(), woodAxe(), named(Material.BREWING_STAND_ITEM, "Alchemist's Brewing Stand"), named(Material.BOOK, "Alchemist's Tome"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void useTome(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p == null)
			return;
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BREWING_STAND && owners.containsKey(event.getClickedBlock().getLocation()))
		{
			UUID owner = owners.get(event.getClickedBlock().getLocation());
			if(!owner.equals(player.getUniqueId()))
			{
				player.sendMessage(ChatColor.RED+"This brewing stand belongs to another Alchemist.");
				event.setCancelled(true);
			}
			return;
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && namedLike(event.getItem(), "Brewing Stand"))
		{
			placeStand(player, event.getClickedBlock().getRelative(BlockFace.UP));
			event.setCancelled(true);
		}
		else if(event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Tome"))
		{
			if(startCooldown(player, "tome", 90000))
				return;
			Material[] materials = new Material[]{Material.NETHER_STALK, Material.SPECKLED_MELON, Material.SUGAR, Material.SPIDER_EYE, Material.GLOWSTONE_DUST, Material.BLAZE_POWDER};
			for(int i = 0; i < 3; i++)
				player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(materials[random.nextInt(materials.length)], 1 + random.nextInt(3))));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void breakStand(BlockBreakEvent event)
	{
		UUID owner = owners.remove(event.getBlock().getLocation());
		if(owner != null)
		{
			stands.remove(owner);
			event.getBlock().setType(Material.AIR);
		}
	}

	@Override
	public void cleanup(Player player)
	{
		if(player == null)
			return;
		Location location = stands.remove(player.getUniqueId());
		if(location != null)
		{
			owners.remove(location);
			if(location.getBlock().getType() == Material.BREWING_STAND)
				location.getBlock().setType(Material.AIR);
		}
	}

	private void placeStand(Player player, Block block)
	{
		if(block.getType() != Material.AIR)
			return;
		cleanup(player);
		block.setType(Material.BREWING_STAND);
		stands.put(player.getUniqueId(), block.getLocation());
		owners.put(block.getLocation(), player.getUniqueId());
	}
}
