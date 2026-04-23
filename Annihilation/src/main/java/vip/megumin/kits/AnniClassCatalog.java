package vip.megumin.kits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import vip.megumin.anniPro.kits.Kit;

public final class AnniClassCatalog
{
	private static final int DEFAULT_PRICE = 15000;
	private static final Map<String,Integer> PRICES = new HashMap<String,Integer>();

	static
	{
		PRICES.put("Civilian", 0);
	}

	private AnniClassCatalog()
	{
	}

	public static int getDefaultPrice(String kitName)
	{
		Integer price = PRICES.get(ChatColor.stripColor(kitName));
		return price == null ? DEFAULT_PRICE : price.intValue();
	}

	public static List<Kit> createMissingKits()
	{
		List<Kit> kits = new ArrayList<Kit>();
		kits.add(new AnniClassKit("Alchemist", Material.BREWING_STAND_ITEM, url("Alchemist"),
				AnniClassKit.lore("Enhanced brewing specialist.", "Private brewing stand brews faster.", "Tome supplies potion ingredients on cooldown."),
				woodSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.BREWING_STAND_ITEM, ChatColor.AQUA + "Alchemist's Brewing Stand"), AnniClassKit.named(Material.BOOK, ChatColor.AQUA + "Alchemist's Tome")));
		kits.add(new AnniClassKit("Bard", Material.JUKEBOX, url("Bard"),
				AnniClassKit.lore("Area support with a Buffbox.", "Songs buff teammates or debuff enemies nearby.", "Moving the Buffbox has a cooldown."),
				woodSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.JUKEBOX, ChatColor.AQUA + "Buffbox")));
		kits.add(new AnniClassKit("Bloodmage", Material.FERMENTED_SPIDER_EYE, url("Bloodmage"),
				AnniClassKit.lore("Damage-over-time caster.", "Melee hits can poison enemies.", "Corrupt and Terraform pressure nearby enemies."),
				stoneSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.FERMENTED_SPIDER_EYE, ChatColor.AQUA + "Corrupt"), AnniClassKit.named(Material.NETHERRACK, ChatColor.AQUA + "Bloodcursed Terraform")));
		kits.add(new AnniClassKit("Builder", Material.BRICK, url("Builder"),
				AnniClassKit.lore("Defense builder.", "Resource Drop supplies blocks.", "Replication Cache produces stored building blocks."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), AnniClassKit.named(Material.BOOK, ChatColor.AQUA + "Resource Drop"), AnniClassKit.named(Material.CHEST, ChatColor.AQUA + "Replication Cache")));
		kits.add(new AnniClassKit("Dasher", Material.ENDER_PEARL, url("Dasher"),
				AnniClassKit.lore("Short-range mobility class.", "Sneak to Blink between 5 and 20 blocks.", "Cooldown scales with distance."),
				woodSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.ENDER_PEARL, ChatColor.AQUA + "Blink")));
		kits.add(new AnniClassKit("Engineer", Material.TNT, url("Engineer"),
				AnniClassKit.lore("Demolition and utility class.", "Bunker Buster breaks enemy defenses.", "Evertool changes into the needed tool."),
				stoneSword(), AnniClassKit.named(Material.TNT, ChatColor.AQUA + "Bunker Buster"), AnniClassKit.named(Material.BLAZE_ROD, ChatColor.AQUA + "Evertool")));
		kits.add(new AnniClassKit("Farmer", Material.SEEDS, url("Farmer"),
				AnniClassKit.lore("Food and crop specialist.", "Harvesting crops can produce useful loot.", "Feast feeds allies; Famine starves enemies."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), AnniClassKit.named(Material.GOLDEN_CARROT, ChatColor.AQUA + "Feast"), AnniClassKit.named(Material.DEAD_BUSH, ChatColor.AQUA + "Famine"), AnniClassKit.item(Material.STONE_HOE), AnniClassKit.item(Material.INK_SACK, 15), AnniClassKit.item(Material.CHEST)));
		kits.add(new AnniClassKit("Handyman", Material.ANVIL, url("Handyman"),
				AnniClassKit.lore("Nexus repair specialist.", "Damaging enemy nexuses can repair your nexus.", "Starts with an Efficiency I wooden pickaxe."),
				woodSword(), AnniClassKit.enchanted(Material.WOOD_PICKAXE, null, Enchantment.DIG_SPEED, 1), woodAxe()));
		kits.add(new AnniClassKit("Healer", Material.REDSTONE, url("Healer"),
				AnniClassKit.lore("Team sustain class.", "Sees teammate health.", "Blood Bag heals nearby low-health allies."),
				woodSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.REDSTONE, ChatColor.AQUA + "Blood Bag")));
		kits.add(new AnniClassKit("Hunter", Material.LEASH, url("Hunter"),
				AnniClassKit.lore("Trap specialist.", "Trap Snare places enemy-triggered traps.", "Flurry is the active combat tool."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), AnniClassKit.named(Material.LEASH, ChatColor.AQUA + "Trap Snare"), AnniClassKit.named(Material.SNOW_BALL, ChatColor.AQUA + "Flurry")));
		kits.add(new AnniClassKit("Immobilizer", Material.SLIME_BALL, url("Immobilizer"),
				AnniClassKit.lore("Single-target control class.", "Right-click immobilizes a nearby enemy.", "Left-click slows enemies in an area."),
				woodSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.SLIME_BALL, ChatColor.AQUA + "Immobilizer")));
		kits.add(new AnniClassKit("Mercenary", Material.SKULL_ITEM, url("Mercenary"),
				AnniClassKit.lore("Marks priority targets.", "Marked enemies take extra pressure from your team.", "Starts with a chain helmet."),
				woodSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.SKULL_ITEM, ChatColor.AQUA + "Mark")));
		kits.add(new AnniClassKit("Neptune", Material.WATER_BUCKET, url("Neptune"),
				AnniClassKit.lore("Water combat specialist.", "Tidebringer and Ground Freeze control water fights.", "Designed for rivers and coastal maps."),
				stoneSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.WATER_BUCKET, ChatColor.AQUA + "Tidebringer"), AnniClassKit.named(Material.PACKED_ICE, ChatColor.AQUA + "Ground Freeze"), AnniClassKit.item(Material.WATER_LILY)));
		kits.add(new AnniClassKit("Ninja", Material.FIREWORK_CHARGE, url("Ninja"),
				AnniClassKit.lore("Stealth mobility fighter.", "Smoke Bomb, Shuriken and Ascension enable escapes.", "Jump Boost can be toggled."),
				AnniClassKit.item(Material.GOLD_SWORD), woodPick(), woodAxe(), AnniClassKit.named(Material.SULPHUR, ChatColor.AQUA + "Smoke Bomb"), AnniClassKit.named(Material.NETHER_STAR, ChatColor.AQUA + "Shuriken"), AnniClassKit.named(Material.FIREWORK_CHARGE, ChatColor.AQUA + "Masterful Ascension")));
		kits.add(new AnniClassKit("Rift Walker", Material.BLAZE_ROD, url("Rift_Walker"),
				AnniClassKit.lore("Team teleport class.", "Opens a rift to teammates, enemy territory, or base.", "Sneaking teammates can travel with the rift."),
				woodSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.BLAZE_ROD, ChatColor.AQUA + "Rift Rod")));
		kits.add(new AnniClassKit("Robin Hood", Material.SADDLE, url("Robin_Hood"),
				AnniClassKit.lore("Mounted archer-style mobility.", "Steed summons a strong horse.", "Aerial Agility prevents fall damage windows."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), AnniClassKit.named(Material.SADDLE, ChatColor.AQUA + "Steed"), AnniClassKit.named(Material.PAPER, ChatColor.AQUA + "Aerial Agility")));
		kits.add(new AnniClassKit("Sniper", Material.ARROW, url("Sniper"),
				AnniClassKit.lore("Precision bow class.", "Compound Bow can switch firing modes.", "Sniper mode fires fast, flat arrows."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), AnniClassKit.named(Material.BOW, ChatColor.AQUA + "Compound Bow"), AnniClassKit.item(Material.ARROW, 32)));
		kits.add(new AnniClassKit("Spider", Material.WEB, url("Spider"),
				AnniClassKit.lore("Wall and web control.", "Wall Climb creates temporary vines.", "Cobwebs slow enemies and help escapes."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), AnniClassKit.named(Material.VINE, ChatColor.AQUA + "Wall Climb"), AnniClassKit.item(Material.WEB, 5)));
		kits.add(new AnniClassKit("Spy", Material.POTION, url("Spy"),
				AnniClassKit.lore("Vanish class.", "Standing still hides armor, particles and body.", "Flee creates a clone and grants temporary invisibility."),
				AnniClassKit.item(Material.GOLD_SWORD), woodPick(), woodAxe(), AnniClassKit.named(Material.POTION, ChatColor.AQUA + "Flee")));
		kits.add(new AnniClassKit("Tank", Material.IRON_CHESTPLATE, url("Tank"),
				AnniClassKit.lore("Frontline damage sponge.", "Defensive Shield blocks incoming damage.", "Shield Charge blasts through enemies."),
				stoneSword(), woodPick(), woodAxe(), woodShovel(), AnniClassKit.named(Material.IRON_CHESTPLATE, ChatColor.AQUA + "Defensive Shield"), AnniClassKit.named(Material.PRISMARINE_SHARD, ChatColor.AQUA + "Shield Charge")));
		kits.add(new AnniClassKit("Tinkerer", Material.REDSTONE_BLOCK, url("Tinkerer"),
				AnniClassKit.lore("PowerPad support class.", "Pads grant Speed, Haste or Absorption.", "Books can disenchant compatible items."),
				stoneSword(), woodPick(), woodAxe(), AnniClassKit.named(Material.COAL_BLOCK, ChatColor.AQUA + "Haste PowerPad"), AnniClassKit.named(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Speed PowerPad"), AnniClassKit.item(Material.BOOK, 10)));
		kits.add(new AnniClassKit("Wizard", Material.STICK, url("Wizard"),
				AnniClassKit.lore("Spell caster.", "Spellbook selects five combat spells.", "Wand casts with a shared cooldown."),
				woodSword(), woodPick(), woodAxe(), woodShovel(), AnniClassKit.named(Material.STICK, ChatColor.AQUA + "Wand"), AnniClassKit.named(Material.BOOK, ChatColor.AQUA + "Spellbook")));
		return kits;
	}

	private static String url(String page)
	{
		return "https://wiki.shotbow.net/" + page;
	}

	private static AnniClassKit.KitItem woodSword()
	{
		return AnniClassKit.item(Material.WOOD_SWORD);
	}

	private static AnniClassKit.KitItem stoneSword()
	{
		return AnniClassKit.item(Material.STONE_SWORD);
	}

	private static AnniClassKit.KitItem woodPick()
	{
		return AnniClassKit.item(Material.WOOD_PICKAXE);
	}

	private static AnniClassKit.KitItem woodAxe()
	{
		return AnniClassKit.item(Material.WOOD_AXE);
	}

	private static AnniClassKit.KitItem woodShovel()
	{
		return AnniClassKit.item(Material.WOOD_SPADE);
	}
}
