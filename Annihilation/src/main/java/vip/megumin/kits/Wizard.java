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

public class Wizard extends AnniKitBase
{
	private final Map<UUID, String> selectedSpell = new HashMap<UUID, String>();

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
			cycle(player);
			event.setCancelled(true);
		}
		else if(namedLike(event.getItem(), "Wand"))
		{
			cast(player, p);
			event.setCancelled(true);
		}
	}

	private void cycle(Player player)
	{
		String current = selectedSpell.get(player.getUniqueId());
		String next = "Inferno";
		if("Inferno".equals(current))
			next = "Void Bolt";
		else if("Void Bolt".equals(current))
			next = "Arcane Bolt";
		else if("Arcane Bolt".equals(current))
			next = "Glacial Nova";
		else if("Glacial Nova".equals(current))
			next = "Whirlwind";
		selectedSpell.put(player.getUniqueId(), next);
		player.sendMessage(ChatColor.AQUA + "Selected spell: " + next);
	}

	private void cast(Player player, AnniPlayer p)
	{
		if(startCooldown(player, "spell", 15000))
			return;
		String spell = selectedSpell.get(player.getUniqueId());
		if(spell == null)
			spell = "Inferno";
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
}
