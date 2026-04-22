package vip.megumin.xpSystem.shop;

import vip.megumin.anniPro.kits.Kit;

public class KitWrapper
{
	public KitWrapper(Kit k, int price)
	{
		this.kit = k;
		this.price = price;
	}
	public Kit kit;
	public int price;
}
