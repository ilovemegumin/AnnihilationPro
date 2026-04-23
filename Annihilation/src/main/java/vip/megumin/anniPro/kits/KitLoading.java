package vip.megumin.anniPro.kits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import vip.megumin.kits.Acrobat;
import vip.megumin.kits.Archer;
import vip.megumin.kits.Assassin;
import vip.megumin.kits.AnniClassCatalog;
import vip.megumin.kits.Berserker;
import vip.megumin.kits.Defender;
import vip.megumin.kits.Enchanter;
import vip.megumin.kits.Lumberjack;
import vip.megumin.kits.Miner;
import vip.megumin.kits.Pyro;
import vip.megumin.kits.Scorpio;
import vip.megumin.kits.Scout;
import vip.megumin.kits.Succubus;
import vip.megumin.kits.Swapper;
import vip.megumin.kits.Thor;
import vip.megumin.kits.Transporter;
import vip.megumin.kits.Vampire;
import vip.megumin.kits.Warrior;
import vip.megumin.anniPro.main.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.anniGame.AnniTeam;
import vip.megumin.anniPro.anniGame.Game;
import vip.megumin.anniPro.itemMenus.ItemMenu;
import vip.megumin.anniPro.itemMenus.ItemMenu.Size;
import vip.megumin.anniPro.main.AnnihilationMain;
import vip.megumin.anniPro.utils.Loc;

public class KitLoading implements Listener, CommandExecutor
{
	private final KitMenuItem[] items;
	private final Map<UUID,ItemMenu> menus;
	
	public KitLoading(final JavaPlugin p)
	{
		redcompass = ChatColor.RED+Lang.COMPASSTEXT.toString()+" "+AnniTeam.Red.getExternalName()+"'s Nexus";
		bluecompass = ChatColor.BLUE+Lang.COMPASSTEXT.toString()+" "+AnniTeam.Blue.getExternalName()+"'s Nexus";
		greencompass = ChatColor.GREEN+Lang.COMPASSTEXT.toString()+" "+AnniTeam.Green.getExternalName()+"'s Nexus";
		yellowcompass = ChatColor.YELLOW+Lang.COMPASSTEXT.toString()+" "+AnniTeam.Yellow.getExternalName()+"'s Nexus";

		Bukkit.getPluginManager().registerEvents(this, p);
		p.getCommand("Kit").setExecutor(this);
		
		menus = new HashMap<UUID,ItemMenu>();
		registerBuiltInKits(p);

		Collection<Kit> kits = Kit.getKits();
		items = new KitMenuItem[kits.size()];
		int counter = 0;
		Iterator<Kit> it = kits.iterator();
		while(it.hasNext())
		{
			items[counter] = new KitMenuItem(it.next());
			counter++;
		}
	}

	@SuppressWarnings("unchecked")
	private void registerBuiltInKits(JavaPlugin plugin)
	{
		Class<? extends Kit>[] builtIns = new Class[]
		{
			Acrobat.class,
			Archer.class,
			Assassin.class,
			Berserker.class,
			Defender.class,
			Enchanter.class,
			Lumberjack.class,
			Miner.class,
			Pyro.class,
			Scorpio.class,
			Scout.class,
			Succubus.class,
			Swapper.class,
			Thor.class,
			Transporter.class,
			Vampire.class,
			Warrior.class
		};

		for (Class<? extends Kit> kitClass : builtIns)
		{
			try
			{
				Kit kit = kitClass.newInstance();
				registerKit(plugin, kit);
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				throw new IllegalStateException("Unable to initialize kit: " + kitClass.getName(), e);
			}
		}

		for(Kit kit : AnniClassCatalog.createMissingKits())
			registerKit(plugin, kit);
	}

	private void registerKit(JavaPlugin plugin, Kit kit)
	{
		if (kit.Initialize())
		{
			Bukkit.getPluginManager().registerEvents(kit, plugin);
			Kit.registerKit(kit);
			Bukkit.getLogger().info("[Annihilation] --" + kit.getName());
		}
	}
	
	public void openKitMap(Player player)
	{
		refreshMenu(player).open(player);
	}
	
	private ItemMenu refreshMenu(Player player)
	{
		ItemMenu menu = menus.get(player.getUniqueId());
		if(menu == null)
		{
			menu = new ItemMenu(player.getName()+"'s Kits",Size.fit(items.length));
			for(int x = 0; x < items.length; x++)
				menu.setItem(x, items[x]);
			menus.put(player.getUniqueId(), menu);
		}
		return menu;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void ClassChanger(final PlayerPortalEvent event)
	{
		if(Game.isGameRunning() && event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			AnniPlayer p = AnniPlayer.getPlayer(event.getPlayer().getUniqueId());
			if(p != null)
			{
				event.setCancelled(true);
				if(p.getTeam() != null)
				{
					final Player pl = event.getPlayer();
					pl.teleport(p.getTeam().getRandomSpawn());
					Bukkit.getScheduler().runTaskLater(AnnihilationMain.getInstance(), new Runnable(){

						@Override
						public void run()
						{
							openKitMap(pl);
						}}, 40);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void openKitMenuCheck(PlayerInteractEvent event)
	{
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
		{
			final Player player = event.getPlayer();
			if(KitUtils.itemHasName(player.getItemInHand(), CustomItem.KITMAP.getName()))
			{
				openKitMap(player);
				event.setCancelled(true);
			}
		}
	}

	private String redcompass,bluecompass,greencompass,yellowcompass;
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void navCompassCheck(PlayerInteractEvent event)
	{
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
		{
			final Player player = event.getPlayer();
			ItemStack item = player.getItemInHand();
			String name = null;
			Loc target = null;
			if(KitUtils.itemHasName(item, CustomItem.NAVCOMPASS.getName()))
			{
				name = redcompass;
				target = AnniTeam.Red.getNexus().getLocation();
			}
			else if(KitUtils.itemHasName(item, redcompass))
			{
				name = bluecompass;
				target = AnniTeam.Blue.getNexus().getLocation();
			}
			else if(KitUtils.itemHasName(item, bluecompass))
			{
				name = greencompass;
				target = AnniTeam.Green.getNexus().getLocation();
			}
			else if(KitUtils.itemHasName(item, greencompass))
			{
				name = yellowcompass;
				target = AnniTeam.Yellow.getNexus().getLocation();
			}
			else if(KitUtils.itemHasName(item, yellowcompass))
			{
				name = redcompass;
				target = AnniTeam.Red.getNexus().getLocation();
			}
			
			if(name != null && target != null)
			{
				ItemMeta m = item.getItemMeta();
				m.setDisplayName(name);
				item.setItemMeta(m);
				player.setCompassTarget(target.toLocation());
			}
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void StopDrops(PlayerDropItemEvent event)
	{
	    Player player = event.getPlayer();
	    Item item = event.getItemDrop();
	    if(item != null)
	    {
		    ItemStack stack = item.getItemStack();
		    if(stack != null)
		    {
			    if(KitUtils.isSoulbound(stack))
			    {
				    player.playSound(player.getLocation(), Sound.BLAZE_HIT, 1.0F, 0.3F);
				    event.getItemDrop().remove();
			    }
		    }
	    }
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void RemoveDeathDrops(PlayerDeathEvent event)
	{

		
		for(ItemStack s : new ArrayList<ItemStack>(event.getDrops()))
		{
			if(KitUtils.isSoulbound(s))
				event.getDrops().remove(s);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void StopClicking(InventoryClickEvent event)
	{
	    HumanEntity entity = event.getWhoClicked();
	    ItemStack stack = event.getCurrentItem();
	    InventoryType top = event.getView().getTopInventory().getType();
	    
	    if (stack != null && (entity instanceof Player)) 
	    {
	    	if (top == InventoryType.PLAYER || top == InventoryType.WORKBENCH || top == InventoryType.CRAFTING) 
	    		return;

	    	if(KitUtils.isSoulbound(stack))
	          event.setCancelled(true); 
	    }
	 }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(player.hasPermission("Anni.ChangeKit"))
			{
				this.openKitMap(player);
				return true;
			}
		}
		return false;
	}
}
