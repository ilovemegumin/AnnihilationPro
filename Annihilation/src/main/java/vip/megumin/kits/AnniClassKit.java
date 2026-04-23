package vip.megumin.kits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import vip.megumin.anniPro.anniEvents.NexusHitEvent;
import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.anniGame.AnniTeam;
import vip.megumin.anniPro.anniGame.Game;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.anniPro.kits.Loadout;
import vip.megumin.anniPro.main.AnnihilationMain;
import vip.megumin.anniPro.voting.ConfigManager;
import vip.megumin.base.ConfigurableKit;

public class AnniClassKit extends ConfigurableKit
{
	private final String internalName;
	private final Material icon;
	private final String sourceUrl;
	private final List<String> description;
	private final List<KitItem> items;
	private final Map<String,Long> cooldowns = new HashMap<String,Long>();
	private final Map<UUID,ItemStack[]> hiddenArmor = new HashMap<UUID,ItemStack[]>();
	private final Map<UUID,Location> spyVanishStart = new HashMap<UUID,Location>();
	private final Map<UUID,String> selectedSpell = new HashMap<UUID,String>();
	private final Map<UUID,Long> markedPlayers = new HashMap<UUID,Long>();
	private final Map<UUID,UUID> projectileOwners = new HashMap<UUID,UUID>();
	private final Set<Location> temporaryBlocks = new HashSet<Location>();
	private final Set<UUID> aerialGrace = new HashSet<UUID>();
	private Random random;

	public AnniClassKit(String internalName, Material icon, String sourceUrl, List<String> description, KitItem... items)
	{
		this.internalName = internalName;
		this.icon = icon;
		this.sourceUrl = sourceUrl;
		this.description = description;
		this.items = Arrays.asList(items);
	}

	@Override
	protected void setUp()
	{
		random = new Random(System.currentTimeMillis());
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
		revealSpy(player);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer anniPlayer = getCurrentPlayer(player);
		if(anniPlayer == null)
			return;

		ItemStack item = event.getItem();
		if(item == null)
			return;

		boolean left = event.getAction().name().contains("LEFT");
		boolean right = event.getAction().name().contains("RIGHT");

		if(is("Alchemist") && right && namedLike(item, "Tome"))
		{
			if(startCooldown(player, "tome", 90000))
				return;
			giveAlchemySupplies(player);
			event.setCancelled(true);
		}
		else if(is("Bard") && right && namedLike(item, "Buffbox"))
		{
			if(startCooldown(player, "song", 15000))
				return;
			bardSong(player, anniPlayer);
			event.setCancelled(true);
		}
		else if(is("Bloodmage") && right && namedLike(item, "Corrupt"))
		{
			if(startCooldown(player, "corrupt", 60000))
				return;
			for(Player target : enemiesNear(player, anniPlayer, 4))
			{
				target.damage(3.0, player);
				target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
			}
			event.setCancelled(true);
		}
		else if(is("Bloodmage") && right && namedLike(item, "Terraform"))
		{
			if(startCooldown(player, "terraform", 120000))
				return;
			for(Player target : enemiesNear(player, anniPlayer, 8))
			{
				target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
				target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
				target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 200, 2));
			}
			event.setCancelled(true);
		}
		else if(is("Builder") && right && namedLike(item, "Resource Drop"))
		{
			if(startCooldown(player, "drop", 90000))
				return;
			giveBuilderBlocks(player);
			event.setCancelled(true);
		}
		else if(is("Builder") && right && namedLike(item, "Replication Cache"))
		{
			if(startCooldown(player, "cache", 45000))
				return;
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.WOOD, 64)));
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.STONE, 64)));
			event.setCancelled(true);
		}
		else if(is("Engineer") && right && item.getType() == Material.TNT)
		{
			if(startCooldown(player, "bomb", 30000))
				return;
			player.getWorld().createExplosion(player.getLocation().add(player.getLocation().getDirection().normalize().multiply(3)), 2.2F, false);
			event.setCancelled(true);
		}
		else if(is("Farmer") && right && namedLike(item, "Feast"))
		{
			if(startCooldown(player, "feast", 30000))
				return;
			for(Player target : alliesNear(player, anniPlayer, 13))
			{
				target.setFoodLevel(20);
				target.setSaturation(Math.max(target.getSaturation(), 4F));
				target.removePotionEffect(PotionEffectType.HUNGER);
			}
			event.setCancelled(true);
		}
		else if(is("Farmer") && right && namedLike(item, "Famine"))
		{
			if(startCooldown(player, "famine", 90000))
				return;
			for(Player target : enemiesNear(player, anniPlayer, 13))
				target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 600, 19));
			event.setCancelled(true);
		}
		else if(is("Healer") && namedLike(item, "Blood Bag"))
		{
			if(right)
			{
				if(startCooldown(player, "groupheal", 15000))
					return;
				healLowestAllies(player, anniPlayer);
			}
			else if(left)
			{
				if(startCooldown(player, "burstheal", 45000))
					return;
				Player target = getTargetPlayer(player, 6);
				if(target != null && isAlly(anniPlayer, target))
					healPlayer(target, 15.0);
			}
			event.setCancelled(true);
		}
		else if(is("Hunter") && right && namedLike(item, "Trap Snare"))
		{
			if(startCooldown(player, "trap", 40000))
				return;
			placeTemporaryWebs(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation(), 80);
			event.setCancelled(true);
		}
		else if(is("Immobilizer") && namedLike(item, "Immobilizer"))
		{
			if(startCooldown(player, "immobilize", 30000))
				return;
			if(right)
			{
				Player target = getTargetPlayer(player, 5);
				if(target != null && isEnemy(anniPlayer, target))
				{
					immobilize(target, 60);
					immobilize(player, 40);
				}
			}
			else if(left)
			{
				for(Player target : enemiesNear(player, anniPlayer, 5))
					target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
			}
			event.setCancelled(true);
		}
		else if(is("Mercenary") && right && namedLike(item, "Mark"))
		{
			Player target = getTargetPlayer(player, 20);
			if(target != null && isEnemy(anniPlayer, target))
			{
				markedPlayers.put(target.getUniqueId(), System.currentTimeMillis() + 30000);
				target.sendMessage(ChatColor.RED+"You have been marked!");
				player.sendMessage(ChatColor.GREEN+"Marked "+target.getName()+".");
			}
			event.setCancelled(true);
		}
		else if(is("Neptune") && right && (namedLike(item, "Tidebringer") || namedLike(item, "Ground Freeze")))
		{
			if(startCooldown(player, "tide", 30000))
				return;
			player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 600, 0));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
			for(Player target : enemiesNear(player, anniPlayer, 6))
				target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
			event.setCancelled(true);
		}
		else if(is("Ninja") && right && namedLike(item, "Smoke Bomb"))
		{
			if(startCooldown(player, "smoke", 40000))
				return;
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1));
			event.setCancelled(true);
		}
		else if(is("Ninja") && right && namedLike(item, "Shuriken"))
		{
			Snowball snowball = player.launchProjectile(Snowball.class);
			projectileOwners.put(snowball.getUniqueId(), player.getUniqueId());
			event.setCancelled(true);
		}
		else if(is("Ninja") && right && namedLike(item, "Ascension"))
		{
			togglePotion(player, PotionEffectType.JUMP, 1);
			event.setCancelled(true);
		}
		else if(is("Rift Walker") && right && namedLike(item, "Rift"))
		{
			if(startCooldown(player, "rift", 30000))
				return;
			beginRift(player, anniPlayer);
			event.setCancelled(true);
		}
		else if(is("Robin Hood") && right && namedLike(item, "Steed"))
		{
			if(startCooldown(player, "steed", 80000))
				return;
			Horse horse = (Horse)player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
			horse.setTamed(true);
			horse.setOwner(player);
			horse.setMaxHealth(30.0);
			horse.setHealth(30.0);
			horse.setPassenger(player);
			event.setCancelled(true);
		}
		else if(is("Robin Hood") && right && namedLike(item, "Aerial Agility"))
		{
			aerialGrace.add(player.getUniqueId());
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1));
			new BukkitRunnable(){
				@Override
				public void run()
				{
					aerialGrace.remove(player.getUniqueId());
				}
			}.runTaskLater(AnnihilationMain.getInstance(), 200);
			event.setCancelled(true);
		}
		else if(is("Spider") && right && item.getType() == Material.WEB)
		{
			placeTemporaryWebs(player.getTargetBlock((HashSet<Byte>)null, 8).getLocation(), 500);
			event.setCancelled(true);
		}
		else if(is("Spider") && right && namedLike(item, "Wall Climb"))
		{
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
			event.setCancelled(true);
		}
		else if(is("Tank") && right && namedLike(item, "Shield Charge"))
		{
			if(startCooldown(player, "charge", 60000))
				return;
			tankCharge(player, anniPlayer);
			event.setCancelled(true);
		}
		else if(is("Wizard") && right && namedLike(item, "Spellbook"))
		{
			cycleSpell(player);
			event.setCancelled(true);
		}
		else if(is("Wizard") && right && namedLike(item, "Wand"))
		{
			castWizardSpell(player, anniPlayer);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer anniPlayer = getCurrentPlayer(player);
		if(anniPlayer == null)
			return;

		if(is("Dasher") && event.isSneaking())
			dash(player);
		else if(is("Spy"))
		{
			if(event.isSneaking())
				beginSpyVanish(player);
			else
				revealSpy(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer anniPlayer = getCurrentPlayer(player);
		if(anniPlayer == null)
			return;

		if(is("Spy") && spyVanishStart.containsKey(player.getUniqueId()) && event.getTo().distanceSquared(spyVanishStart.get(player.getUniqueId())) > 9)
			revealSpy(player);

		if(is("Tinkerer"))
		{
			Material below = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
			if(below == Material.REDSTONE_BLOCK)
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 900, 0));
			else if(below == Material.COAL_BLOCK)
				player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 900, 0));
			else if(below == Material.DIAMOND_BLOCK)
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1));
			else if(below == Material.GOLD_BLOCK)
				player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 300, 1));
			else if(below == Material.EMERALD_BLOCK)
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 400, 0));
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if(event.getEntityType() != EntityType.PLAYER)
			return;

		Player victim = (Player)event.getEntity();
		Player damager = getDamagingPlayer(event);
		if(damager == null)
			return;

		AnniPlayer attacker = getCurrentPlayer(damager);
		if(attacker != null)
		{
			if(is("Bloodmage") && random.nextInt(100) < 25)
				victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
			else if(is("Mercenary") && markedPlayers.containsKey(victim.getUniqueId()) && markedPlayers.get(victim.getUniqueId()) > System.currentTimeMillis())
				event.setDamage(event.getDamage() + 1.0);
			else if(is("Spy") && isBehind(damager, victim))
				event.setDamage(event.getDamage() + 1.0);
		}

		AnniPlayer victimPlayer = getCurrentPlayer(victim);
		if(victimPlayer != null && is("Spy"))
			revealSpy(victim);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event)
	{
		if(is("Robin Hood") && event.getEntityType() == EntityType.PLAYER && event.getCause() == EntityDamageEvent.DamageCause.FALL && aerialGrace.contains(event.getEntity().getUniqueId()))
			event.setCancelled(true);
		if(is("Spider") && event.getEntityType() == EntityType.PLAYER && event.getCause() == EntityDamageEvent.DamageCause.FALL)
		{
			Player player = (Player)event.getEntity();
			AnniPlayer p = getCurrentPlayer(player);
			if(p != null && event.getDamage() >= player.getHealth())
			{
				event.setDamage(Math.max(0, player.getHealth() - 2));
				player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 0));
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		AnniPlayer anniPlayer = getCurrentPlayer(player);
		if(anniPlayer == null)
			return;

		if(is("Farmer") && (event.getBlock().getType() == Material.CROPS || event.getBlock().getType() == Material.LONG_GRASS) && random.nextInt(100) == 0)
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(random.nextBoolean() ? Material.GOLD_ORE : Material.IRON_ORE));
		else if(is("Builder"))
			player.giveExp(2);
		else if(is("Spy"))
			revealSpy(player);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onNexusHit(NexusHitEvent event)
	{
		AnniPlayer player = event.getPlayer();
		if(player == null || player.getKit() == null || !player.getKit().equals(this))
			return;
		if(is("Handyman") && player.getTeam() != null && !event.getHitNexus().Team.equals(player.getTeam()) && random.nextInt(100) < 35)
		{
			AnniTeam team = player.getTeam();
			team.setHealth(team.getHealth() + 1);
			Player bukkitPlayer = player.getPlayer();
			if(bukkitPlayer != null)
				bukkitPlayer.sendMessage(ChatColor.GREEN+"Your nexus was repaired by 1.");
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event)
	{
		ProjectileSource source = event.getEntity().getShooter();
		if(source instanceof Player)
		{
			Player player = (Player)source;
			AnniPlayer p = getCurrentPlayer(player);
			if(p != null && is("Sniper") && event.getEntity() instanceof Arrow)
				event.getEntity().setVelocity(event.getEntity().getVelocity().multiply(3.0));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onProjectileHit(ProjectileHitEvent event)
	{
		UUID owner = projectileOwners.remove(event.getEntity().getUniqueId());
		if(owner != null)
		{
			Player player = Bukkit.getPlayer(owner);
			if(player != null)
			{
				for(Entity entity : event.getEntity().getNearbyEntities(2, 2, 2))
					if(entity.getType() == EntityType.PLAYER)
						((Player)entity).damage(4.0, player);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFood(FoodLevelChangeEvent event)
	{
		if(event.getEntityType() == EntityType.PLAYER)
		{
			Player player = (Player)event.getEntity();
			AnniPlayer p = getCurrentPlayer(player);
			if(p != null && is("Farmer") && event.getFoodLevel() > player.getFoodLevel() && random.nextInt(100) < 30)
				player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 2.0));
		}
	}

	private AnniPlayer getCurrentPlayer(Player player)
	{
		if(player == null)
			return null;
		AnniPlayer p = AnniPlayer.getPlayer(player.getUniqueId());
		return p != null && p.getKit() != null && p.getKit().equals(this) ? p : null;
	}

	private boolean is(String name)
	{
		return internalName.equalsIgnoreCase(name);
	}

	private boolean namedLike(ItemStack item, String text)
	{
		if(item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
			return false;
		return ChatColor.stripColor(item.getItemMeta().getDisplayName()).toLowerCase().contains(text.toLowerCase());
	}

	private boolean startCooldown(Player player, String key, long length)
	{
		String mapKey = player.getUniqueId().toString()+":"+key;
		Long ends = cooldowns.get(mapKey);
		if(ends != null && ends.longValue() > System.currentTimeMillis())
		{
			player.sendMessage(ChatColor.RED+"Cooldown: "+((ends.longValue()-System.currentTimeMillis()+999)/1000)+"s");
			return true;
		}
		cooldowns.put(mapKey, System.currentTimeMillis()+length);
		return false;
	}

	private List<Player> alliesNear(Player player, AnniPlayer anniPlayer, int radius)
	{
		List<Player> players = new ArrayList<Player>();
		players.add(player);
		for(Entity entity : player.getNearbyEntities(radius, radius, radius))
			if(entity.getType() == EntityType.PLAYER && isAlly(anniPlayer, (Player)entity))
				players.add((Player)entity);
		return players;
	}

	private List<Player> enemiesNear(Player player, AnniPlayer anniPlayer, int radius)
	{
		List<Player> players = new ArrayList<Player>();
		for(Entity entity : player.getNearbyEntities(radius, radius, radius))
			if(entity.getType() == EntityType.PLAYER && isEnemy(anniPlayer, (Player)entity))
				players.add((Player)entity);
		return players;
	}

	private boolean isAlly(AnniPlayer player, Player target)
	{
		AnniPlayer targetPlayer = AnniPlayer.getPlayer(target.getUniqueId());
		return player != null && targetPlayer != null && player.getTeam() != null && player.getTeam().equals(targetPlayer.getTeam());
	}

	private boolean isEnemy(AnniPlayer player, Player target)
	{
		AnniPlayer targetPlayer = AnniPlayer.getPlayer(target.getUniqueId());
		return player != null && targetPlayer != null && player.getTeam() != null && targetPlayer.getTeam() != null && !player.getTeam().equals(targetPlayer.getTeam());
	}

	private Player getTargetPlayer(Player player, int distance)
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

	private Player getDamagingPlayer(EntityDamageByEntityEvent event)
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

	private void giveAlchemySupplies(Player player)
	{
		Material[] materials = new Material[]{Material.NETHER_STALK, Material.SPECKLED_MELON, Material.SUGAR, Material.SPIDER_EYE, Material.GLOWSTONE_DUST, Material.BLAZE_POWDER};
		for(int i = 0; i < 3; i++)
			player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(materials[random.nextInt(materials.length)], 1 + random.nextInt(3))));
	}

	private void bardSong(Player player, AnniPlayer anniPlayer)
	{
		for(Player ally : alliesNear(player, anniPlayer, 15))
		{
			ally.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 400, 0));
			ally.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 500, 0));
		}
		for(Player enemy : enemiesNear(player, anniPlayer, 15))
		{
			enemy.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 400, 2));
			enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 1));
		}
	}

	private void giveBuilderBlocks(Player player)
	{
		player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.WOOD, 64)));
		player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.DIRT, 48)));
		player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.STONE, 32)));
		player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.BRICK, 16)));
		player.getInventory().addItem(KitUtils.addSoulbound(new ItemStack(Material.THIN_GLASS, 16)));
	}

	private void healLowestAllies(Player player, AnniPlayer anniPlayer)
	{
		List<Player> allies = alliesNear(player, anniPlayer, 6);
		int healed = 0;
		while(healed < 3 && !allies.isEmpty())
		{
			Player lowest = allies.get(0);
			for(Player ally : allies)
				if(ally.getHealth() < lowest.getHealth())
					lowest = ally;
			lowest.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 2));
			allies.remove(lowest);
			healed++;
		}
	}

	private void healPlayer(Player target, double amount)
	{
		target.setHealth(Math.min(target.getMaxHealth(), target.getHealth()+amount));
		target.removePotionEffect(PotionEffectType.POISON);
		target.removePotionEffect(PotionEffectType.WITHER);
		target.removePotionEffect(PotionEffectType.SLOW);
		target.removePotionEffect(PotionEffectType.WEAKNESS);
	}

	private void immobilize(Player player, int ticks)
	{
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 10));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ticks, 128));
	}

	private void dash(Player player)
	{
		int distance = 10;
		if(startCooldown(player, "dash", distance * 1000L))
			return;
		Location from = player.getLocation();
		Location target = from.clone();
		Vector direction = from.getDirection().normalize();
		for(int i = 0; i < distance; i++)
		{
			Location next = target.clone().add(direction);
			if(next.getBlock().getType().isSolid() || next.clone().add(0, 1, 0).getBlock().getType().isSolid())
				break;
			target = next;
		}
		player.teleport(target);
	}

	private void beginSpyVanish(final Player player)
	{
		spyVanishStart.put(player.getUniqueId(), player.getLocation());
		new BukkitRunnable(){
			@Override
			public void run()
			{
				if(spyVanishStart.containsKey(player.getUniqueId()) && player.isSneaking() && getCurrentPlayer(player) != null)
				{
					hiddenArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());
					player.getInventory().setArmorContents(null);
					player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
				}
			}
		}.runTaskLater(AnnihilationMain.getInstance(), 40);
	}

	private void revealSpy(Player player)
	{
		if(player == null)
			return;
		ItemStack[] armor = hiddenArmor.remove(player.getUniqueId());
		if(armor != null)
			player.getInventory().setArmorContents(armor);
		spyVanishStart.remove(player.getUniqueId());
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	private boolean isBehind(Player attacker, Player victim)
	{
		Vector victimDirection = victim.getLocation().getDirection().normalize();
		Vector attackerDirection = attacker.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
		return victimDirection.dot(attackerDirection) < -0.5;
	}

	private void beginRift(final Player player, final AnniPlayer anniPlayer)
	{
		final Location start = player.getLocation();
		player.sendMessage(ChatColor.GREEN+"Rift opening in 10 seconds.");
		new BukkitRunnable(){
			@Override
			public void run()
			{
				if(getCurrentPlayer(player) == null || player.getLocation().distanceSquared(start) > 9 || anniPlayer.getTeam() == null)
					return;
				Location spawn = anniPlayer.getTeam().getRandomSpawn();
				if(spawn != null)
				{
					player.teleport(spawn);
					player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
				}
			}
		}.runTaskLater(AnnihilationMain.getInstance(), 200);
	}

	private void placeTemporaryWebs(Location center, int ticks)
	{
		for(int x = -1; x <= 1; x++)
			for(int z = -1; z <= 1; z++)
			{
				final Block block = center.clone().add(x, 0, z).getBlock();
				if(block.getType() == Material.AIR)
				{
					block.setType(Material.WEB);
					temporaryBlocks.add(block.getLocation());
					new BukkitRunnable(){
						@Override
						public void run()
						{
							if(temporaryBlocks.remove(block.getLocation()) && block.getType() == Material.WEB)
								block.setType(Material.AIR);
						}
					}.runTaskLater(AnnihilationMain.getInstance(), ticks);
				}
			}
	}

	private void togglePotion(Player player, PotionEffectType type, int level)
	{
		if(player.hasPotionEffect(type))
			player.removePotionEffect(type);
		else
			player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, level));
	}

	private void tankCharge(final Player player, final AnniPlayer anniPlayer)
	{
		player.setVelocity(player.getLocation().getDirection().normalize().multiply(1.8).setY(0.2));
		new BukkitRunnable(){
			int ticks = 0;
			@Override
			public void run()
			{
				if(ticks++ > 20 || getCurrentPlayer(player) == null)
				{
					cancel();
					return;
				}
				for(Player target : enemiesNear(player, anniPlayer, 2))
				{
					target.damage(2.5, player);
					target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
				}
			}
		}.runTaskTimer(AnnihilationMain.getInstance(), 0, 2);
	}

	private void cycleSpell(Player player)
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
		player.sendMessage(ChatColor.AQUA+"Selected spell: "+next);
	}

	private void castWizardSpell(Player player, AnniPlayer anniPlayer)
	{
		if(startCooldown(player, "spell", 15000))
			return;
		String spell = selectedSpell.get(player.getUniqueId());
		if(spell == null)
			spell = "Inferno";
		Player target = getTargetPlayer(player, 30);
		Location center = target == null ? player.getLocation().add(player.getLocation().getDirection().normalize().multiply(8)) : target.getLocation();
		int radius = "Whirlwind".equals(spell) ? 3 : ("Glacial Nova".equals(spell) ? 2 : 1);
		for(Player enemy : enemiesNearAt(center, anniPlayer, radius))
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

	private List<Player> enemiesNearAt(Location center, AnniPlayer anniPlayer, int radius)
	{
		List<Player> players = new ArrayList<Player>();
		if(center.getWorld() == null)
			return players;
		for(Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius))
			if(entity.getType() == EntityType.PLAYER && isEnemy(anniPlayer, (Player)entity))
				players.add((Player)entity);
		return players;
	}

	static List<String> lore(String... lines)
	{
		List<String> lore = new ArrayList<String>();
		for(String line : lines)
			lore.add(line.length() == 0 ? "" : ChatColor.AQUA + line);
		return lore;
	}

	static KitItem item(Material material)
	{
		return new KitItem(material, 1, null, null, 0);
	}

	static KitItem item(Material material, int amount)
	{
		return new KitItem(material, amount, null, null, 0);
	}

	static KitItem named(Material material, String name)
	{
		return new KitItem(material, 1, name, null, 0);
	}

	static KitItem enchanted(Material material, String name, Enchantment enchantment, int level)
	{
		return new KitItem(material, 1, name, enchantment, level);
	}

	static class KitItem
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
