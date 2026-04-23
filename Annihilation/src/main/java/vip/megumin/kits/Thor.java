package vip.megumin.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.base.SpecialItemKit;

public class Thor extends SpecialItemKit
{
	@Override
	protected void onInitialize()
	{

		
	}

	@Override
	protected ItemStack specialItem()
	{
		ItemStack hammer  = KitUtils.addSoulbound(new ItemStack(Material.GOLD_AXE));			
		KitUtils.addEnchant(hammer, org.bukkit.enchantments.Enchantment.DURABILITY, 10);
		KitUtils.addEnchant(hammer, org.bukkit.enchantments.Enchantment.KNOCKBACK, 1);
		ItemMeta meta = hammer.getItemMeta();
		meta.setDisplayName(getSpecialItemName()+" "+ChatColor.GREEN+"READY");
		hammer.setItemMeta(meta);
		return hammer;
	}

	@Override
	protected String defaultSpecialItemName()
	{
		return ChatColor.GOLD+"Hammer";
	}

	@Override
	protected boolean isSpecialItem(ItemStack stack)
	{
		if(stack != null && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
		{
			String name = stack.getItemMeta().getDisplayName();
			if(name.contains(getSpecialItemName()) && KitUtils.isSoulbound(stack))
				return true;
		}
		return false;
	}

	@Override
	protected boolean performSpecialAction(Player player, AnniPlayer p)
	{
		if(p.getTeam() == null)
			return false;
		for(org.bukkit.entity.Entity entity : player.getNearbyEntities(10, 10, 10))
		{
			if(entity.getType() == EntityType.PLAYER)
			{
				AnniPlayer target = AnniPlayer.getPlayer(entity.getUniqueId());
				if(target != null && target.getTeam() != null && !target.getTeam().equals(p.getTeam()))
				{
					player.getWorld().strikeLightningEffect(entity.getLocation());
					((Player)entity).damage(5.0, player);
				}
			}
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 0));
		return true;
	}

	@Override
	protected long getDelayLength()
	{
		return 40000;
	}

	@Override
	protected boolean useDefaultChecking()
	{
		return true;
	}

	@Override
	protected String getInternalName()
	{
		return "Thor";
	}

	@Override
	protected ItemStack getIcon()
	{
		return new ItemStack(Material.GOLD_AXE);
	}

	@Override
	protected List<String> getDefaultDescription()
	{
		List<String> l = new ArrayList<String>();
		addToList(l,new String[]
				{
					aqua+"You are the hammer.",
					aqua+"",
					aqua+"You are not afraid of",
					aqua+"lava and fire because",
					aqua+"you are immune, but your",
					aqua+"enemies are not.",
					aqua+"",
					aqua+"Every hit you land has",
					aqua+"a chance of igniting your",
					aqua+"enemy.",
				});
		return l;
	}

	@Override
	protected Loadout getFinalLoadout()
	{
		return new Loadout().addWoodSword().addWoodPick().addWoodAxe().addItem(super.getSpecialItem());
	}

	@Override
	public void cleanup(Player arg0)
	{
		
	}

}
