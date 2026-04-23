package vip.megumin.anniPro.itemMenus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * An event called when an Item in the
 * {@link ItemMenu} is clicked.
 */
public class ItemClickEvent
{
	private final Player player;
	private final ClickType clicktype;
	private boolean goBack = false;
	private boolean close = false;
	private boolean update = false;
	private final ItemStack stack;

	public ItemClickEvent(Player player,ItemStack stack, ClickType type)
	{
		this.player = player;
		this.stack = stack;
		this.clicktype = type;
	}

	/**
	 * Gets the player who clicked.
	 *
	 * @return The player who clicked.
	 */
	public Player getPlayer()
	{
		return player;
	}
	
	public ClickType getClickType()
	{
		return clicktype;
	}
	
	public ItemStack getClickedItem()
	{
		return stack;
	}

	/**
	 * Checks if the {@link ItemMenu} will go back
	 * to the parent menu.
	 *
	 * @return True if the {@link ItemMenu} will go
	 *         back to the parent menu, else false.
	 */
	public boolean willGoBack()
	{
		return goBack;
	}

	/**
	 * Sets if the {@link ItemMenu} will go back to
	 * the parent menu.
	 *
	 * @param goBack
	 *            If the {@link ItemMenu} will go
	 *            back to the parent menu.
	 */
	public void setWillGoBack(boolean goBack)
	{
		this.goBack = goBack;
		if (goBack)
		{
			close = false;
			update = false;
		}
	}

	/**
	 * Checks if the {@link ItemMenu} will close.
	 *
	 * @return True if the {@link ItemMenu} will
	 *         close, else false.
	 */
	public boolean willClose()
	{
		return close;
	}

	/**
	 * Sets if the {@link ItemMenu} will close.
	 *
	 * @param close
	 *            If the {@link ItemMenu} will
	 *            close.
	 */
	public void setWillClose(boolean close)
	{
		this.close = close;
		if (close)
		{
			goBack = false;
			update = false;
		}
	}

	/**
	 * Checks if the {@link ItemMenu} will update.
	 *
	 * @return True if the {@link ItemMenu} will
	 *         update, else false.
	 */
	public boolean willUpdate()
	{
		return update;
	}

	/**
	 * Sets if the {@link ItemMenu} will update.
	 *
	 * @param update
	 *            If the {@link ItemMenu} will
	 *            update.
	 */
	public void setWillUpdate(boolean update)
	{
		this.update = update;
		if (update)
		{
			goBack = false;
			close = false;
		}
	}
}