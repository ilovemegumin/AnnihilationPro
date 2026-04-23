package vip.megumin.anniPro.stats;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import vip.megumin.anniPro.anniEvents.GameEndEvent;
import vip.megumin.anniPro.anniEvents.NexusHitEvent;
import vip.megumin.anniPro.anniEvents.PlayerKilledEvent;
import vip.megumin.anniPro.anniGame.AnniPlayer;

public class ProfileCommand implements Listener, CommandExecutor
{
	private final JavaPlugin plugin;
	private final File file;
	private final YamlConfiguration data;
	private final Map<UUID, Long> sessionStarts = new HashMap<UUID, Long>();
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public ProfileCommand(JavaPlugin plugin)
	{
		this.plugin = plugin;
		this.file = new File(plugin.getDataFolder(), "Profiles.yml");
		this.data = YamlConfiguration.loadConfiguration(file);
		Bukkit.getPluginManager().registerEvents(this, plugin);
		plugin.getCommand("profile").setExecutor(this);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void join(PlayerJoinEvent event)
	{
		UUID id = event.getPlayer().getUniqueId();
		String path = path(id);
		if(!data.isSet(path+".first-join"))
			data.set(path+".first-join", System.currentTimeMillis());
		data.set(path+".name", event.getPlayer().getName());
		sessionStarts.put(id, System.currentTimeMillis());
		save();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void quit(PlayerQuitEvent event)
	{
		flushPlayTime(event.getPlayer().getUniqueId());
		save();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void kill(PlayerKilledEvent event)
	{
		add(event.getKiller().getID(), "kills", 1);
		add(event.getPlayer().getID(), "deaths", 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void nexus(NexusHitEvent event)
	{
		add(event.getPlayer().getID(), "nexus-damage", event.getDamage());
		if(event.willKillTeam())
			add(event.getPlayer().getID(), "teams-destroyed", 1);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void gameEnd(GameEndEvent event)
	{
		for(AnniPlayer player : AnniPlayer.getPlayers())
			if(player.isOnline())
				add(player.getID(), "games", 1);
		save();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		OfflinePlayer target;
		if(args.length > 0)
			target = Bukkit.getOfflinePlayer(args[0]);
		else if(sender instanceof Player)
			target = (Player)sender;
		else
		{
			sender.sendMessage(ChatColor.RED+"Usage: /profile <mcid>");
			return true;
		}

		UUID id = target.getUniqueId();
		if(target.isOnline())
			flushPlayTime(id);
		String path = path(id);
		if(!data.isSet(path+".first-join"))
		{
			sender.sendMessage(ChatColor.RED+"No Annihilation profile found for "+target.getName()+".");
			return true;
		}

		int kills = data.getInt(path+".kills");
		int deaths = data.getInt(path+".deaths");
		double kd = deaths == 0 ? kills : ((double)kills / (double)deaths);
		sender.sendMessage(ChatColor.GOLD+"----- Annihilation Profile: "+data.getString(path+".name", target.getName())+" -----");
		sender.sendMessage(ChatColor.YELLOW+"First Join: "+ChatColor.WHITE+dateFormat.format(new Date(data.getLong(path+".first-join"))));
		sender.sendMessage(ChatColor.YELLOW+"Play Time: "+ChatColor.WHITE+formatDuration(data.getLong(path+".play-time")));
		sender.sendMessage(ChatColor.YELLOW+"Kills: "+ChatColor.WHITE+kills+ChatColor.GRAY+" / Deaths: "+ChatColor.WHITE+deaths+ChatColor.GRAY+" / KD: "+ChatColor.WHITE+String.format("%.2f", kd));
		sender.sendMessage(ChatColor.YELLOW+"Nexus Damage: "+ChatColor.WHITE+data.getInt(path+".nexus-damage"));
		sender.sendMessage(ChatColor.YELLOW+"Teams Destroyed: "+ChatColor.WHITE+data.getInt(path+".teams-destroyed"));
		sender.sendMessage(ChatColor.YELLOW+"Games Played: "+ChatColor.WHITE+data.getInt(path+".games"));
		return true;
	}

	private void flushPlayTime(UUID id)
	{
		Long start = sessionStarts.get(id);
		if(start == null)
			return;
		long now = System.currentTimeMillis();
		add(id, "play-time", now - start.longValue());
		sessionStarts.put(id, now);
	}

	private void add(UUID id, String key, long amount)
	{
		String path = path(id)+"."+key;
		data.set(path, data.getLong(path)+amount);
	}

	private String path(UUID id)
	{
		return "players."+id.toString();
	}

	private String formatDuration(long millis)
	{
		long seconds = millis / 1000;
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long secs = seconds % 60;
		return hours+"h "+minutes+"m "+secs+"s";
	}

	public void save()
	{
		try
		{
			if(!plugin.getDataFolder().exists())
				plugin.getDataFolder().mkdirs();
			data.save(file);
		}
		catch(IOException e)
		{
			throw new IllegalStateException("Unable to save profile stats", e);
		}
	}
}
