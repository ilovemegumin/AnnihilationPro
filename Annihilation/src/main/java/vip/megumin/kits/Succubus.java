package vip.megumin.kits;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.anniGame.Game;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.anniPro.voting.ConfigManager;
import vip.megumin.base.ConfigurableKit;
import vip.megumin.base.Delays;
import vip.megumin.base.StandardItemUpdater;
import com.google.common.base.Function;

public class Succubus extends ConfigurableKit
{
	private ItemStack sucItem;
	private String sucItemName;
	
	@Override
	protected void setUp()
	{
		sucItem = KitUtils.addSoulbound(getIcon().clone());
		ItemMeta m = sucItem.getItemMeta();
		m.setDisplayName(sucItemName);
		sucItem.setItemMeta(m);
		Delays.getInstance().createNewDelay(getInternalName(), new StandardItemUpdater(sucItemName,sucItem.getType(),new Function<ItemStack,Boolean>(){
			@Override
			public Boolean apply(ItemStack stack)
			{
				return isSuccubusItem(stack);
			}}));
	}
	
	private boolean isSuccubusItem(ItemStack stack)
	{
		if(stack != null && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
		{
			String name = stack.getItemMeta().getDisplayName();
            return name.contains(this.sucItemName) && KitUtils.isSoulbound(stack);
		}
		return false;
	}	

	@Override
	protected String getInternalName()
	{
		return "Succubus";
	}

	@SuppressWarnings("deprecation")
	@Override
	protected ItemStack getIcon()
	{
		return new ItemStack(Material.INK_SACK, 1, (short) 0, (byte) 1);
	}

	@Override
	protected int setDefaults(ConfigurationSection section)
	{

		return ConfigManager.setDefaultIfNotSet(section, "SuccubusItemName", aqua+"Life Drain");
	}
	
	@Override
	protected void loadKitStuff(ConfigurationSection section)
	{
		super.loadKitStuff(section);
		sucItemName = section.getString("SuccubusItemName");
	}

	@Override
	protected List<String> getDefaultDescription()
	{
		List<String> l = new ArrayList<String>();
		addToList(l, aqua+"You are the hunter.",
                "",
                aqua+"Once every 2 minutes the",
                aqua+"Succubus can attempt to",
                aqua+"suck the remaining life",
                aqua+"out of an enemy player.",
                "",
                aqua+"If the enemy player has",
                aqua+"less than 40% health,",
                aqua+"they are killed immediately",
                aqua+"and the remaining health",
                aqua+"is transferred to the",
                aqua+"Succubus.",
                "",
                aqua+"However, if the enemy",
                aqua+"player has more than 40%",
                aqua+"health, the Succubus is",
                aqua+"dealt the enemy player's",
                aqua+"remaining health in true",
                aqua+"damage.");
		return l;
	}

	@Override
	public void cleanup(Player arg0)
	{

	}

	
	@Override
	protected Loadout getFinalLoadout()
	{
		return new Loadout().addStoneSword().addWoodPick().addWoodAxe().addItem(this.sucItem);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST,ignoreCancelled = true)
	public void lifeDrain(PlayerInteractEntityEvent event)
	{
		if(Game.isGameRunning() && event.getRightClicked().getType() == EntityType.PLAYER)
		{
			Player user = event.getPlayer();
			AnniPlayer anniUser = AnniPlayer.getPlayer(user.getUniqueId());
			if(anniUser != null && anniUser.getKit().equals(this))
			{
				if(!Delays.getInstance().hasActiveDelay(user, this.getInternalName()))
				{
					Player target = (Player)event.getRightClicked();
					AnniPlayer t = AnniPlayer.getPlayer(target.getUniqueId());
					if(t != null && !t.getTeam().equals(anniUser.getTeam()))
					{
						Delays.getInstance().addDelay(user, System.currentTimeMillis()+120000, this.getInternalName());
						if((target.getHealth() / target.getMaxHealth()) <= .42)
						{
							double newHealth = user.getHealth() + target.getHealth();
							if(newHealth > user.getMaxHealth())
								newHealth = user.getMaxHealth();
							user.setHealth(newHealth);

							target.damage(100, user);
							user.playSound(user.getLocation(), Sound.BLAZE_BREATH, 1, 1.2F);
						}
						else
						{
							double newHealth = user.getHealth() - target.getHealth();
							if(newHealth < 0)
								newHealth = 0;
							user.setHealth(newHealth);
						}
					}
				}
			}
		}
	}
}
