package vip.megumin.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.itemMenus.ActionMenuItem;
import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.ItemClickHandler;
import vip.megumin.anniPro.itemMenus.ItemMenu;
import vip.megumin.anniPro.itemMenus.ItemMenu.Size;
import vip.megumin.anniPro.main.AnnihilationMain;

public class Bard extends AnniKitBase
{
	private final Map<UUID, Location> buffboxes = new HashMap<UUID, Location>();
	private final Map<Location, UUID> owners = new HashMap<Location, UUID>();
	private final Map<UUID, Song> songs = new HashMap<UUID, Song>();
	private final Map<UUID, ItemMenu> menus = new HashMap<UUID, ItemMenu>();

	public Bard()
	{
		super("Bard", Material.JUKEBOX, url("Bard"),
				lore("You are the song.", "Your Buffbox grants nearby buffs", "and weakens enemies around you."),
				woodSword(), woodPick(), woodAxe(), named(Material.JUKEBOX, "Buffbox"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playSong(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p == null)
			return;
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && owners.containsKey(event.getClickedBlock().getLocation()))
		{
			UUID owner = owners.get(event.getClickedBlock().getLocation());
			if(owner.equals(player.getUniqueId()))
				menu(player).open(player);
			event.setCancelled(true);
			return;
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && namedLike(event.getItem(), "Buffbox"))
		{
			placeBuffbox(player, event.getClickedBlock().getRelative(BlockFace.UP));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void breakBuffbox(BlockBreakEvent event)
	{
		UUID owner = owners.remove(event.getBlock().getLocation());
		if(owner == null)
			return;
		buffboxes.remove(owner);
		event.getBlock().setType(Material.AIR);
		Player player = org.bukkit.Bukkit.getPlayer(owner);
		if(player != null)
			player.sendMessage(ChatColor.RED+"Your Buffbox was broken.");
	}

	@Override
	public void cleanup(Player player)
	{
		if(player != null)
			removeBuffbox(player.getUniqueId());
	}

	private void placeBuffbox(final Player player, Block block)
	{
		if(block.getType() != Material.AIR)
			return;
		if(startCooldown(player, "place", 10000))
			return;
		removeBuffbox(player.getUniqueId());
		block.setType(Material.JUKEBOX);
		buffboxes.put(player.getUniqueId(), block.getLocation());
		owners.put(block.getLocation(), player.getUniqueId());
		songs.put(player.getUniqueId(), Song.INVIGORATE);
		new BukkitRunnable(){
			@Override
			public void run()
			{
				Location location = buffboxes.get(player.getUniqueId());
				if(location == null || location.getBlock().getType() != Material.JUKEBOX || getCurrentPlayer(player) == null)
				{
					if(location != null)
						owners.remove(location);
					buffboxes.remove(player.getUniqueId());
					cancel();
					return;
				}
				pulse(player, location);
			}
		}.runTaskTimer(AnnihilationMain.getInstance(), 20, 40);
		menu(player).open(player);
	}

	private void removeBuffbox(UUID player)
	{
		Location location = buffboxes.remove(player);
		if(location != null)
		{
			owners.remove(location);
			if(location.getBlock().getType() == Material.JUKEBOX)
				location.getBlock().setType(Material.AIR);
		}
	}

	private void pulse(Player player, Location location)
	{
		AnniPlayer p = getCurrentPlayer(player);
		if(p == null)
			return;
		location.getWorld().playEffect(location.clone().add(0.5, 1.0, 0.5), Effect.NOTE, 1);
		Song song = songs.get(player.getUniqueId());
		if(song == null)
			song = Song.INVIGORATE;
		if(song.targetAllies)
		{
			for(Player ally : alliesNear(location, p, 15))
				ally.addPotionEffect(new PotionEffect(song.effect, song.durationTicks, song.amplifier));
		}
		else
		{
			for(Player enemy : enemiesNear(location, p, 15))
				enemy.addPotionEffect(new PotionEffect(song.effect, song.durationTicks, song.amplifier));
		}
	}

	private java.util.List<Player> alliesNear(Location center, AnniPlayer anniPlayer, int radius)
	{
		java.util.List<Player> players = new java.util.ArrayList<Player>();
		for(org.bukkit.entity.Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius))
			if(entity instanceof Player && isAlly(anniPlayer, (Player)entity))
				players.add((Player)entity);
		return players;
	}

	private ItemMenu menu(final Player player)
	{
		ItemMenu menu = menus.get(player.getUniqueId());
		if(menu != null)
			return menu;
		menu = new ItemMenu("Bard Songs", Size.ONE_LINE);
		addSong(menu, 0, player, Song.INVIGORATE, Material.RED_ROSE);
		addSong(menu, 1, player, Song.ENLIGHTEN, Material.SUGAR);
		addSong(menu, 2, player, Song.INTIMIDATE, Material.BONE);
		addSong(menu, 3, player, Song.SHACKLE, Material.WEB);
		menu.setItem(8, new ActionMenuItem(ChatColor.RED+"Recall Buffbox", new ItemClickHandler(){
			@Override
			public void onItemClick(ItemClickEvent event)
			{
				removeBuffbox(player.getUniqueId());
				startCooldown(player, "place", 10000);
				event.setWillClose(true);
			}
		}, new ItemStack(Material.REDSTONE_BLOCK), ChatColor.GRAY+"Remove your active Buffbox."));
		menus.put(player.getUniqueId(), menu);
		return menu;
	}

	private void addSong(ItemMenu menu, int slot, final Player player, final Song song, Material icon)
	{
		menu.setItem(slot, new ActionMenuItem(song.display, new ItemClickHandler(){
			@Override
			public void onItemClick(ItemClickEvent event)
			{
				if(startCooldown(player, "song:"+song.name(), 30000))
				{
					event.setWillUpdate(true);
					return;
				}
				songs.put(player.getUniqueId(), song);
				event.setWillClose(true);
			}
		}, new ItemStack(icon), song.description));
	}

	private enum Song
	{
		INVIGORATE(ChatColor.GREEN+"Invigorate", true, PotionEffectType.REGENERATION, 0, 400, ChatColor.GRAY+"Teammates: Regeneration I"),
		ENLIGHTEN(ChatColor.AQUA+"Enlighten", true, PotionEffectType.SPEED, 0, 500, ChatColor.GRAY+"Teammates: Speed I"),
		INTIMIDATE(ChatColor.RED+"Intimidate", false, PotionEffectType.WEAKNESS, 2, 400, ChatColor.GRAY+"Enemies: Weakness III"),
		SHACKLE(ChatColor.DARK_PURPLE+"Shackle", false, PotionEffectType.SLOW, 1, 300, ChatColor.GRAY+"Enemies: Slowness II");

		private final String display;
		private final boolean targetAllies;
		private final PotionEffectType effect;
		private final int amplifier;
		private final int durationTicks;
		private final String description;

		Song(String display, boolean targetAllies, PotionEffectType effect, int amplifier, int durationTicks, String description)
		{
			this.display = display;
			this.targetAllies = targetAllies;
			this.effect = effect;
			this.amplifier = amplifier;
			this.durationTicks = durationTicks;
			this.description = description;
		}
	}
}
