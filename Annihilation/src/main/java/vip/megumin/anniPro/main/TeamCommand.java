package vip.megumin.anniPro.main;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vip.megumin.imagetomsg.ImageChar;
import vip.megumin.imagetomsg.ImageMessage;
import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.anniGame.AnniTeam;
import vip.megumin.anniPro.anniGame.Game;
import vip.megumin.anniPro.anniGame.GameVars;
import vip.megumin.anniPro.itemMenus.ActionMenuItem;
import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.ItemClickHandler;
import vip.megumin.anniPro.itemMenus.ItemMenu;
import vip.megumin.anniPro.itemMenus.ItemMenu.Size;
import vip.megumin.anniPro.kits.CustomItem;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.utils.ResourceImages;

public class TeamCommand implements CommandExecutor, Listener
{
	private final ItemMenu menu;
	private final Map<ChatColor,ImageMessage> messages;
	public TeamCommand(JavaPlugin plugin)
	{
		menu = new ItemMenu("Join a Team",Size.ONE_LINE);
		messages = new EnumMap<ChatColor,ImageMessage>(ChatColor.class);
		plugin.getCommand("Team").setExecutor(this);
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		int x = 0;
		for(final AnniTeam team : AnniTeam.Teams)
		{
			BufferedImage image = ResourceImages.read(team.getName()+"Team.png");
			if(image != null)
			{
				String[] lore = new String[]
				{
					"",
					"",
					"",
					"",
					Lang.JOINTEAM.toString(),
					team.getExternalColoredName()+" "+Lang.TEAM,
				};
				ImageMessage message =  new ImageMessage(image, 10, ImageChar.MEDIUM_SHADE.getChar()).appendText(lore);
				messages.put(team.getColor(), message);
			}
			
			byte datavalue;
			if(team.equals(AnniTeam.Red))
				datavalue = (byte)14;
			else if(team.equals(AnniTeam.Blue))
				datavalue = (byte)11;
			else if(team.equals(AnniTeam.Green))
				datavalue = (byte)13;
			else
				datavalue = (byte)4;
			
			ActionMenuItem item = new ActionMenuItem(team.getExternalColoredName(),new ItemClickHandler(){
				@Override
				public void onItemClick(ItemClickEvent event)
				{
					event.getPlayer().performCommand("Team "+team.getName());
					event.setWillClose(true);
				}},new ItemStack(Material.WOOL,0,datavalue));
			menu.setItem(x, item);
			x++;
		}
		menu.setItem(4, new ActionMenuItem(ChatColor.AQUA+"Leave a Team",new ItemClickHandler(){
			@Override
			public void onItemClick(ItemClickEvent event)
			{
				event.getPlayer().performCommand("Team Leave");
				event.setWillClose(true);
			}},new ItemStack(Material.WOOL)));
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void voteGUIcheck(PlayerInteractEvent event)
	{
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
		{
			final Player player = event.getPlayer();
			if(KitUtils.itemHasName(player.getItemInHand(), CustomItem.TEAMMAP.getName()))
			{
				if(menu != null)
					menu.open(player);
				event.setCancelled(true);
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(sender instanceof Player)
		{
			final AnniPlayer p = AnniPlayer.getPlayer(((Player) sender).getUniqueId());
			if(args.length == 0) //checking the amount of players on a team
			{
				String[] messages = new String[8];
				for(int x = 0; x < 4; x++)
				{
					AnniTeam t = AnniTeam.Teams[x];
					int cat = x*2;
					messages[cat] = t.getColor()+"/Team "+t.getExternalName()+":";
					int y = t.getPlayerCount();
					messages[cat+1] = t.getColor()+Lang.TEAMCHECK.toStringReplacement(y, t.getExternalName());
				}
				sender.sendMessage(messages);
			}
			else if (args.length == 1) //Joining or leaving a team
			{
				if(args[0].equalsIgnoreCase("leave"))
				{
					if(!Game.isGameRunning())
					{
						if(p.getTeam() != null)
						{
							sender.sendMessage(Lang.LEAVETEAM.toStringReplacement(p.getTeam().getExternalColoredName()));
							p.getTeam().leaveTeam(p);
						}
						else sender.sendMessage(Lang.NOTEAM.toString());
					}
					else sender.sendMessage(Lang.CANNOTLEAVE.toString());
				}
				else
				{
					AnniTeam t = AnniTeam.getTeamByName(args[0]);
					if(t != null)
					{
						this.joinCheck(t, p);
					}
					else
						sender.sendMessage(Lang.INVALIDTEAM.toString());
				}
			}
			else sender.sendMessage(Lang.TEAMHELP.toString());
			
		}
		else sender.sendMessage("This command can only be used by a player!");
		return true;
	}
	
	private void joinCheck(AnniTeam team, AnniPlayer p)
	{
		if(p != null && team != null)
		{
			Object obj = p.getData("TeamDelay");
			if(obj == null || System.currentTimeMillis() >= (long)obj)
			{
				p.setData("TeamDelay", System.currentTimeMillis()+1000);
				Player player = p.getPlayer();
				if(p.getTeam() == null)
				{
					if(team.isTeamDead())
					{
						player.sendMessage(Lang.DESTROYEDTEAM.toString());
						return;
					}
					
					if(Game.getGameMap() != null)
					{
						int phase = Game.getGameMap().getCurrentPhase();
						if(phase > 2)
						{
							boolean allowed = false;
							for(int x = phase; x < 6; x++)
							{
								if(player.hasPermission("Anni.JoinPhase."+x))
								{
									allowed = true;
									break;
								}
							}
							
							if(!allowed)
							{
								player.sendMessage(Lang.WRONGPHASE.toString());
								return;
							}
						}
					}
					
					if(player.hasPermission("Anni.BypassJoin"))
					{
						joinTeam(p,team);
						return;
					}		
						
					if(GameVars.useTeamBalance())
					{
						int currentTeamsPlayers = team.getPlayerCount();
						int smallest = Integer.MAX_VALUE;
						for(int x = 0; x < 4; x++)
						{
							AnniTeam t = AnniTeam.Teams[x];
							if(!t.isTeamDead() && t.getPlayerCount() < smallest)
								smallest = t.getPlayerCount();

						}

						if(currentTeamsPlayers - smallest > GameVars.getBalanceTolerance())
						{
							player.sendMessage(Lang.JOINANOTHERTEAM.toString());
							return;
						}
					}
					
					joinTeam(p,team);
				}
				else player.sendMessage(Lang.ALREADYHAVETEAM.toString());
			}
		}
	}
	
	private void joinTeam(AnniPlayer player, AnniTeam team)
	{
		team.joinTeam(player);
		Player p = player.getPlayer();
		ImageMessage m = messages.get(team.getColor());
		if(m != null)
			m.sendToPlayer(p);
		if(Game.isGameRunning())
			p.setHealth(0);

	}

}
