package vip.megumin.anniPro.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vip.megumin.anniPro.itemMenus.ItemMenu;
import vip.megumin.anniPro.itemMenus.ItemMenu.Size;
import vip.megumin.anniPro.kits.KitUtils;
import vip.megumin.kits.Builder;
 
public class ShopMenu
{
	private static final ItemMenu weapon;
	private static final ItemMenu brewing;
	static
	{
		weapon = new ItemMenu("Weapon Shop",Size.THREE_LINE);
		brewing = new ItemMenu("Brewing Shop",Size.THREE_LINE);
		buildBrewingShop(brewing);
		buildWeaponShop(weapon);
	}


	private static void buildWeaponShop(ItemMenu menu)
	{
		for(int x = 0; x < 27; x++)
		{
			ItemStack icon = null;
			int cost = 0;
			boolean cat = true;
			switch(x)
			{
				case 0:
					icon = new ItemStack(Material.IRON_HELMET,1);
					cost = 3;
					break;
				case 1:
					icon = new ItemStack(Material.IRON_CHESTPLATE,1);
					cost = 5;
					break;
				case 2:
					icon = new ItemStack(Material.IRON_LEGGINGS,1);
					cost = 5;
					break;
				case 3:
					icon = new ItemStack(Material.IRON_BOOTS,1);
					cost = 3;
					break;
				case 4:
					icon = new ItemStack(Material.IRON_SWORD,1);
					cost = 1;
					break;
				case 5:
					icon = new ItemStack(Material.BOW,1);
					cost = 1;
					break;
				case 6:
					icon = new ItemStack(Material.ARROW,16);
					cost = 1;
					break;
				case 9:
					icon = new ItemStack(Material.CAKE,1);
					cost = 1;
					break;
				case 10:
					icon = new ItemStack(Material.COOKED_BEEF,10);
					cost = 5;
					break;
				case 11:
					icon = new ItemStack(Material.WEB,1);
					cost = 1;
					break;
				case 12:
					icon = new ItemStack(Material.EXP_BOTTLE,1);
					cost = 2;
					break;
				case 13:
					icon = new ItemStack(Material.ENDER_PEARL,1);
					cost = 35;
					break;
				case 14:
					icon = new ItemStack(Material.MILK_BUCKET,1);
					cost = 5;
					break;
				case 15:
					icon = new ItemStack(Material.IRON_DOOR,1);
					cost = 10;
					break;
				case 16:
					icon = new ItemStack(Material.SPONGE,1);
					cost = 5;
					break;
				case 17:
					icon = Builder.delayingBlock();
					cost = 20;
					break;
                default:
                    cat = false;
                    break;

            }
			if(cat)
				menu.setItem(x, new ShopMenuItem(icon, icon, cost));
		}
	}
	
	private static void buildBrewingShop(ItemMenu menu)
	{
		for(int x = 0; x < 27; x++)
		{
			int cost = 0;
			ItemStack icon = null;
			boolean cat = true;
			switch(x)
			{
                case 0:
					icon = new ItemStack(Material.BREWING_STAND_ITEM,1);
					cost = 10;
					break;
				case 1:
					icon = new ItemStack(Material.GLASS_BOTTLE,3);
					cost = 1;
					break;
				case 2:
					icon = new ItemStack(Material.NETHER_STALK,1);
					cost = 5;
					break;
				case 9:
					icon = new ItemStack(Material.REDSTONE,1);
					cost = 3;
					break;
				case 11:
					icon = new ItemStack(Material.FERMENTED_SPIDER_EYE,1);
					cost = 3;
					break;
				case 18:
					icon = new ItemStack(Material.MAGMA_CREAM,1);
					cost = 2;
					break;
				case 19:
					icon = new ItemStack(Material.SUGAR,1);
					cost = 2;
					break;
				case 20:
					icon = new ItemStack(Material.SPECKLED_MELON,1);
					cost = 2;
					break;
				case 21:
					icon = new ItemStack(Material.GHAST_TEAR,1);
					cost = 15;
					break;
				case 22:
					icon = new ItemStack(Material.GOLDEN_CARROT,1);
					cost = 2;
					break;
				case 23:
					icon = new ItemStack(Material.SPIDER_EYE,1);
					cost = 2;
					break;

                default:
                    cat = false;
                    break;

            }
			if(cat)
				menu.setItem(x, new ShopMenuItem(icon, icon, cost){});
		}
	}
	
	public static void addGunPowder()
	{
		ItemStack s = new ItemStack(Material.BLAZE_POWDER);
		brewing.setItem(24, new ShopMenuItem(s,s,15));
	}
	
	public static void openWeaponShop(Player p)
	{
		weapon.open(p);
	}
	
	public static void openBrewingShop(Player p)
	{
		brewing.open(p);
	}
}


