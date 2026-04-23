package vip.megumin.kits;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.projectiles.ProjectileSource;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.base.ConfigurableKit;

public class Archer extends ConfigurableKit
{

	
	@Override
	protected void setUp()
	{
		ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(Material.ARROW,3)).addIngredient(Material.FLINT).addIngredient(Material.STICK);
		Bukkit.addRecipe(recipe);	
	}

	@Override
	protected String getInternalName()
	{
		return "Archer";
	}

	@Override
	protected ItemStack getIcon()
	{
		return new ItemStack(Material.BOW);
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
					aqua+"You are the rain.", 
					"",
					aqua+"Obliterate your enemies",
					aqua+"from a distance.", 
					"", 
					aqua+"You deal +1 damage with",
					aqua+"any bow and you have the",
					aqua+"ability to create arrows",
					aqua+"from flint and stucks.",
					"",
					aqua+"Just put a flint on top",
					aqua+"of a stick in your crafting",
					aqua+"interface to create 3",
					aqua+"arrows!",
				});
		return l;
	}

	
	@Override
	protected Loadout getFinalLoadout()
	{
		ItemStack bow = KitUtils.addSoulbound(new ItemStack(Material.BOW));
		KitUtils.addEnchant(bow, Enchantment.ARROW_KNOCKBACK, 1);
		KitUtils.addEnchant(bow, Enchantment.ARROW_INFINITE, 1);
		ItemStack arrow = KitUtils.setName(new ItemStack(Material.ARROW), ChatColor.AQUA+"Arrow of Infinity");
		return new Loadout().addWoodSword().addWoodPick().addWoodAxe().addWoodShovel().addItem(bow)
				.addSoulboundItem(arrow).addHealthPotion1();
	}

	@Override
	public void cleanup(Player player)
	{
		
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void arrowCraftingStopper(CraftItemEvent event)
	{
		if(event.getRecipe().getResult().getType() == Material.ARROW && event.getRecipe().getResult().getAmount() == 3)
		{
			AnniPlayer player = AnniPlayer.getPlayer(event.getWhoClicked().getUniqueId());
			if(player != null && (player.getKit() == null || !player.getKit().equals(this)))
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
	public void damageListener(final EntityDamageByEntityEvent event)
	{
		if(event.getDamager().getType() == EntityType.ARROW)
		{
			ProjectileSource s = ((Projectile)event.getDamager()).getShooter();
			if(s instanceof Player)
			{
				AnniPlayer shooter = AnniPlayer.getPlayer(((Player) s).getUniqueId());
				if(shooter != null && shooter.getKit() != null && shooter.getKit().equals(this))
					event.setDamage(event.getDamage()+1);
			}
		}
	}
}

