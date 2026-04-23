package vip.megumin.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import vip.megumin.anniPro.anniEvents.NexusHitEvent;
import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.anniGame.AnniTeam;
import vip.megumin.anniPro.anniGame.Game;

public class Handyman extends AnniKitBase
{
	public Handyman()
	{
		super("Handyman", Material.ANVIL, url("Handyman"),
				lore("You are the repair.", "Damaging enemy nexuses may", "repair your team's nexus."),
				woodSword(), enchanted(Material.WOOD_PICKAXE, null, Enchantment.DIG_SPEED, 1), woodAxe());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void repair(NexusHitEvent event)
	{
		AnniPlayer player = event.getPlayer();
		if(player != null && player.getKit() != null && player.getKit().equals(this) && player.getTeam() != null && !event.getHitNexus().Team.equals(player.getTeam()) && !player.getTeam().isTeamDead() && random.nextInt(100) < repairChance())
		{
			AnniTeam team = player.getTeam();
			team.setHealth(team.getHealth() + 1);
			Player bukkitPlayer = player.getPlayer();
			if(bukkitPlayer != null)
				bukkitPlayer.sendMessage(ChatColor.GREEN + "Your nexus was repaired by 1.");
		}
	}

	private int repairChance()
	{
		int phase = Game.getGameMap() == null ? 2 : Game.getGameMap().getCurrentPhase();
		if(phase <= 2)
			return 25;
		if(phase == 3)
			return 20;
		if(phase == 4)
			return 15;
		return 10;
	}
}
