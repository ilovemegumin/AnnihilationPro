package vip.megumin.anniPro.utils;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.MenuItem;
import vip.megumin.anniPro.main.Lang;

public class ShopMenuItem extends MenuItem
{
	private final ItemStack display;
	private final ItemStack product;
	private final int cost;
	private final Material currency;
	public ShopMenuItem(ItemStack displayStack, ItemStack productStack, int cost)
	{
		this(displayStack, productStack, cost, Material.GOLD_INGOT);
	}

	public ShopMenuItem(ItemStack displayStack, ItemStack productStack, int cost, Material currency)
	{
		super(null, null);
		this.display = displayStack.clone();
		this.product = productStack.clone();
		ArrayList<String> l = new ArrayList<String>();
		l.add(currency == Material.GOLD_INGOT ? Lang.COST.toStringReplacement(cost) : ChatColor.GOLD+"Cost: "+cost+" "+currency.name());
		l.add(Lang.QUANTITY.toStringReplacement(product.getAmount()));
		ItemMeta m = display.getItemMeta();
		m.setLore(l);
		display.setItemMeta(m);
		this.cost = cost;
		this.currency = currency;
	}
	
	@Override
	public void onItemClick(ItemClickEvent event)
	{
		Player player = event.getPlayer();
		PlayerInventory p = player.getInventory();
		if(p.containsAtLeast(new ItemStack(currency), cost))
		{
			int total = 0;
			for(ItemStack s : p.all(currency).values())
			{
				total += s.getAmount();
			}
			p.remove(currency);
			if(total-cost > 0)
				p.addItem(new ItemStack(currency,total-cost));
			p.addItem(product);
			player.sendMessage(Lang.PURCHASEDITEM.toString());
		}
		else player.sendMessage(Lang.COULDNOTPURCHASE.toString());
	}
	
	@Override
	public ItemStack getFinalIcon(Player player)
	{
		return display;
	}

}
