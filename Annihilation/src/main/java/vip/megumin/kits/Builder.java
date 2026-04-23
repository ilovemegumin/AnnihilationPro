package vip.megumin.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.itemMenus.ActionMenuItem;
import vip.megumin.anniPro.itemMenus.ItemClickEvent;
import vip.megumin.anniPro.itemMenus.ItemClickHandler;
import vip.megumin.anniPro.itemMenus.ItemMenu;
import vip.megumin.anniPro.itemMenus.ItemMenu.Size;
import vip.megumin.anniPro.kits.KitUtils;

public class Builder extends AnniKitBase
{
	private static final String DELAYING_BLOCK_NAME = ChatColor.GOLD+"Delaying Block";
	private final Map<UUID, Long> xpCooldowns = new HashMap<UUID, Long>();
	private final Map<Location, UUID> delayingBlocks = new HashMap<Location, UUID>();

	public Builder()
	{
		super("Builder", Material.BRICK, url("Builder"),
				lore("You are the stone.", "Resource Drop opens a building supply inventory.", "Delaying Blocks punish enemies breaking nearby blocks."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), named(Material.BOOK, "Resource Drop"));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void use(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) == null)
			return;
		if(event.getAction().name().contains("RIGHT") && namedLike(event.getItem(), "Resource Drop"))
		{
			if(startCooldown(player, "drop", 90000))
				return;
			resourceDrop(player).open(player);
			event.setCancelled(true);
		}
		else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && delayingBlocks.containsKey(event.getClickedBlock().getLocation()))
		{
			showRange(player, event.getClickedBlock().getLocation());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void place(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if(KitUtils.itemHasName(event.getItemInHand(), DELAYING_BLOCK_NAME))
		{
			delayingBlocks.put(event.getBlock().getLocation(), player.getUniqueId());
			return;
		}
		if(getCurrentPlayer(player) != null)
		{
			Long next = xpCooldowns.get(player.getUniqueId());
			if(next == null || next.longValue() <= System.currentTimeMillis())
			{
				player.giveExp(2);
				xpCooldowns.put(player.getUniqueId(), System.currentTimeMillis()+1500);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void breakBlock(BlockBreakEvent event)
	{
		Location broken = event.getBlock().getLocation();
		UUID blockOwner = delayingBlocks.get(broken);
		if(blockOwner != null)
		{
			AnniPlayer owner = AnniPlayer.getPlayer(blockOwner);
			AnniPlayer breaker = AnniPlayer.getPlayer(event.getPlayer().getUniqueId());
			if(owner != null && breaker != null && owner.getTeam() != null && owner.getTeam().equals(breaker.getTeam()))
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED+"Allies cannot break this Delaying Block.");
				return;
			}
			delayingBlocks.remove(broken);
			event.getBlock().getWorld().dropItemNaturally(broken, delayingBlock());
			return;
		}

		for(Map.Entry<Location, UUID> entry : delayingBlocks.entrySet())
		{
			if(!entry.getKey().getWorld().equals(broken.getWorld()) || entry.getKey().distanceSquared(broken) > 25)
				continue;
			AnniPlayer owner = AnniPlayer.getPlayer(entry.getValue());
			AnniPlayer breaker = AnniPlayer.getPlayer(event.getPlayer().getUniqueId());
			if(owner != null && breaker != null && owner.getTeam() != null && !owner.getTeam().equals(breaker.getTeam()))
			{
				int amplifier = breaker.getKit() != null && breaker.getKit().getName().equalsIgnoreCase("Engineer") ? 0 : 1;
				event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 140, amplifier));
				event.getPlayer().playSound(event.getPlayer().getLocation(), org.bukkit.Sound.NOTE_BASS, 1F, 0.5F);
				break;
			}
		}
	}

	private ItemMenu resourceDrop(final Player player)
	{
		ItemMenu menu = new ItemMenu("Resource Drop", Size.THREE_LINE);
		int slot = 0;
		slot = addResource(menu, slot, Material.DIRT, 33 + random.nextInt(26));
		slot = addResource(menu, slot, Material.BRICK, random.nextInt(37));
		slot = addResource(menu, slot, Material.STONE, 25 + random.nextInt(21));
		slot = addResource(menu, slot, Material.WOOD, random.nextInt(100) < 25 ? 0 : 32 + random.nextInt(33));
		slot = addResource(menu, slot, Material.WOOL, 18 + random.nextInt(11));
		slot = addResource(menu, slot, Material.IRON_FENCE, 5 + random.nextInt(5));
		slot = addResource(menu, slot, Material.FENCE, random.nextInt(10));
		slot = addResource(menu, slot, Material.SPRUCE_WOOD_STAIRS, random.nextInt(15));
		slot = addResource(menu, slot, Material.GLASS, random.nextInt(20));
		slot = addResource(menu, slot, Material.TORCH, 2 + random.nextInt(2));
		if(random.nextInt(100) < 30)
			addMenuItem(menu, slot++, delayingBlock());
		return menu;
	}

	private int addResource(ItemMenu menu, int slot, Material material, int amount)
	{
		if(amount <= 0)
			return slot;
		addMenuItem(menu, slot, KitUtils.addSoulbound(new ItemStack(material, amount)));
		return slot + 1;
	}

	private void addMenuItem(ItemMenu menu, int slot, final ItemStack item)
	{
		final ItemMenu resourceMenu = menu;
		final int resourceSlot = slot;
		menu.setItem(slot, new ActionMenuItem(ChatColor.GREEN+item.getType().name()+" x"+item.getAmount(), new ItemClickHandler(){
			@Override
			public void onItemClick(ItemClickEvent event)
			{
				event.getPlayer().getInventory().addItem(item.clone());
				resourceMenu.clearItem(resourceSlot);
				event.setWillUpdate(true);
			}
		}, item.clone(), ChatColor.GRAY+"Click to take this resource."));
	}

	public static ItemStack delayingBlock()
	{
		return KitUtils.setName(new ItemStack(Material.LAPIS_BLOCK), DELAYING_BLOCK_NAME);
	}

	private void showRange(Player player, Location center)
	{
		for(int x = -5; x <= 5; x++)
			for(int z = -5; z <= 5; z++)
				if(x*x + z*z <= 25)
					player.sendBlockChange(center.clone().add(x, 0, z), Material.STAINED_GLASS, (byte)4);
	}
}
