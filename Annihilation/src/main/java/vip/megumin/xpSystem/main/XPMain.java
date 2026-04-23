package vip.megumin.xpSystem.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.Kit;
import vip.megumin.anniPro.main.AnniCommand;
import vip.megumin.anniPro.main.AnnihilationMain;
import vip.megumin.anniPro.utils.Perm;
import vip.megumin.anniPro.voting.ConfigManager;
import vip.megumin.kits.AnniClassCatalog;
import vip.megumin.xpSystem.shop.Shop;

public final class XPMain implements Listener
{
    private static List<Perm> perms = new ArrayList<Perm>();

    private final JavaPlugin plugin;
    private XPSystem xpSystem;
    private YamlConfiguration config;
    private File configFile;

    public XPMain(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void onEnable()
    {
        configFile = new File(AnnihilationMain.getInstance().getDataFolder(), "AnnihilationXPConfig.yml");
        Bukkit.getLogger().info("[AnnihilationXPSystem] Loading XP system...");
        Bukkit.getPluginManager().registerEvents(this, plugin);

        checkFile(configFile);
        config = YamlConfiguration.loadConfiguration(configFile);

        int x = 0;
        x += ConfigManager.setDefaultIfNotSet(config, "Nexus-Hit-XP", 1);
        x += ConfigManager.setDefaultIfNotSet(config, "Player-Kill-XP", 3);
        x += ConfigManager.setDefaultIfNotSet(config, "Winning-Team-XP", 100);
        x += ConfigManager.setDefaultIfNotSet(config, "Second-Place-Team-XP", 75);
        x += ConfigManager.setDefaultIfNotSet(config, "Third-Place-Team-XP", 50);
        x += ConfigManager.setDefaultIfNotSet(config, "Last-Place-Team-XP", 25);
        x += ConfigManager.setDefaultIfNotSet(config, "Gave-XP-Message", "&a+%# Annihilation XP");
        x += ConfigManager.setDefaultIfNotSet(config, "MyXP-Command-Message", "&dYou have &a%#&d Annihilation XP.");
        if (!config.isConfigurationSection("XP-Multipliers"))
        {
            ConfigurationSection multipliers = config.createSection("XP-Multipliers");
            multipliers.set("Multiplier-1.Permission", "Anni.XP.2");
            multipliers.set("Multiplier-1.Multiplier", 2);
            x++;
        }

        ConfigurationSection data = config.getConfigurationSection("Database");
        if (data == null)
            data = config.createSection("Database");

        x += ConfigManager.setDefaultIfNotSet(data, "Type", "mysql"); // Default to mysql for backward compatibility
        x += ConfigManager.setDefaultIfNotSet(data, "Host", "Test");
        x += ConfigManager.setDefaultIfNotSet(data, "Port", "Test");
        x += ConfigManager.setDefaultIfNotSet(data, "Database", "Test");
        x += ConfigManager.setDefaultIfNotSet(data, "Username", "Test");
        x += ConfigManager.setDefaultIfNotSet(data, "Password", "Test");

        ConfigurationSection shopSec = config.getConfigurationSection("Kit-Shop");
        if (shopSec == null)
        {
            shopSec = config.createSection("Kit-Shop");
            shopSec.createSection("Kits");
        }

        x += ConfigManager.setDefaultIfNotSet(shopSec, "On", false);
        x += ConfigManager.setDefaultIfNotSet(shopSec, "Already-Purchased-Kit", "&aPURCHASED");
        x += ConfigManager.setDefaultIfNotSet(shopSec, "Not-Yet-Purchased-Kit", "&cLOCKED. PURCHASE FOR &6%# &cXP");
        x += ConfigManager.setDefaultIfNotSet(shopSec, "Confirm-Purchase-Kit", "&aPUCHASE BEGUN. CONFIRM FOR &6%# &AXP");
        x += ConfigManager.setDefaultIfNotSet(shopSec, "Confirmation-Expired", "&cThe confirmation time has expired. Please try again.");
        x += ConfigManager.setDefaultIfNotSet(shopSec, "Not-Enough-XP", "&cYou do not have enough XP to purchase this kit.");
        x += ConfigManager.setDefaultIfNotSet(shopSec, "Kit-Purchased", "&aKit %w purchased!");
        x += ConfigManager.setDefaultIfNotSet(shopSec, "No-Kits-To-Purchase", "&cNo kits left to purchase!");
        ConfigurationSection kitPrices = shopSec.getConfigurationSection("Kits");
        if(kitPrices == null)
            kitPrices = shopSec.createSection("Kits");
        for(Kit kit : Kit.getKits())
            x += ConfigManager.setDefaultIfNotSet(kitPrices, kit.getName(), AnniClassCatalog.getDefaultPrice(kit.getName()));

        if (x > 0)
            saveConfig();

        xpSystem = new XPSystem(config.getConfigurationSection("Database"));
        if (!xpSystem.isActive())
        {
            Bukkit.getLogger().warning("[AnnihilationXPSystem] Could not connect to the database. XP features will stay disabled.");
            return;
        }

        Bukkit.getLogger().info("[AnnihilationXPSystem] CONNECTED to the database");

        if (config.getBoolean("Kit-Shop.On"))
        {
            Bukkit.getLogger().info("[AnnihilationXPSystem] The shop is ENABLED");
            plugin.getCommand("Shop").setExecutor(new Shop(xpSystem, config.getConfigurationSection("Kit-Shop")));
        }
        else
        {
            Bukkit.getLogger().info("[AnnihilationXPSystem] The shop is DISABLED");
        }

        loadMultipliers(config.getConfigurationSection("XP-Multipliers"));
        loadXPVars(config);
        AnniCommand.registerArgument(new XPArgument(xpSystem));
        AnniCommand.registerArgument(new KitArgument(xpSystem));
        for (AnniPlayer player : AnniPlayer.getPlayers())
            xpSystem.loadKits(player, null);
    }

    @EventHandler
    public void loadKits(PlayerJoinEvent e)
    {
        if (xpSystem == null || !xpSystem.isActive())
            return;

        AnniPlayer player = AnniPlayer.getPlayer(e.getPlayer().getUniqueId());
        if (player != null)
            xpSystem.loadKits(player, null);
    }

    private void checkFile(File file)
    {
        if (!file.exists())
        {
            try
            {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists())
                    parent.mkdirs();
                file.createNewFile();
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Unable to create XP config: " + file.getAbsolutePath(), e);
            }
        }
    }

    public void saveConfig()
    {
        try
        {
            config.save(configFile);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to save XP config: " + configFile.getAbsolutePath(), e);
        }
    }

    public void loadXPVars(ConfigurationSection section)
    {
        Objects.requireNonNull(section, "section");

        int nexusHitXP = section.getInt("Nexus-Hit-XP");
        int killXP = section.getInt("Player-Kill-XP");
        String gaveXPMessage = section.getString("Gave-XP-Message");
        String myXPMessage = section.getString("MyXP-Command-Message");
        int[] teamXPs = new int[4];
        teamXPs[0] = section.getInt("Winning-Team-XP");
        teamXPs[1] = section.getInt("Second-Place-Team-XP");
        teamXPs[2] = section.getInt("Third-Place-Team-XP");
        teamXPs[3] = section.getInt("Last-Place-Team-XP");

        XPListeners listeners = new XPListeners(xpSystem, gaveXPMessage, killXP, nexusHitXP, teamXPs);
        MyXPCommand command = new MyXPCommand(xpSystem, myXPMessage);

        Bukkit.getPluginManager().registerEvents(listeners, plugin);
        plugin.getCommand("MyXP").setExecutor(command);
    }

    public void loadMultipliers(ConfigurationSection multipliers)
    {
        perms = new ArrayList<Perm>();
        if (multipliers == null)
            return;

        for (String key : multipliers.getKeys(false))
        {
            ConfigurationSection multSec = multipliers.getConfigurationSection(key);
            if (multSec == null)
                continue;

            String perm = multSec.getString("Permission");
            int multiplier = multSec.getInt("Multiplier");
            if (perm != null && !perm.isEmpty() && multiplier > 0)
            {
                Permission permission = Bukkit.getPluginManager().getPermission(perm);
                if (permission == null)
                {
                    permission = new Permission(perm);
                    permission.setDefault(PermissionDefault.FALSE);
                    Bukkit.getPluginManager().addPermission(permission);
                }
                permission.recalculatePermissibles();
                perms.add(new Perm(perm, multiplier));
            }
        }
        Collections.sort(perms);
    }

    public void onDisable()
    {
        if (xpSystem != null)
            xpSystem.disable();
    }

    public static String formatString(String string, int amount)
    {
        return ChatColor.translateAlternateColorCodes('&', string.replace("%#", "" + amount));
    }

    public static int checkMultipliers(Player player, int initialXP)
    {
        if (player != null && !perms.isEmpty())
        {
            for (Perm perm : perms)
            {
                if (player.hasPermission(perm.perm))
                {
                    initialXP = (int) Math.ceil(((double) initialXP) * perm.multiplier);
                    break;
                }
            }
        }
        return initialXP;
    }
}
