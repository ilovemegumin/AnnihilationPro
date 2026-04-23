package vip.megumin.anniPro.kits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.main.AnnihilationMain;
import vip.megumin.anniPro.main.Lang;

public class CivilianKit extends Kit
{
	public CivilianKit()
	{
		Bukkit.getPluginManager().registerEvents(this, AnnihilationMain.getInstance());
		this.Initialize();
	}
	
	private Loadout loadout;
	
	@Override
	public boolean Initialize()
	{
		loadout = new Loadout().addWoodSword().addStonePick().addSoulboundItem(new ItemStack(Material.STONE_AXE)).addWoodShovel()
				.addSoulboundItem(new ItemStack(Material.CHEST)).addSoulboundItem(KitUtils.setName(new ItemStack(Material.BRICK), ChatColor.AQUA+"Craft O' Matic")).addNavCompass().finalizeLoadout();
		return true;
	}

	@Override
	public String getDisplayName()
	{
		return Lang.CIVILIANNAME.toString();
	}

	@Override
	public IconPackage getIconPackage()
	{
		return new IconPackage(new ItemStack(Material.WORKBENCH), Lang.CIVILIANLORE.toStringArray());
	}

	@Override
	public void onPlayerSpawn(Player player)
	{
		loadout.giveLoadout(player);
	}

	@Override
	public void cleanup(Player player)
	{
		
	}

	@Override
	public boolean hasPermission(Player player)
	{
		return true;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void craftOMatic(PlayerInteractEvent event)
	{
		AnniPlayer player = AnniPlayer.getPlayer(event.getPlayer().getUniqueId());
		if(player != null && player.getKit().equals(this) && event.getAction().name().contains("RIGHT") && KitUtils.itemHasName(event.getItem(), ChatColor.AQUA+"Craft O' Matic"))
		{
			event.getPlayer().openWorkbench(null, true);
			event.setCancelled(true);
		}
	}
}
