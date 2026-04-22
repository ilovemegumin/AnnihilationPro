package vip.megumin.anniPro.enderFurnace.api;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public class SpigotFurnaceCreator implements FurnaceCreator
{
    @Override
    public IFurnace createFurnace(final AnniPlayer player)
    {
        IFurnace furnace = new SpigotFurnace(player.getPlayer());
        FurnaceData data = EnderFurnace.getFurnaceData(player);
        if (data != null)
            furnace.load(data);
        return furnace;
    }
}
