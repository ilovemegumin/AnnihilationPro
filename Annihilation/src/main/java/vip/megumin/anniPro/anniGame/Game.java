package vip.megumin.anniPro.anniGame;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;

import vip.megumin.anniPro.anniEvents.AnniEvent;
import vip.megumin.anniPro.anniEvents.GameEndEvent;
import vip.megumin.anniPro.anniEvents.GameStartEvent;
import vip.megumin.anniPro.announcementBar.AnnounceBar;
import vip.megumin.anniPro.anniMap.GameMap;
import vip.megumin.anniPro.anniMap.LobbyMap;
import vip.megumin.anniPro.main.AnnihilationMain;
import vip.megumin.anniPro.voting.VoteMapManager;

public class Game
{
	
	private static GameMap GameMap = null;
	public static LobbyMap LobbyMap = null;
	
	private static final Map<String,String> worldNames = new HashMap<String,String>();
	private static final Map<String,String> niceNames = new HashMap<String,String>();
	
	private static boolean GameRunning = false;
	
	public static boolean isGameRunning()
	{
		return GameRunning;
	}
	
	public static World getWorld(String name)
	{
		World w = Bukkit.getWorld(name);
		if(w == null)
			w = Bukkit.getWorld(worldNames.get(name.toLowerCase()));
		return w;
	}
	
	public static String getNiceWorldName(String worldName)
	{
		String name = niceNames.get(worldName.toLowerCase());
		if(name == null)
			name = worldName;
		return name;
	}
	
	public static GameMap getGameMap()
	{
		return GameMap;
	}
	
	public static boolean loadGameMap(File worldFolder)
	{
		
		if(worldFolder.exists() && worldFolder.isDirectory())
		{
			File[] files = worldFolder.listFiles(new FilenameFilter()
			{
				public boolean accept(File file, String name)
				{
					return name.equalsIgnoreCase("level.dat");
				}
			});
			
			if ((files != null) && (files.length == 1))
			{
		try
		{
			String path = worldFolder.getPath();
			if(path.contains("plugins"))
				path = path.substring(path.indexOf("plugins"));
			path = path.replace('\\', '/');

			World existing = Bukkit.getWorld(path);
			if(existing != null)
			{
				if(GameMap != null && !existing.getName().equals(GameMap.getWorldName()))
				{
					GameMap.unLoadMap();
					GameMap = null;
				}
				existing.setAutoSave(false);
				existing.setGameRuleValue("doMobSpawning", "false");
				existing.setGameRuleValue("doFireTick", "false");
				Game.GameMap = new GameMap(existing.getName(),worldFolder);
				GameMap.registerListeners(AnnihilationMain.getInstance());
				Game.worldNames.put(worldFolder.getName().toLowerCase(), existing.getName());
				Game.niceNames.put(existing.getName().toLowerCase(),worldFolder.getName());
				return true;
			}

			WorldCreator cr = new WorldCreator(path);
					cr.environment(Environment.NORMAL);
					World mapWorld = Bukkit.createWorld(cr);
					if(mapWorld != null)
					{
						if(GameMap != null)
						{
							GameMap.unLoadMap();
							GameMap = null;
						}
						mapWorld.setAutoSave(false);
						mapWorld.setGameRuleValue("doMobSpawning", "false");
						mapWorld.setGameRuleValue("doFireTick", "false");	
						Game.GameMap = new GameMap(mapWorld.getName(),worldFolder);
						GameMap.registerListeners(AnnihilationMain.getInstance());
						Game.worldNames.put(worldFolder.getName().toLowerCase(), mapWorld.getName());
						Game.niceNames.put(mapWorld.getName().toLowerCase(),worldFolder.getName());
						return true;
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
					GameMap = null;
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean loadGameMap(String mapName)
	{
		return loadGameMap(new File(AnnihilationMain.getInstance().getDataFolder().getAbsolutePath()+"/Worlds",mapName));
	}
	
	public static boolean startGame()
	{
		if(!isGameRunning())
		{
			if(Game.getGameMap() == null)
			{
				if(GameVars.getVoting())
				{
					String winner = VoteMapManager.getWinningMap();
					if(Game.loadGameMap(winner))
					{
						GameRunning = true;
						AnniEvent.callEvent(new GameStartEvent());
						return true;
					}
				}
				else if(Game.loadGameMap(GameVars.getMap()))
				{
					GameRunning = true;
					AnniEvent.callEvent(new GameStartEvent());
					return true;
				}
			}
			else
			{
				GameRunning = true;
				AnniEvent.callEvent(new GameStartEvent());
				return true;
			}
		}
		return false;
	}

	public static boolean endGame(AnniTeam winner)
	{
		if(!GameRunning)
			return false;
		GameRunning = false;
		AnniEvent.callEvent(new GameEndEvent(winner));
		return true;
	}

	public static boolean forceStop()
	{
		if(!GameRunning)
			return false;
		GameRunning = false;
		AnnounceBar.getInstance().cancelCountdown();
		if(GameMap != null)
		{
			GameMap.setCanDamageNexus(false);
			GameMap.setPhase(0);
		}
		return true;
	}
}
