package vip.megumin.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniEvents.PlayerKilledEvent;
import vip.megumin.anniPro.anniEvents.PlayerKilledEvent.KillAttribute;
import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.base.ConfigurableKit;

public class Defender extends ConfigurableKit
{

	@Override
	protected void setUp() 
	{
	}

	@Override
	protected String getInternalName() 
	{
		return "Defender";
	}

	@Override
	protected ItemStack getIcon() 
	{
		return new ItemStack(Material.WOOD_SWORD);
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
		addToList(l, aqua+"You are the last line.",
                "",
                aqua+"While around the nexus",
                aqua+"you gain the regeneration",
                aqua+"buff and killing players",
                aqua+"while in the vicinity",
                aqua+"of the nexus rewards you",
                aqua+"with extra experience",
                aqua+"points.");
		return l;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void checkXP(PlayerKilledEvent event)
	{
		if(event.getKiller().getKit().equals(this) && event.getAttributes().contains(KillAttribute.NEXUSDEFENSE) && event.getKiller().getPlayer() != null)
			event.getKiller().getPlayer().giveExp(20);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void damageHandler(EntityDamageEvent event) 
	{
		if(event.getEntityType() == EntityType.PLAYER)
		{
			AnniPlayer p = AnniPlayer.getPlayer(event.getEntity().getUniqueId());
			if(p != null && p.getTeam() != null && !p.getTeam().isTeamDead() && p.getTeam().getNexus().getLocation() != null && p.getKit().equals(this))
			{
				Player player = (Player)event.getEntity();
				if(player.getLocation().distanceSquared(p.getTeam().getNexus().getLocation().toLocation()) <= 20*20)
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,Integer.MAX_VALUE,0));
				else 
					player.removePotionEffect(PotionEffectType.REGENERATION);
			}
		}
	}

	@Override
	public void cleanup(Player arg0) 
	{
		
	}

	@Override
	protected Loadout getFinalLoadout()
	{
		return new Loadout().addWoodSword().addWoodPick().addWoodAxe().addWoodShovel()
				.addSoulboundItem(KitUtils.setName(new ItemStack(Material.INK_SACK, 1, (short)10), ChatColor.GREEN+"Guardian's Warp"))
				.setUseDefaultArmor(true).setArmor(2,KitUtils.addSoulbound(new ItemStack(Material.CHAINMAIL_CHESTPLATE)));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void guardianWarp(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = AnniPlayer.getPlayer(player.getUniqueId());
		if(p != null && p.getKit().equals(this) && p.getTeam() != null && event.getAction().name().contains("RIGHT") && KitUtils.itemHasName(event.getItem(), ChatColor.GREEN+"Guardian's Warp"))
		{
			if(p.getTeam().getNexus().getLocation() != null)
				player.teleport(p.getTeam().getNexus().getLocation().toLocation().add(0.5, 1, 0.5));
			event.setCancelled(true);
		}
	}

}
