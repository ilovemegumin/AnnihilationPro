package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;

public class Alchemist extends AnniKitBase
{
	public Alchemist()
	{
		super("Alchemist", Material.BREWING_STAND_ITEM, url("Alchemist"),
				lore("You are the brew.", "Use your private stand and tome", "to prepare stronger potion rushes."),
				woodSword(), woodPick(), woodAxe(), named(Material.BREWING_STAND_ITEM, "Alchemist's Brewing Stand"), named(Material.BOOK, "Alchemist's Tome"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void useTome(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p != null && event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Tome"))
		{
			if(startCooldown(player, "tome", 90000))
				return;
			Material[] materials = new Material[]{Material.NETHER_STALK, Material.SPECKLED_MELON, Material.SUGAR, Material.SPIDER_EYE, Material.GLOWSTONE_DUST, Material.BLAZE_POWDER};
			for(int i = 0; i < 3; i++)
				player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(materials[random.nextInt(materials.length)], 1 + random.nextInt(3))));
			event.setCancelled(true);
		}
	}
}
