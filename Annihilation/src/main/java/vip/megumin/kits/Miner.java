package vip.megumin.kits;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import vip.megumin.anniPro.anniEvents.ResourceBreakEvent;
import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.anniPro.main.AnnihilationMain;
import vip.megumin.base.ConfigurableKit;

public class Miner extends ConfigurableKit
{

	
	private Random rand;
	private java.util.Set<java.util.UUID> goldRush;
	private java.util.Map<java.util.UUID,Long> cooldowns;
	
	@Override
	protected void setUp()
	{
		rand = new Random(System.currentTimeMillis());
		goldRush = new java.util.HashSet<java.util.UUID>();
		cooldowns = new java.util.HashMap<java.util.UUID,Long>();
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
		addToList(l, aqua+"You are the hands.",
                "",
                aqua+"Mine precious resources",
                aqua+"to gear up your team as",
                aqua+"well as yourself so you",
                aqua+"will strike swiftly and",
                aqua+"with strength on battlefield!",
                "",
                aqua+"Start with an effeciency",
                aqua+"pick, 4 coal, and a furnace",
                aqua+"to get minerals quicker.");
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
						if(goldRush.contains(event.getPlayer().getID()))
							products[x].setAmount(products[x].getAmount() * (rand.nextInt(100) < 33 ? 3 : 2));
						else if(rand.nextInt(100) < 80)
							products[x].setAmount(products[x].getAmount()*2);
					}
				}
				event.setProducts(products);	
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void goldRush(PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();
		final AnniPlayer anniPlayer = AnniPlayer.getPlayer(player.getUniqueId());
		if(anniPlayer != null && anniPlayer.getKit().equals(this) && event.getAction().name().contains("RIGHT") && KitUtils.itemHasName(event.getItem(), org.bukkit.ChatColor.GOLD+"Gold Rush"))
		{
			Long end = cooldowns.get(player.getUniqueId());
			if(end != null && end.longValue() > System.currentTimeMillis())
			{
				player.sendMessage(org.bukkit.ChatColor.RED+"Cooldown: "+((end.longValue()-System.currentTimeMillis()+999)/1000)+"s");
				return;
			}
			cooldowns.put(player.getUniqueId(), System.currentTimeMillis()+60000);
			goldRush.add(player.getUniqueId());
			new BukkitRunnable(){
				@Override
				public void run()
				{
					goldRush.remove(player.getUniqueId());
				}
			}.runTaskLater(AnnihilationMain.getInstance(), 200);
			event.setCancelled(true);
		}
	}
	
	@Override
	protected Loadout getFinalLoadout()
	{
		return new Loadout().addWoodSword().addSoulboundEnchantedItem(new ItemStack(Material.STONE_PICKAXE), Enchantment.DIG_SPEED, 1).addWoodAxe()
				.addSoulboundItem(KitUtils.setName(new ItemStack(Material.GOLD_NUGGET), org.bukkit.ChatColor.GOLD+"Gold Rush")).addItem(new ItemStack(Material.COAL,8))
				.addSoulboundItem(new ItemStack(Material.FURNACE));
	}

}
