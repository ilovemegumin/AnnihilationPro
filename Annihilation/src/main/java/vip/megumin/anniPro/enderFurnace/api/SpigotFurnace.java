package vip.megumin.anniPro.enderFurnace.api;

import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.TileEntityFurnace;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftFurnace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

class SpigotFurnace extends TileEntityFurnace implements IFurnace
{
    private final EntityPlayer owningPlayer;

    SpigotFurnace(Player player)
    {
        this.owningPlayer = ((CraftPlayer) player).getHandle();
        this.world = owningPlayer.world;
        super.a("Ender Furnace");
    }

    @Override
    public boolean a(EntityHuman entityhuman)
    {
        return true;
    }

    @Override
    public int g()
    {
        return 0;
    }

    @Override
    public InventoryHolder getOwner()
    {
        org.bukkit.block.Furnace furnace = new CraftFurnace(this.world.getWorld().getBlockAt(0, 0, 0));
        try
        {
            ReflectionUtil.setValue(furnace, "furnace", this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return furnace;
    }

    @Override
    public void open()
    {
        owningPlayer.openContainer(this);
    }

    @Override
    public void tick()
    {
        try
        {
            c();
        }
        catch (Throwable t)
        {
        }
    }

    @Override
    public FurnaceData getFurnaceData()
    {
        return new SpigotFurnaceData(this);
    }

    @Override
    public void load(final FurnaceData data)
    {
        ItemStack[] items = data.getItems();
        for (int x = 0; x < 3; x++)
            setItem(x, CraftItemStack.asNMSCopy(items[x]));
        b(0, data.getBurnTime());
        b(1, data.getTicksForCurrentFuel());
        b(2, data.getCookTime());
    }
}
