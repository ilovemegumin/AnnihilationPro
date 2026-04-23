package vip.megumin.kits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.itemMenus.ActionMenuItem;
import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.ItemClickHandler;
import vip.megumin.anniPro.itemMenus.ItemMenu;
import vip.megumin.anniPro.itemMenus.ItemMenu.Size;

public class Hunter extends AnniKitBase
{
	private final Map<Location, Trap> traps = new HashMap<Location, Trap>();
	private final Map<Location, UUID> owners = new HashMap<Location, UUID>();
	private final Map<UUID, Trap> selected = new HashMap<UUID, Trap>();
	private final Map<UUID, ItemMenu> menus = new HashMap<UUID, ItemMenu>();
	private final HashSet<Location> temporaryBlocks = new HashSet<Location>();

	public Hunter()
	{
		super("Hunter", Material.LEASH, url("Hunter"),
				lore("You are the snare.", "Choose and place traps", "that trigger when enemies step on them."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.LEASH, "Trap Snare"), named(Material.SNOW_BALL, "Flurry"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void trap(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) != null && event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Trap Snare"))
		{
			if(player.isSneaking())
				menu(player).open(player);
			else
				placeTrap(player);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void trigger(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Location location = player.getLocation().getBlock().getLocation();
		Trap trap = traps.get(location);
		UUID owner = owners.get(location);
		if(trap == null || owner == null)
			return;
		Player ownerPlayer = org.bukkit.Bukkit.getPlayer(owner);
		if(ownerPlayer == null)
			return;
		AnniPlayer ownerAnni = AnniPlayer.getPlayer(owner);
		if(ownerAnni == null || !isEnemy(ownerAnni, player))
			return;
		traps.remove(location);
		owners.remove(location);
		activate(trap, ownerPlayer, player, location);
	}

	private void placeTrap(Player player)
	{
		if(startCooldown(player, "trap", 40000))
			return;
		Trap trap = selected.get(player.getUniqueId());
		if(trap == null)
			trap = Trap.FREEZE;
		Location center = player.getLocation().getBlock().getLocation();
		for(int x = -1; x <= 1; x++)
			for(int z = -1; z <= 1; z++)
			{
				Location location = center.clone().add(x, 0, z);
				traps.put(location, trap);
				owners.put(location, player.getUniqueId());
			}
		player.sendMessage(ChatColor.GREEN+"Placed "+trap.display+" trap.");
	}

	private void activate(Trap trap, Player owner, Player target, Location location)
	{
		if(trap == Trap.FREEZE)
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1));
		else if(trap == Trap.BLAST)
		{
			target.damage(4.0, owner);
			target.setVelocity(target.getLocation().toVector().subtract(location.toVector()).normalize().multiply(1.8).setY(0.7));
		}
		else if(trap == Trap.DECAY)
			target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 160, 1));
		else if(trap == Trap.LEVITATION)
		{
			addTempBlock(location.getBlock(), Material.WEB, 200, temporaryBlocks);
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 10));
		}
	}

	private ItemMenu menu(final Player player)
	{
		ItemMenu menu = menus.get(player.getUniqueId());
		if(menu != null)
			return menu;
		menu = new ItemMenu("Hunter Traps", Size.ONE_LINE);
		addTrap(menu, 0, player, Trap.FREEZE, Material.ICE);
		addTrap(menu, 1, player, Trap.BLAST, Material.TNT);
		addTrap(menu, 2, player, Trap.DECAY, Material.FERMENTED_SPIDER_EYE);
		addTrap(menu, 3, player, Trap.LEVITATION, Material.WEB);
		menus.put(player.getUniqueId(), menu);
		return menu;
	}

	private void addTrap(ItemMenu menu, int slot, final Player player, final Trap trap, Material icon)
	{
		menu.setItem(slot, new ActionMenuItem(ChatColor.AQUA+trap.display, new ItemClickHandler(){
			@Override
			public void onItemClick(ItemClickEvent event)
			{
				selected.put(player.getUniqueId(), trap);
				event.setWillClose(true);
			}
		}, new ItemStack(icon), ChatColor.GRAY+"Select this trap."));
	}

	private enum Trap
	{
		FREEZE("Freeze"),
		BLAST("Blast"),
		DECAY("Decay"),
		LEVITATION("Levitation");

		private final String display;

		Trap(String display)
		{
			this.display = display;
		}
	}
}
