package vip.megumin.npclib.api;

import vip.megumin.npclib.impl.SpigotNms;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Util
{
    private Util() {}
    
    
    private static final NMS NMS = new SpigotNms();
    public static NMS getNMS() {
    	return NMS;
    }
    
    public static Player[] getNearbyPlayers(int range, Location l) {
    	List<Player> nearby = new ArrayList<>(12);
    	for (Player p : Bukkit.getOnlinePlayers()) {
    		double distance = p.getLocation().distanceSquared(l);
    		if (distance <= range) {
    			nearby.add(p);
    		}
    	}
    	return nearby.toArray(new Player[nearby.size()]);
    }
}
