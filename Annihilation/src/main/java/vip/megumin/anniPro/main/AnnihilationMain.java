
package vip.megumin.anniPro.main;

import java.io.File;
import java.io.IOException;

import vip.megumin.anniPro.anniEvents.PluginDisableEvent;
import vip.megumin.anniPro.announcementBar.AnnounceBar;
import vip.megumin.anniPro.announcementBar.Announcement;
import vip.megumin.npclib.api.NPCMain;
import vip.megumin.anniPro.utils.DamageControl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vip.megumin.anniPro.anniEvents.GameEndEvent;
import vip.megumin.anniPro.anniEvents.GameStartEvent;
import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.anniGame.AnniTeam;
import vip.megumin.anniPro.anniGame.Game;
import vip.megumin.anniPro.anniGame.GameListeners;
import vip.megumin.anniPro.anniGame.GameVars;
import vip.megumin.anniPro.anniGame.StandardPhaseHandler;
import vip.megumin.anniPro.anniMap.GameMap;
import vip.megumin.anniPro.anniMap.LobbyMap;
import vip.megumin.anniPro.itemMenus.ActionMenuItem;
import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.ItemClickHandler;
import vip.megumin.anniPro.itemMenus.MenuItem;
import vip.megumin.anniPro.kits.CustomItem;
import vip.megumin.anniPro.kits.KitLoading;
import vip.megumin.anniPro.mapBuilder.MapBuilder;
import vip.megumin.anniPro.security.Log4jExploitFix;
import vip.megumin.anniPro.utils.InvisibilityListeners;
import vip.megumin.anniPro.voting.AutoStarter;
import vip.megumin.anniPro.voting.ConfigManager;
import vip.megumin.anniPro.voting.ScoreboardAPI;
import vip.megumin.anniPro.voting.VoteMapManager;
import vip.megumin.anniPro.world.WorldBackupRestorer;
import vip.megumin.xpSystem.main.XPMain;

public class AnnihilationMain extends JavaPlugin implements Listener
{ 
	private static JavaPlugin instance;
    private XPMain xpMain;
	public static JavaPlugin getInstance()
	{
		return instance;
	}
	
	
	@Override
	public void onLoad()
	{
		System.setProperty("log4j2.formatMsgNoLookups", "true");
	}
	
	@Override
	public void onEnable()
	{
		instance = this;
        WorldBackupRestorer.restoreWorlds(this);
		loadLang();
		new InvisibilityListeners(this);
		Bukkit.getPluginManager().registerEvents(this,this);

        DamageControl.register(this);
		
		ConfigManager.load(this); //Enables the loading of the main config file, this is now different from the lobby config file
		if(ConfigManager.getConfig().getBoolean("Log4jExploitFix.Enabled", true))
			new Log4jExploitFix(this, ConfigManager.getConfig().getBoolean("Log4jExploitFix.Log-Attempts", true));
		
		loadMainValues(); //This will load values from the main config file and load the lobby from the lobby config file
		
		VoteMapManager.registerListener(this);
		AnniCommand.register(this);
		buildAnniCommand();
		new MapBuilder(this);

		handleAutoAndVoting();

        if(GameVars.getUseAntiLog()) //Only register the logout prevention if its enabled from the config
            NPCMain.registerLogoutPrevention(this);

        new KitLoading(this);
        xpMain = new XPMain(this);
        xpMain.onEnable();
		
		AnniPlayer.RegisterListener(this); //needs to come after loading vars, checks players against game and lobby worlds
		
		if(Game.LobbyMap != null)
		{
			for(Player pl : Bukkit.getOnlinePlayers())
				Game.LobbyMap.sendToSpawn(pl);
		}
		
		ScoreboardAPI.registerListener(this); //needs to come after anniplayer loading, checks anni players
		
		new TeamCommand(this);
		new GameListeners(this);
		new AreaCommand(this);
	}
	
	public void loadLang()
	{
		File lang = new File(getDataFolder(), "LanguageConfig.yml");
		if (!lang.exists())
		{
			try
			{
				if(!getDataFolder().exists() && !getDataFolder().mkdirs())
					throw new IOException("Unable to create plugin data folder");
				lang.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace(); // So they notice
				Bukkit.getLogger().severe("[Annihilation] Couldn't create language file.");
				Bukkit.getLogger().severe("[Annihilation] This is a fatal error. Now disabling");
				this.setEnabled(false); // Without it loaded, we can't send them messages
			}
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
		conf.options().header(
				"This is the language config file. "
				+ "%# will be replaced with a number when needed. "
				+ "%w will be replaced with a word when needed. "
				+ "%n is the line separator. "
				+ "Normal MC color codes are supported.");
		for (Lang item : Lang.values())
		{
			if (conf.getString(item.getPath()) == null)
			{
				conf.set(item.getPath(), item.getDefault());
			}
		}
		Lang.setFile(conf);
		try
		{
			conf.save(lang);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void buildAnniCommand()
	{
		AnniCommand.registerArgument(new AnniArgument(){

			@Override
			public String getHelp() 
			{
				return ChatColor.LIGHT_PURPLE+"Start--"+ChatColor.GREEN+"Starts a game of Annihilation.";
			}

			@Override
			public boolean useByPlayerOnly() 
			{
				return false;
			}

			@Override
			public String getArgumentName() 
			{
				return "Start";
			}

			@Override
			public void executeCommand(CommandSender sender, String label,String[] args) 
			{
				if(!Game.isGameRunning())
				{
					if(Game.startGame())
						sender.sendMessage(ChatColor.GREEN+"The game has begun!");
					else 
						sender.sendMessage(ChatColor.RED+"The game was not started!");
				}
				else sender.sendMessage(ChatColor.RED+"The game is already running.");
			}

			@Override
			public String getPermission() 
			{
				return null;
			}

			@Override
			public MenuItem getMenuItem() 
			{
				return new ActionMenuItem("Start Game", new ItemClickHandler(){
					@Override
					public void onItemClick(ItemClickEvent event) {
						executeCommand(event.getPlayer(),null,null);
						event.setWillClose(true);
					}}, new ItemStack(Material.FEATHER), Game.isGameRunning() ? ChatColor.RED+"The game is already running." : ChatColor.GREEN+"Click to start the game.");
			}
			});
		AnniCommand.registerArgument(new AnniArgument(){

			@Override
			public String getHelp() 
			{
				return ChatColor.LIGHT_PURPLE+"Mapbuilder--"+ChatColor.GREEN+"Gives the mapbuilder item.";
			}

			@Override
			public boolean useByPlayerOnly() 
			{
				return true;
			}

			@Override
			public String getArgumentName() 
			{
				return "Mapbuilder";
			}

			@Override
			public void executeCommand(CommandSender sender, String label,String[] args)
			{
				if(sender instanceof Player)
				{
					((Player)sender).getInventory().addItem(CustomItem.MAPBUILDER.toItemStack(1));
				}
			}

			@Override
			public String getPermission()
			{
				return null;
			}

			@Override
			public MenuItem getMenuItem() 
			{
				return new ActionMenuItem("Get Mapbuilder", new ItemClickHandler(){
					@Override
					public void onItemClick(ItemClickEvent event) {
						executeCommand(event.getPlayer(),null,null);
						
					}},new ItemStack(Material.DIAMOND_PICKAXE),ChatColor.GREEN+"Click to get the mapbuilder item.");
			}});
		AnniCommand.registerArgument(new AnniArgument(){

			@Override
			public String getHelp() 
			{
				return ChatColor.LIGHT_PURPLE+"Save [Config,World,All]--"+ChatColor.GREEN+"Saves the specified item.";
			}

			@Override
			public boolean useByPlayerOnly() 
			{
				return false;
			}

			@Override
			public String getArgumentName() 
			{
				return "Save";
			}

			@Override
			public void executeCommand(CommandSender sender, String label,String[] args) 
			{	
				if(args != null && args.length > 0)
				{
					if(Game.getGameMap() != null)
					{
						GameMap map = Game.getGameMap();
						String name = map.getNiceWorldName();
						if(args[0].equalsIgnoreCase("config"))
						{
							map.saveToConfig();
							map.backupConfig();
							sender.sendMessage(ChatColor.GREEN+"Saved "+name+" config");
						}
						else if(args[0].equalsIgnoreCase("world"))
						{
							Game.getGameMap().backUpWorld();
							sender.sendMessage(ChatColor.GREEN+"Saved "+name+" world");
						}
						else if(args[0].equalsIgnoreCase("both") || args[0].equalsIgnoreCase("all"))
						{
							map.saveToConfig();
							Game.getGameMap().backupConfig();
							Game.getGameMap().backUpWorld();		
							sender.sendMessage(ChatColor.GREEN+"Saved "+name+" config and world");
						}
					}
					else sender.sendMessage(ChatColor.RED+"You do not have a game map loaded!");
				}
			}

			@Override
			public String getPermission() 
			{
				return null;
			}

			@Override
			public MenuItem getMenuItem() 
			{
				return new ActionMenuItem("Save", new ItemClickHandler(){
					@Override
					public void onItemClick(ItemClickEvent event)
					{
						if(event.getClickType() == ClickType.LEFT)
							executeCommand(event.getPlayer(),null,new String[]{"Config"});
						else if(event.getClickType() == ClickType.RIGHT)
							executeCommand(event.getPlayer(),null,new String[]{"World"});
						else if(event.getClickType() == ClickType.SHIFT_LEFT || event.getClickType() == ClickType.SHIFT_RIGHT)
							executeCommand(event.getPlayer(),null,new String[]{"All"});
					}}, new ItemStack(Material.ANVIL), 
						ChatColor.GREEN+"Left click to save the Map Config.", ChatColor.GREEN+"Right click to save the Map World.",ChatColor.GREEN+"Shift click to save Both. (Config and World)");
			}});
	}
	
	private void handleAutoAndVoting()
	{
		if(GameVars.getAutoStart()) //This means they do want auto-start
			new AutoStarter(this,GameVars.getPlayersToStart(),GameVars.getCountdownToStart());

		if(GameVars.getVoting())
			VoteMapManager.beginVoting();	
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onGameStart(GameStartEvent event)
	{
		if(Game.getGameMap() != null)
		{
			for(AnniTeam t : AnniTeam.Teams)
			{
				t.setHealth(75);
			}
			GameMap map = Game.getGameMap();
			for(final AnniPlayer p : AnniPlayer.getPlayers())
			{
				final Player player = p.getPlayer();
				if(player != null && p.getTeam() != null)
				{
					player.setHealth(player.getHealth());
					player.setFoodLevel(20);
					player.setGameMode(GameVars.getDefaultGamemode());
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.teleport(p.getTeam().getRandomSpawn());
					p.getKit().onPlayerSpawn(player);
				}
			}
            AnnounceBar.getInstance().countDown(new Announcement(Lang.PHASEBAR.toStringReplacement(1) + " - {#}").setTime(map.getPhaseTime()).setCallback(new StandardPhaseHandler()));
			map.setPhase(1);
			map.setCanDamageNexus(false);
		}
	}
	
	@EventHandler
	public void onGameEnd(GameEndEvent event)
	{
		GameMap map = Game.getGameMap();
		map.setPhase(0);
		map.setCanDamageNexus(false);
		
		if(event.getWinningTeam() != null)
			Bukkit.broadcastMessage(ChatColor.DARK_PURPLE+"The game has ended! "+event.getWinningTeam().getColor()+event.getWinningTeam().getName()+" Team "+ChatColor.DARK_PURPLE+"has won!");
		
		if(!GameVars.getEndOfGameCommand().equals(""))
            AnnounceBar.getInstance().countDown(new Announcement(ChatColor.DARK_PURPLE + "Game ends in: {#}").setTime(GameVars.getEndOfGameCountdown()).setCallback(new Runnable()
            {
                @Override
                public void run()
                {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), GameVars.getEndOfGameCommand());
                }
            }));
	}
	
	@Override
	public void onDisable()
	{
        if (xpMain != null)
            xpMain.onDisable();
		for(AnniPlayer p : AnniPlayer.getPlayers())
		{
			if(p.isOnline())
				p.getKit().cleanup(p.getPlayer());
		}
		saveMainValues();
		if(Game.getGameMap() != null)
			Game.getGameMap().unLoadMap();

        Bukkit.getPluginManager().callEvent(new PluginDisableEvent());
	}
	
	public void saveMainValues()
	{
		if(Game.LobbyMap != null)
		{
			Game.LobbyMap.saveToConfig();
		}
	}
	
	private void loadMainValues()
	{
		YamlConfiguration config = ConfigManager.getConfig();
		if(config != null)
		{
			GameVars.loadGameVars(config);
		}
		
		File lobbyFile = new File(this.getDataFolder(),"AnniLobbyConfig.yml");
		if(lobbyFile.exists())
		{
			Game.LobbyMap = new LobbyMap(lobbyFile);
			Game.LobbyMap.registerListeners(this);
		}
	}
}
