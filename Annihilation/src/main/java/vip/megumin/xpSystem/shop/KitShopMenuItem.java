package vip.megumin.xpSystem.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.MenuItem;
import vip.megumin.anniPro.kits.Kit;
import vip.megumin.xpSystem.main.XPMain;
import vip.megumin.xpSystem.main.XPSystem;
import vip.megumin.xpSystem.utils.Acceptor;

public class KitShopMenuItem extends MenuItem
{
	private final KitWrapper wrapper;
	private final XPSystem system;
	private final Map<String,Long> confirmingPlayers;
	
	public KitShopMenuItem(final KitWrapper wrapper,final XPSystem system)
	{
		super(wrapper.kit.getName(), wrapper.kit.getIconPackage().getIcon(), wrapper.kit.getIconPackage().getLore());
		this.wrapper = wrapper;
		this.system = system;
		confirmingPlayers = new HashMap<String,Long>();
	}
	
	public Kit getKit()
	{
		return wrapper.kit;
	}
	
	@Override
	public ItemStack getFinalIcon(Player player)
	{
		List<String> str = new ArrayList<String>(getLore());
		str.add(ChatColor.GOLD+"--------------------------");
		if(wrapper.kit.hasPermission(player))
			str.add(Shop.purchasedMessage);

		else
		{
			Long l = confirmingPlayers.get(player.getName());
			if(l == null)
				str.add(XPMain.formatString(Shop.forsaleMessage, wrapper.price));
			else
			{
				if(System.currentTimeMillis()-l.longValue() > 2000)
				{
					str.add(XPMain.formatString(Shop.forsaleMessage, wrapper.price));
					confirmingPlayers.remove(player.getName());
				}
				else
					str.add(XPMain.formatString(Shop.confirmMessage, wrapper.price));
			}
		}

		return setNameAndLore(getIcon().clone(), getDisplayName(), str);
	}
	
	@Override
	public void onItemClick(ItemClickEvent event)
	{
		final Player player = event.getPlayer();
		if(player != null && !wrapper.kit.hasPermission(player))
		{
			event.setWillUpdate(true);
			final AnniPlayer anniplayer = AnniPlayer.getPlayer(player.getUniqueId());
			if(anniplayer != null)
			{
				Long l = confirmingPlayers.get(player.getName());
				if(l != null)
				{
					confirmingPlayers.remove(player.getName());
					if(System.currentTimeMillis()-l.longValue() > 2000)
						player.sendMessage(Shop.confirmExpired);	
					else
					{
						event.setWillUpdate(false);
						event.setWillClose(true);
						this.system.getXP(player.getUniqueId(), new Acceptor<Integer>(){
							@Override
							public void accept(Integer xp)
							{
								if(xp >= wrapper.price)
								{
									system.addKit(player.getUniqueId(), wrapper.kit);
									system.removeXP(player.getUniqueId(), wrapper.price);
									player.sendMessage(Shop.kitPurchased.replace("%w", wrapper.kit.getName()));
								}
								else player.sendMessage(Shop.notEnoughXP);
								
							}});
					}	
				}
				else confirmingPlayers.put(player.getName(), System.currentTimeMillis());
			}
		}	
	}
}
