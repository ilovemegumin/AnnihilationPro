package vip.megumin.anniPro.main;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import vip.megumin.anniPro.itemMenus.ActionMenuItem;
import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.ItemClickHandler;
import vip.megumin.anniPro.itemMenus.ItemMenu;
import vip.megumin.anniPro.itemMenus.MenuItem;
import vip.megumin.anniPro.itemMenus.ItemMenu.Size;

public class AnniCommand
{
	private static boolean reigstered = false;
	private static Map<String,AnniArgument> arguments;
	private static ItemMenu menu;
	
	public static void register(JavaPlugin plugin)
	{
		if(!reigstered)
		{
			reigstered = true;
			arguments = new TreeMap<String,AnniArgument>();
			recalcItemMenu();
			plugin.getCommand("Anni").setExecutor(new Executor());
			registerArgument(new AnniArgument(){
				@Override
				public String getHelp()
				{
					return ChatColor.RED+"Help--"+ChatColor.GREEN+"Returns the help for the /Anni command";
				}

				@Override
				public boolean useByPlayerOnly()
				{
					return false;
				}

				@Override
				public String getArgumentName()
				{
					return "Help";
				}

				@Override
				public void executeCommand(CommandSender sender, String label, String[] args)
				{
					sendHelp(sender);
				}

				@Override
				public String getPermission()
				{
					return null;
				}

				@Override
				public MenuItem getMenuItem()
				{
					return new ActionMenuItem("Show Anni Help",new ItemClickHandler(){
						@Override
						public void onItemClick(ItemClickEvent event)
						{
							executeCommand(event.getPlayer(),null,null);
							event.setWillClose(true);
						}},new ItemStack(Material.BOOK),ChatColor.GREEN+"Click to show help for the /Anni command");
				}});
		}
	}
	
	private static void recalcItemMenu()
	{
		menu = new ItemMenu("Annihilation Command Menu",arguments.isEmpty() ? Size.ONE_LINE : Size.fit(arguments.size()));
		int x = 0;
		for(AnniArgument arg : arguments.values())
		{
			if(arg.getMenuItem() != null)
			{
				menu.setItem(x, arg.getMenuItem());
				x++;
			}
		}
	}
	
	public static void registerArgument(AnniArgument argument)
	{
		if(argument != null)
		{
            if(argument.getPermission() != null)
            {
                Permission perm = Bukkit.getPluginManager().getPermission(argument.getPermission());
                if(perm == null)
                {
                    try
                    {
                        perm = new Permission(argument.getPermission());
                        Bukkit.getPluginManager().addPermission(perm);
                    }
                    catch (IllegalArgumentException ignored)
                    {
                        // Already registered (e.g. from plugin.yml) - retrieve it
                        perm = Bukkit.getPluginManager().getPermission(argument.getPermission());
                    }
                }
                if(perm != null)
                {
                    perm.recalculatePermissibles();
                }
            }
			arguments.put(argument.getArgumentName().toLowerCase(), argument);
			recalcItemMenu();
		}
	}
	
	private static void sendHelp(CommandSender sender)
	{
		if(arguments.isEmpty())
			sender.sendMessage(ChatColor.RED+"There are currently no registered arguments for the /Anni command!");
		else
		{
			for(AnniArgument arg : arguments.values())
				sender.sendMessage(arg.getHelp());
		}
	}
	
	private static void openMenu(Player player)
	{
		recalcItemMenu();
		menu.open(player);
	}
	
	private static class Executor implements CommandExecutor
	{
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
		{
			if(!(sender instanceof Player) || sender.hasPermission("A.anni"))
			{
				if(args.length == 0)
				{
					if(sender instanceof Player)
						openMenu((Player)sender);
					else sender.sendMessage(ChatColor.RED+"You must be a player to use /Anni item menu.");
				}
				else
				{
					AnniArgument arg = arguments.get(args[0].toLowerCase());
					if(arg != null)
					{
						if(arg.getPermission() != null && !sender.hasPermission(arg.getPermission()))
						{
							sender.sendMessage(ChatColor.RED+"You do not have permission to use /"+label+" "+arg.getArgumentName()+".");
							return true;
						}
						if(arg.useByPlayerOnly())
						{
							if(!(sender instanceof Player))
							{
								sender.sendMessage(ChatColor.RED+"The argument "+ChatColor.GOLD+arg.getArgumentName()+ChatColor.RED+" must be used by a player.");
								return true;
							}
						}
						arg.executeCommand(sender, label, excludeFirstArgument(args));
					}
					else sendHelp(sender);
				}
			}
			else sender.sendMessage(ChatColor.RED+"You do not have permission to use this command!");
			return true;
		}
		
		private String[] excludeFirstArgument(String[] args)
		{
			String[] r = new String[args.length-1];
			if(r.length == 0)
				return r;
            System.arraycopy(args, 1, r, 0, args.length - 1);
			return r;
		}		
	}
}
