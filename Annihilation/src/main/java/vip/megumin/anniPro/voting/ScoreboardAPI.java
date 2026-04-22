package vip.megumin.anniPro.voting;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import vip.megumin.anniPro.anniEvents.GameStartEvent;
import vip.megumin.anniPro.anniGame.AnniTeam;
import vip.megumin.anniPro.anniGame.Game;
import vip.megumin.anniPro.main.Lang;
import vip.megumin.anniPro.utils.Util;

public class ScoreboardAPI
{
	public static void registerListener(JavaPlugin p)
	{
		final BoardListeners l = new BoardListeners();
		Bukkit.getPluginManager().registerEvents(l,p);
	}
	
	private static class BoardListeners implements Listener
	{
		public BoardListeners()
		{
			if(Game.isGameRunning())
				for(Player pl : Bukkit.getOnlinePlayers())
					ScoreboardAPI.setScoreboard(pl);
		}
		
		@EventHandler
		public void onGameStart(GameStartEvent event)
		{
			ScoreboardAPI.showGameBoard(ChatColor.BOLD+(ChatColor.GOLD+Lang.SCOREBOARDMAP.toString()+" "+(Game.getGameMap().getNiceWorldName())));
			for(Player pl : Bukkit.getOnlinePlayers())
				ScoreboardAPI.setScoreboard(pl);
		}
				
		@EventHandler(priority = EventPriority.MONITOR)
		public void playerCheck(PlayerJoinEvent event)
		{
			if(Game.isGameRunning())
				ScoreboardAPI.setScoreboard(event.getPlayer());
		}
	}
	
	private static final Scoreboard anniScoreboard;
	private static final Objective obj;
	static
	{
		anniScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		obj = anniScoreboard.registerNewObjective("CAT", "MEOW MEOW");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public static Scoreboard getScoreboard()
	{
		return anniScoreboard;
	}
	
	private static void showGameBoard(String name)
	{
		obj.setDisplayName(name);
		for(AnniTeam team : AnniTeam.Teams)
		{
			Score score = obj.getScore(Util.shortenString(team.getExternalColoredName()+" Nexus", 16));
			score.setScore(team.getHealth());
		}
		Score score = obj.getScore(Util.shortenString(Lang.SCOREBOARDPHASE.toString(), 16));
		score.setScore(1);
	}
	
	public static void updatePhase()
	{
		if(Game.isGameRunning() && Game.getGameMap() != null)
		{
			Score score = obj.getScore(Util.shortenString(Lang.SCOREBOARDPHASE.toString(), 16));
			score.setScore(Game.getGameMap().getCurrentPhase());
		}
	}
	
	public static void setScore(AnniTeam team, int score)
	{
		if(obj != null)
			obj.getScore(Util.shortenString(team.getExternalColoredName()+" Nexus", 16)).setScore(score);	
	}
	
	public static void removeTeam(AnniTeam team)
	{
		anniScoreboard.resetScores(Util.shortenString(team.getExternalColoredName()+" Nexus", 16));
	}
	
	public static void setScoreboard(final Player player)
	{
		player.setScoreboard(anniScoreboard);
	}
}
