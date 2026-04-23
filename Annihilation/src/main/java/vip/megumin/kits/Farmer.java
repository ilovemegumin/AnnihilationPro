package vip.megumin.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public class Farmer extends AnniKitBase
{
	public Farmer()
	{
		super("Farmer", Material.SEEDS, url("Farmer"),
				lore("You are the harvest.", "Crops and grass can yield supplies.", "Feast feeds allies; Famine starves enemies."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.GOLDEN_CARROT, "Feast"), named(Material.DEAD_BUSH, "Famine"), item(Material.STONE_HOE), item(Material.INK_SACK, 15), item(Material.CHEST));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p == null || !event.getAction().name().contains("RIGHT"))
			return;
		if(namedLike(event.getItem(), "Feast"))
		{
			if(startCooldown(player, "feast", 30000))
				return;
			for(Player target : alliesNear(player, p, 13))
			{
				target.setFoodLevel(20);
				target.setSaturation(Math.max(target.getSaturation(), 4F));
				target.removePotionEffect(PotionEffectType.HUNGER);
			}
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Famine"))
		{
			if(startCooldown(player, "famine", 90000))
				return;
			for(Player target : enemiesNear(player, p, 13))
				target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 600, 19));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void harvest(BlockBreakEvent event)
	{
		if(getCurrentPlayer(event.getPlayer()) != null && (event.getBlock().getType() == Material.CROPS || event.getBlock().getType() == Material.LONG_GRASS) && random.nextInt(100) == 0)
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(random.nextBoolean() ? Material.GOLD_ORE : Material.IRON_ORE));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void eat(FoodLevelChangeEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			if(getCurrentPlayer(player) != null && event.getFoodLevel() > player.getFoodLevel() && random.nextInt(100) < 30)
				player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 2.0));
		}
	}
}
