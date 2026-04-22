package vip.megumin.anniPro.enderFurnace.api;

import vip.megumin.anniPro.anniGame.AnniPlayer;

public final class EnderFurnace
{
    private static final FurnaceCreator CREATOR = new SpigotFurnaceCreator();

    private EnderFurnace()
    {}


    public static FurnaceCreator getCreator()
    {
        return CREATOR;
    }

    public static FurnaceData getFurnaceData(AnniPlayer player)
    {
        Object obj = player.getData("ED");
        if(obj == null)
            return null;
        return (FurnaceData)obj;
    }
}
