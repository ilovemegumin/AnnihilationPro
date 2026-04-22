package vip.megumin.base;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.voting.ConfigManager;
import com.google.common.base.Function;

public abstract class SpecialItemKit extends ConfigurableKit
{
	private ItemStack specialItem;
	private String specialItemName;
	protected Delays delays;
	
	@Override
	protected void setUp()
	{
		delays = Delays.getInstance();
		specialItem = specialItem();
		if(getDelayLength() > 0 && useDefaultChecking())
		{
			delays.createNewDelay(getInternalName(), new StandardItemUpdater(getSpecialItemName(),specialItem.getType(),new Function<ItemStack,Boolean>(){
				@Override
				public Boolean apply(ItemStack stack)
				{
					return isSpecialItem(stack);
				}}));
		}
		onInitialize();
	}
	
	protected abstract void onInitialize();

	protected abstract ItemStack specialItem();
	protected abstract String defaultSpecialItemName();
	protected abstract boolean isSpecialItem(ItemStack stack);
	protected abstract boolean performSpecialAction(Player player, AnniPlayer p);
	protected abstract long getDelayLength();
	protected abstract boolean useDefaultChecking();
	
	public ItemStack getSpecialItem()
	{
		return specialItem;
	}

	@Override
	protected void loadKitStuff(ConfigurationSection section)
	{
		super.loadKitStuff(section);
		specialItemName = section.getString("SpecialItemName");
	}
	
	@Override
	protected int setDefaults(ConfigurationSection section)
	{

		return ConfigManager.setDefaultIfNotSet(section, "SpecialItemName", defaultSpecialItemName());
	}
	
	public String getSpecialItemName()
	{
		return specialItemName;
	}

	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void specialItemActionCheck(final PlayerInteractEvent event)
	{
		if(useDefaultChecking())
		{
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				if(event.getItem() != null && event.getItem().getType() == specialItem.getType())
				{
					AnniPlayer p = AnniPlayer.getPlayer(event.getPlayer().getUniqueId());
					if(p != null && p.getKit().equals(this) && isSpecialItem(event.getItem()))
					{
						event.setCancelled(true);
						if(!delays.hasActiveDelay(event.getPlayer(), getInternalName()))
						{

							if(performSpecialAction(event.getPlayer(),p) && getDelayLength() > 0)
								delays.addDelay(event.getPlayer(), System.currentTimeMillis()+getDelayLength(), getInternalName());
						}
					}
				}
			}
		}
	}
	
}
