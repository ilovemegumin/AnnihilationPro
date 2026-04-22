package vip.megumin.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniEvents.ResourceBreakEvent;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.base.ConfigurableKit;

public class Miner extends ConfigurableKit
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
		return "Miner";
	}

	@Override
	protected ItemStack getIcon()
	{
		return new ItemStack(Material.STONE_PICKAXE);
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
					aqua+"You are the hands.", 
					"",
					aqua+"Mine precious resources", 
					aqua+"to gear up your team as", 
					aqua+"well as yourself so you", 
					aqua+"will strike swiftly and", 
					aqua+"with strength on battlefield!", 
					"",
					aqua+"Start with an effeciency", 
					aqua+"pick, 4 coal, and a furnace", 
					aqua+"to get minerals quicker.", 
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
			if(event.getResource().Type != Material.LOG && event.getResource().Type != Material.MELON_BLOCK && event.getResource().Type != Material.GRAVEL)
			{
				ItemStack[] products = event.getProducts();
				if(products != null)
				{
					for(int x = 0; x < products.length; x++)
					{
						boolean y = rand.nextBoolean();
						if(y)
							products[x].setAmount(products[x].getAmount()*2);
					}
				}
				event.setProducts(products);	
			}
		}
	}
	
	@Override
	protected Loadout getFinalLoadout()
	{
		return new Loadout().addWoodSword().addSoulboundEnchantedItem(new ItemStack(Material.STONE_PICKAXE), Enchantment.DIG_SPEED, 1).addWoodAxe().addItem(new ItemStack(Material.COAL,4))
				.addSoulboundItem(new ItemStack(Material.FURNACE));
	}

}
