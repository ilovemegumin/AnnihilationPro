package vip.megumin.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.itemMenus.ActionMenuItem;
import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.ItemClickHandler;
import vip.megumin.anniPro.itemMenus.ItemMenu;
import vip.megumin.anniPro.itemMenus.ItemMenu.Size;

public class Wizard extends AnniKitBase
{
	private final Map<UUID, String> selectedSpell = new HashMap<UUID, String>();
	private final Map<UUID, ItemMenu> menus = new HashMap<UUID, ItemMenu>();

	public Wizard()
	{
		super("Wizard", Material.STICK, url("Wizard"),
				lore("You are the spell.", "Spellbook selects magic", "and Wand casts the selected spell."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.STICK, "Wand"), named(Material.BOOK, "Spellbook"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer p = getCurrentPlayer(player);
		if(p == null || !event.getAction().name().contains("RIGHT"))
			return;
		if(namedLike(event.getItem(), "Spellbook"))
		{
			menu(player).open(player);
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Wand"))
		{
			cast(player, p);
			event.setCancelled(true);
		}
	}

	private ItemMenu menu(final Player player)
	{
		ItemMenu menu = menus.get(player.getUniqueId());
		if(menu != null)
			return menu;
		menu = new ItemMenu("Wizard Spells", Size.ONE_LINE);
		addSpell(menu, 0, player, "Inferno", Material.FLINT_AND_STEEL, "50s cooldown: set enemies on fire.");
		addSpell(menu, 1, player, "Void Bolt", Material.ENDER_PEARL, "50s cooldown: wither enemies.");
		addSpell(menu, 2, player, "Arcane Bolt", Material.NETHER_STAR, "30s cooldown: area magic damage.");
		addSpell(menu, 3, player, "Glacial Nova", Material.ICE, "35s cooldown: slow and fatigue enemies.");
		addSpell(menu, 4, player, "Whirlwind", Material.FEATHER, "25s cooldown: knock enemies away.");
		menus.put(player.getUniqueId(), menu);
		return menu;
	}

	private void addSpell(ItemMenu menu, int slot, final Player player, final String spell, Material icon, String lore)
	{
		menu.setItem(slot, new ActionMenuItem(ChatColor.AQUA+spell, new ItemClickHandler(){
			@Override
			public void onItemClick(ItemClickEvent event)
			{
				selectedSpell.put(player.getUniqueId(), spell);
				player.sendMessage(ChatColor.AQUA+"Selected spell: "+spell);
				event.setWillClose(true);
			}
		}, new org.bukkit.inventory.ItemStack(icon), ChatColor.GRAY+lore));
	}

	private void cast(Player player, AnniPlayer p)
	{
		String spell = selectedSpell.get(player.getUniqueId());
		if(spell == null)
			spell = "Inferno";
		if(startCooldown(player, "spell:"+spell, spellCooldown(spell)))
			return;
		Player target = getTargetPlayer(player, 30);
		Location center = target == null ? player.getLocation().add(player.getLocation().getDirection().normalize().multiply(8)) : target.getLocation();
		int radius = "Whirlwind".equals(spell) ? 3 : ("Glacial Nova".equals(spell) ? 2 : 1);
		for(Player enemy : enemiesNear(center, p, radius))
		{
			if("Inferno".equals(spell))
				enemy.setFireTicks(200);
			else if("Void Bolt".equals(spell))
				enemy.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
			else if("Arcane Bolt".equals(spell))
				enemy.damage(7.0, player);
			else if("Glacial Nova".equals(spell))
			{
				enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2));
				enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 0));
			}
			else if("Whirlwind".equals(spell))
				enemy.setVelocity(enemy.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5).setY(0.7));
		}
		player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1F, 1.5F);
	}

	private long spellCooldown(String spell)
	{
		if("Inferno".equals(spell) || "Void Bolt".equals(spell))
			return 50000;
		if("Arcane Bolt".equals(spell))
			return 30000;
		if("Glacial Nova".equals(spell))
			return 35000;
		return 25000;
	}
}
