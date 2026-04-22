
package vip.megumin.anniPro.itemMenus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A {@link StaticMenuItem} that closes the
 * {@link ItemMenu}.
 */
public class CloseMenuItem extends StaticMenuItem
{

	public CloseMenuItem()
	{
		super(ChatColor.RED + "Close", new ItemStack(Material.RECORD_4));
	}

	@Override
	public void onItemClick(ItemClickEvent event)
	{
		event.setWillClose(true);
	}
}