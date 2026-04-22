package vip.megumin.kits;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.base.ConfigurableKit;

public class Berserker extends ConfigurableKit
{
	@Override
	protected void setUp()
	{
		
	}

	@Override
	protected String getInternalName()
	{
		return "Berserker";
	}

	@Override
	protected ItemStack getIcon()
	{
		return new ItemStack(Material.CHAINMAIL_CHESTPLATE);
	}
	
	@Override
	protected int setDefaults(ConfigurationSection section)
	{
		return 0;
	}

	@Override
	protected List<String> getDefaultDescription()
	{
		List<String> l = new ArrayList<String>();
		addToList(l,new String[]
			{
				aqua+"You are the power.", 
				"",
				aqua+"Start each life with only",
				aqua+"9 hearts of health, for",
				aqua+"every kill you make you",
				aqua+"gain a heart to a maximum",
				aqua+"health of 13 hearts.",
			});
		return l;
	}

	@Override
	public void cleanup(Player player) 
	{
		if(player != null)
		{
			player.setMaxHealth(20);

		}
	}

	@Override
	public void onPlayerSpawn(Player player)
	{

		player.setMaxHealth(18);
		super.onPlayerSpawn(player);
	}
	
	@Override
	protected Loadout getFinalLoadout()
	{
		return new Loadout().addStoneSword().addWoodPick().addWoodAxe().addHealthPotion1();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void damageListener(final EntityDamageByEntityEvent event)
	{
		if(event.getEntity().getType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER)
		{
			Player one = (Player)event.getDamager();
			AnniPlayer p = AnniPlayer.getPlayer(one.getUniqueId());
			if(p != null && p.getKit().equals(this))
			{
				if((one.getHealth() / one.getMaxHealth()) <= .42)
					event.setDamage(event.getDamage()+1);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void damageListener(final PlayerDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();
		if(killer != null)
		{
			AnniPlayer p = AnniPlayer.getPlayer(killer.getUniqueId());
			if(p != null && p.getKit().equals(this))
			{
				if(killer.getMaxHealth() <= 24)
					killer.setMaxHealth(killer.getMaxHealth()+2);
			}
		}
	}
}
