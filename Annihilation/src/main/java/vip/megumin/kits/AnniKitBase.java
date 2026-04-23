package vip.megumin.kits;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.anniPro.voting.ConfigManager;
import vip.megumin.base.ConfigurableKit;

public abstract class AnniKitBase extends ConfigurableKit
{
	private final String internalName;
	private final Material icon;
	private final String sourceUrl;
	private final List<String> description;
	private final List<KitItem> items;
	private final Map<String, Long> cooldowns = new HashMap<String, Long>();
	protected final Random random = new Random(System.currentTimeMillis());

	protected AnniKitBase(String internalName, Material icon, String sourceUrl, List<String> description, KitItem... items)
	{
		this.internalName = internalName;
		this.icon = icon;
		this.sourceUrl = sourceUrl;
		this.description = description;
		this.items = new ArrayList<KitItem>();
        Collections.addAll(this.items, items);
	}

	@Override
	protected void setUp()
	{
	}

	@Override
	protected String getInternalName()
	{
		return internalName;
	}

	@Override
	protected ItemStack getIcon()
	{
		return new ItemStack(icon);
	}

	@Override
	protected int setDefaults(ConfigurationSection section)
	{
		return ConfigManager.setDefaultIfNotSet(section, "Class Source", sourceUrl);
	}

	@Override
	protected List<String> getDefaultDescription()
	{
		return description;
	}

	@Override
	protected Loadout getFinalLoadout()
	{
		Loadout loadout = new Loadout();
		for(KitItem item : items)
			loadout.addSoulboundItem(item.toItemStack());
		return loadout;
	}

	@Override
	public void cleanup(Player player)
	{
	}

	protected boolean startCooldown(Player player, String key, long length)
	{
		String mapKey = player.getUniqueId().toString() + ":" + key;
		Long ends = cooldowns.get(mapKey);
		if(ends != null && ends.longValue() > System.currentTimeMillis())
		{
			player.sendMessage(ChatColor.RED + "Cooldown: " + ((ends.longValue() - System.currentTimeMillis() + 999) / 1000) + "s");
			return true;
		}
		cooldowns.put(mapKey, System.currentTimeMillis() + length);
		return false;
	}

	protected AnniPlayer getCurrentPlayer(Player player)
	{
		if(player == null)
			return null;
		AnniPlayer p = AnniPlayer.getPlayer(player.getUniqueId());
		return p != null && p.getKit() != null && p.getKit().equals(this) ? p : null;
	}

	protected boolean namedLike(ItemStack item, String text)
	{
		if(item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
			return false;
		return ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase().contains(text.toLowerCase());
	}

	protected List<Player> alliesNear(Player player, AnniPlayer anniPlayer, int radius)
	{
		List<Player> players = new ArrayList<Player>();
		players.add(player);
		for(Entity entity : player.getNearbyEntities(radius, radius, radius))
			if(entity.getType() == EntityType.PLAYER && isAlly(anniPlayer, (Player)entity))
				players.add((Player)entity);
		return players;
	}

	protected List<Player> enemiesNear(Player player, AnniPlayer anniPlayer, int radius)
	{
		List<Player> players = new ArrayList<Player>();
		for(Entity entity : player.getNearbyEntities(radius, radius, radius))
			if(entity.getType() == EntityType.PLAYER && isEnemy(anniPlayer, (Player)entity))
				players.add((Player)entity);
		return players;
	}

	protected List<Player> enemiesNear(Location center, AnniPlayer anniPlayer, int radius)
	{
		List<Player> players = new ArrayList<Player>();
		if(center.getWorld() == null)
			return players;
		for(Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius))
			if(entity.getType() == EntityType.PLAYER && isEnemy(anniPlayer, (Player)entity))
				players.add((Player)entity);
		return players;
	}

	protected boolean isAlly(AnniPlayer player, Player target)
	{
		AnniPlayer targetPlayer = AnniPlayer.getPlayer(target.getUniqueId());
		return player != null && targetPlayer != null && player.getTeam() != null && player.getTeam().equals(targetPlayer.getTeam());
	}

	protected boolean isEnemy(AnniPlayer player, Player target)
	{
		AnniPlayer targetPlayer = AnniPlayer.getPlayer(target.getUniqueId());
		return player != null && targetPlayer != null && player.getTeam() != null && targetPlayer.getTeam() != null && !player.getTeam().equals(targetPlayer.getTeam());
	}

	protected Player getTargetPlayer(Player player, int distance)
	{
		Player best = null;
		double bestDistance = distance * distance;
		for(Entity entity : player.getNearbyEntities(distance, distance, distance))
		{
			if(entity.getType() == EntityType.PLAYER)
			{
				Vector toEntity = entity.getLocation().toVector().subtract(player.getEyeLocation().toVector());
				double angle = toEntity.normalize().dot(player.getLocation().getDirection());
				double dist = entity.getLocation().distanceSquared(player.getLocation());
				if(angle > 0.97 && dist < bestDistance)
				{
					best = (Player)entity;
					bestDistance = dist;
				}
			}
		}
		return best;
	}

	protected Player getDamagingPlayer(EntityDamageByEntityEvent event)
	{
		if(event.getDamager().getType() == EntityType.PLAYER)
			return (Player)event.getDamager();
		if(event.getDamager() instanceof Arrow)
		{
			ProjectileSource source = ((Arrow)event.getDamager()).getShooter();
			if(source instanceof Player)
				return (Player)source;
		}
		return null;
	}

	protected void healPlayer(Player target, double amount)
	{
		target.setHealth(Math.min(target.getMaxHealth(), target.getHealth() + amount));
		target.removePotionEffect(org.bukkit.potion.PotionEffectType.POISON);
		target.removePotionEffect(org.bukkit.potion.PotionEffectType.WITHER);
		target.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
		target.removePotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS);
	}

	protected boolean isBehind(Player attacker, Player victim)
	{
		Vector victimDirection = victim.getLocation().getDirection().normalize();
		Vector attackerDirection = attacker.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
		return victimDirection.dot(attackerDirection) < -0.5;
	}

	protected void addTempBlock(final org.bukkit.block.Block block, final Material material, int ticks, final HashSet<Location> tracked)
	{
		if(block.getType() == Material.AIR)
		{
			block.setType(material);
			tracked.add(block.getLocation());
			new org.bukkit.scheduler.BukkitRunnable(){
				@Override
				public void run()
				{
					if(tracked.remove(block.getLocation()) && block.getType() == material)
						block.setType(Material.AIR);
				}
			}.runTaskLater(vip.megumin.anniPro.main.AnnihilationMain.getInstance(), ticks);
		}
	}

	protected static List<String> lore(String... lines)
	{
		List<String> lore = new ArrayList<String>();
		for(String line : lines)
			lore.add(line.length() == 0 ? "" : ChatColor.AQUA + line);
		return lore;
	}

	protected static KitItem item(Material material)
	{
		return new KitItem(material, 1, null, null, 0);
	}

	protected static KitItem item(Material material, int amount)
	{
		return new KitItem(material, amount, null, null, 0);
	}

	protected static KitItem named(Material material, String name)
	{
		return new KitItem(material, 1, name, null, 0);
	}

	protected static KitItem enchanted(Material material, String name, Enchantment enchantment, int level)
	{
		return new KitItem(material, 1, name, enchantment, level);
	}

	protected static String url(String page)
	{
		return "https://wiki.shotbow.net/" + page;
	}

	protected static KitItem woodSword()
	{
		return item(Material.WOOD_SWORD);
	}

	protected static KitItem stoneSword()
	{
		return item(Material.STONE_SWORD);
	}

	protected static KitItem goldSword()
	{
		return item(Material.GOLD_SWORD);
	}

	protected static KitItem woodPick()
	{
		return item(Material.WOOD_PICKAXE);
	}

	protected static KitItem woodAxe()
	{
		return item(Material.WOOD_AXE);
	}

	protected static KitItem woodShovel()
	{
		return item(Material.WOOD_SPADE);
	}

	protected static final class KitItem
	{
		private final Material material;
		private final int amount;
		private final String name;
		private final Enchantment enchantment;
		private final int level;

		private KitItem(Material material, int amount, String name, Enchantment enchantment, int level)
		{
			this.material = material;
			this.amount = amount;
			this.name = name;
			this.enchantment = enchantment;
			this.level = level;
		}

		private ItemStack toItemStack()
		{
			ItemStack stack = new ItemStack(material, amount);
			if(name != null)
				KitUtils.setName(stack, name);
			if(enchantment != null)
				KitUtils.addEnchant(stack, enchantment, level);
			return stack;
		}
	}
}
