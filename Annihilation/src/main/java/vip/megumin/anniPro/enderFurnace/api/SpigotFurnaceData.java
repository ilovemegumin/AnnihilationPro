package vip.megumin.anniPro.enderFurnace.api;

import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.TileEntityFurnace;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

class SpigotFurnaceData extends FurnaceData
{
    SpigotFurnaceData(TileEntityFurnace furnace)
    {
        super(asBukkitCopy(furnace.getContents()), furnace.getProperty(0), furnace.getProperty(1), furnace.getProperty(2));
    }

    private static org.bukkit.inventory.ItemStack[] asBukkitCopy(ItemStack[] stacks)
    {
        org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[stacks.length];
        for (int i = 0; i < items.length; i++)
        {
            items[i] = CraftItemStack.asBukkitCopy(stacks[i]);
        }
        return items;
    }
}
