package vip.megumin.anniPro.announcementBar;

import vip.megumin.anniPro.main.AnnihilationMain;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AnnounceBar
{
    private static AnnounceBar instance;
    public static AnnounceBar getInstance()
    {
        if(instance == null)
        {
            instance = new AnnounceBar();
        }
        return instance;
    }

    private final Bar bar;

    private Announcement announcement;

    private Integer timer;
    private long timeLeft;
    private long lastUpdate;

    private AnnounceBar()
    {
        this.bar = new BossBar();
    }

    public TempData getData()
    {
        TempData d = new TempData();
        d.announcement = announcement;
        d.timeLeft = timeLeft;
        return d;
    }
    public void countDown(TempData d)
    {
        if(d == null || d.announcement == null)
            return;
        this.announcement = d.announcement;
        this.timeLeft = d.timeLeft;
        this.lastUpdate = System.currentTimeMillis();
        scheduleUpdater();
    }

    public void countDown(Announcement announcement)
    {
        this.announcement = announcement;
        this.timeLeft = announcement.getTime()* 1000L;
        this.lastUpdate = System.currentTimeMillis();
        String mess = ChatColor.translateAlternateColorCodes('&', announcement.getMessage()).replace("{#}", format(timeLeft));
        for(Player pl : Bukkit.getOnlinePlayers())
            bar.sendToPlayer(pl, mess, 100);
        scheduleUpdater();
    }

    private void scheduleUpdater()
    {
        cancelUpdater();
        timer = Bukkit.getScheduler().runTaskTimer(AnnihilationMain.getInstance(), new Runnable()
        {
            @Override
            public void run()
            {
                if(announcement == null)
                {
                    cancelUpdater();
                    return;
                }
                String m = ChatColor.translateAlternateColorCodes('&',announcement.getMessage());              // message.replace("{#}", format(timeLeft));
                if (announcement.isPermanent())
                {
                    for (Player player : Bukkit.getOnlinePlayers())
                        bar.sendToPlayer(player,m,100);
                    return;
                }
                timeLeft -= (System.currentTimeMillis() - lastUpdate);
                lastUpdate = System.currentTimeMillis();
                if (timeLeft <= 100) //if its less than a tenth of a second, then we can pretty msuch say its over
                {
                    timeLeft = 0;
                    cancelUpdater();
                    if (announcement.getCallBack() != null)
                        announcement.getCallBack().run();
                }
                else
                {
                    String mess = m.replace("{#}", format(timeLeft));
                    float percent = ((float) timeLeft) / (announcement.getTime() * 1000.0F);
                    for(Player player : Bukkit.getOnlinePlayers())
                        bar.sendToPlayer(player,mess,percent);

                }
            }
        }, 20L, 20L).getTaskId();
    }

    private void cancelUpdater()
    {
        if(timer != null)
        {
            Bukkit.getScheduler().cancelTask(timer);
            timer = null;
        }
    }

    private static String format(long miliseconds)
    {
        return DurationFormatUtils.formatDuration(miliseconds, "mm:ss");
    }
}
