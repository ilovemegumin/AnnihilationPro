package vip.megumin.kits;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.KitUtils;

public class Tinkerer extends AnniKitBase
{
	private final Map<Location, UUID> padOwners = new HashMap<Location, UUID>();
	private final Map<Location, Pad> pads = new HashMap<Location, Pad>();

	public Tinkerer()
	{
		super("Tinkerer", Material.REDSTONE_BLOCK, url("Tinkerer"),
				lore("You are the pad.", "Place PowerPads that grant", "buffs to teammates who step on them."),
				stoneSword(), woodPick(), woodAxe(), named(Material.COAL_BLOCK, "Haste PowerPad"), named(Material.REDSTONE_BLOCK, "Speed PowerPad"), item(Material.BOOK, 10));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void place(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if(getCurrentPlayer(player) == null || event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Pad pad = padFor(event.getItem());
		if(pad == null)
			return;
		Block block = event.getClickedBlock().getRelative(BlockFace.UP);
		if(block.getType() != Material.AIR)
			return;
		block.setType(pad.block);
		block.getRelative(BlockFace.UP).setType(Material.STONE_PLATE);
		pads.put(block.getLocation(), pad);
		padOwners.put(block.getLocation(), player.getUniqueId());
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void step(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Location below = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
		Pad pad = pads.get(below);
		if(pad == null)
			return;
		UUID owner = padOwners.get(below);
		Player ownerPlayer = owner == null ? null : org.bukkit.Bukkit.getPlayer(owner);
		AnniPlayer ownerAnni = ownerPlayer == null ? null : AnniPlayer.getPlayer(owner);
		if(ownerAnni != null && isAlly(ownerAnni, player))
		{
			player.addPotionEffect(new PotionEffect(pad.effect, pad.durationTicks, pad.amplifier));
			if(ownerPlayer != null && !ownerPlayer.equals(player))
				ownerPlayer.giveExp(2);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void breakPad(BlockBreakEvent event)
	{
		Location location = event.getBlock().getLocation();
		if(event.getBlock().getType() == Material.STONE_PLATE)
			location = event.getBlock().getRelative(BlockFace.DOWN).getLocation();
		Pad pad = pads.remove(location);
		if(pad != null)
		{
			padOwners.remove(location);
			Block block = location.getBlock();
			if(block.getRelative(BlockFace.UP).getType() == Material.STONE_PLATE)
				block.getRelative(BlockFace.UP).setType(Material.AIR);
			if(block.getType() == pad.block)
				block.setType(Material.AIR);
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), KitUtils.addSoulbound(new ItemStack(pad.block, 4)));
		}
	}

	private Pad padFor(ItemStack item)
	{
		if(item == null)
			return null;
		if(namedLike(item, "Speed"))
			return Pad.SPEED;
		if(namedLike(item, "Haste"))
			return Pad.HASTE;
		if(item.getType() == Material.DIAMOND_BLOCK)
			return Pad.SPEED_II;
		if(item.getType() == Material.GOLD_BLOCK)
			return Pad.HASTE_II;
		if(item.getType() == Material.EMERALD_BLOCK)
			return Pad.ABSORPTION;
		return null;
	}

	private enum Pad
	{
		SPEED(Material.REDSTONE_BLOCK, PotionEffectType.SPEED, 0, 900),
		HASTE(Material.COAL_BLOCK, PotionEffectType.FAST_DIGGING, 0, 900),
		SPEED_II(Material.DIAMOND_BLOCK, PotionEffectType.SPEED, 1, 400),
		HASTE_II(Material.GOLD_BLOCK, PotionEffectType.FAST_DIGGING, 1, 300),
		ABSORPTION(Material.EMERALD_BLOCK, PotionEffectType.ABSORPTION, 0, 400);

		private final Material block;
		private final PotionEffectType effect;
		private final int amplifier;
		private final int durationTicks;

		Pad(Material block, PotionEffectType effect, int amplifier, int durationTicks)
		{
			this.block = block;
			this.effect = effect;
			this.amplifier = amplifier;
			this.durationTicks = durationTicks;
		}
	}
}
