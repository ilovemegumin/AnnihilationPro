package vip.megumin.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniEvents.ResourceBreakEvent;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.base.ConfigurableKit;

public class Enchanter extends ConfigurableKit
{	
	private Random rand;
	
	@Override
	protected void setUp()
	{
		rand = new Random(System.currentTimeMillis());
	}

	@Override
	protected String getInternalName()
	{
		return "Enchanter";
	}

	@Override
	protected ItemStack getIcon()
	{
		return new ItemStack(Material.EXP_BOTTLE);
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
					aqua+"Gain extra exp when gathering",
					aqua+"resources which enables",
					aqua+"quicker level succession.",
					aqua+"",
					aqua+"There is a small chance",
					aqua+"to obtain experience bottles",
					aqua+"when mining ores and chopping",
					aqua+"wood.",
				});
		return l;
	}

	@Override
	public void cleanup(Player arg0)
	{
		
	}

	@EventHandler
	public void onResourceBreak(ResourceBreakEvent event)
	{
		if(event.getPlayer().getKit().equals(this))
		{
			int xp = event.getXP();

			if(xp > 0)
			{

				xp = (int)Math.ceil(xp*2);
				event.setXP(xp);

				if(rand.nextInt(100) == 4)
				{
					Player pl = event.getPlayer().getPlayer();
					if(pl != null)
						pl.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE));
				}
			}
		}
	}

	
	@Override
	protected Loadout getFinalLoadout()
	{
		return new Loadout().addGoldSword().addWoodPick().addWoodAxe();
	}
}
