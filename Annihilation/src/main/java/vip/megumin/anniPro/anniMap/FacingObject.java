package vip.megumin.anniPro.anniMap;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import vip.megumin.anniPro.utils.Loc;

public final class FacingObject
{
	private final BlockFace facing;
	private final Loc location;
	public FacingObject(BlockFace facingDirection, Loc location)
	{
		this.facing = facingDirection;
		this.location = location;
	}
	
	public BlockFace getFacingDirection()
	{
		return facing;
	}
	
	public Loc getLocation()
	{
		return location;
	}
	
	public void saveToConfig(ConfigurationSection configSection)
	{
		if(configSection != null)
		{
			configSection.set("FacingDirection", facing.name());
			location.saveToConfig(configSection.createSection("Location"));
		}
	}
	
	public static FacingObject loadFromConfig(ConfigurationSection configSection)
	{
		if(configSection != null)
		{
			Loc loc = new Loc(configSection.getConfigurationSection("Location"));
			BlockFace facing = BlockFace.valueOf(configSection.getString("FacingDirection"));
			return new FacingObject(facing,loc);
		}
		return null;
	}
}
