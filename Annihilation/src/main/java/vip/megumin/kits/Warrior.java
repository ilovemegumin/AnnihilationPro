package vip.megumin.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.anniPro.main.AnnihilationMain;
import vip.megumin.base.ConfigurableKit;

public class Warrior extends ConfigurableKit
{
	private java.util.Set<java.util.UUID> frenzy;
	private java.util.Map<java.util.UUID,Long> cooldowns;

	
	@Override
	protected void setUp()
	{
		frenzy = new java.util.HashSet<java.util.UUID>();
		cooldowns = new java.util.HashMap<java.util.UUID,Long>();
	}

	@Override
	protected String getInternalName()
	{
		return "Warrior";
	}

	@Override
	protected ItemStack getIcon()
	{
		return new ItemStack(Material.STONE_SWORD);
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
		addToList(l, aqua+"You are the sword.",
                "",
                aqua+"You deal +1 damage with",
                aqua+"any melee weapon.",
                "",
                aqua+"Spawn with a sword and",
                aqua+"a health potion which",
                aqua+"enable you to immediately",
                aqua+"move on the enemy and",
                aqua+"attack!",
                "",
                aqua+"If you do not have a good",
                aqua+"support back at base gathering",
                aqua+"better gear for you, you",
                aqua+"will be useless in the",
                aqua+"late game.");
		return l;
	}

	
	@Override
	protected Loadout getFinalLoadout()
	{
		return new Loadout().addSoulboundEnchantedItem(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1).addWoodPick().addWoodAxe()
				.addSoulboundItem(KitUtils.setName(new ItemStack(Material.BLAZE_POWDER), ChatColor.GOLD+"Frenzy")).addHealthPotion1();
	}

	@Override
	public void cleanup(Player player)
	{
		
	}

	@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
	public void damageListener(final EntityDamageByEntityEvent event)
	{
		Entity one = event.getDamager();
		if(one.getType() == EntityType.PLAYER)
		{
			Player damager = (Player)one;
			AnniPlayer d = AnniPlayer.getPlayer(damager.getUniqueId());
			if(d != null && d.getKit().equals(this))
			{
				event.setDamage(event.getDamage()+1);
				if(frenzy.contains(damager.getUniqueId()))
					event.setDamage(event.getDamage()+1);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void frenzy(PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();
		AnniPlayer p = AnniPlayer.getPlayer(player.getUniqueId());
		if(p != null && p.getKit().equals(this) && event.getAction().name().contains("RIGHT") && KitUtils.itemHasName(event.getItem(), ChatColor.GOLD+"Frenzy"))
		{
			Long end = cooldowns.get(player.getUniqueId());
			if(end != null && end.longValue() > System.currentTimeMillis())
			{
				player.sendMessage(ChatColor.RED+"Cooldown: "+((end.longValue()-System.currentTimeMillis()+999)/1000)+"s");
				return;
			}
			cooldowns.put(player.getUniqueId(), System.currentTimeMillis()+60000);
			frenzy.add(player.getUniqueId());
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 0));
			new BukkitRunnable(){
				@Override
				public void run()
				{
					frenzy.remove(player.getUniqueId());
				}
			}.runTaskLater(AnnihilationMain.getInstance(), 300);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void frenzyDamage(EntityDamageEvent event)
	{
		if(event.getEntityType() == EntityType.PLAYER && frenzy.contains(event.getEntity().getUniqueId()))
			event.setDamage(event.getDamage()*1.25);
	}
}

