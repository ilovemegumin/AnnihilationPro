package vip.megumin.anniPro.world;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public final class WorldBackupRestorer
{
    private WorldBackupRestorer()
    {
    }

    public static void restoreWorlds(Plugin plugin)
    {
        File pluginDirectory = plugin.getDataFolder().getParentFile();
        if (pluginDirectory == null)
            return;

        File annihilationDirectory = new File(pluginDirectory, "Annihilation");
        File worldDirectory = new File(annihilationDirectory, "Worlds");
        File backupDirectory = new File(annihilationDirectory, "WorldBackups");

        if (!worldDirectory.isDirectory())
            return;

        if (!backupDirectory.exists())
            backupDirectory.mkdirs();

        File[] worldFiles = worldDirectory.listFiles();
        if (worldFiles == null)
            return;

        for (File worldFile : worldFiles)
        {
            if (!worldFile.isDirectory())
                continue;

            File backup = new File(backupDirectory, worldFile.getName());
            try
            {
                if (backup.exists())
                {
                    deleteRecursively(worldFile.toPath());
                    copyRecursively(backup.toPath(), worldFile.toPath());
                    Bukkit.getLogger().info("[Annihilation] Restored world \"" + backup.getName() + "\" from backup.");
                }
                else
                {
                    copyRecursively(worldFile.toPath(), backup.toPath());
                    Bukkit.getLogger().info("[Annihilation] Backed up world \"" + worldFile.getName() + "\".");
                }
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Failed to synchronize world backup for " + worldFile.getName(), e);
            }
        }
    }

    private static void copyRecursively(Path source, Path destination) throws IOException
    {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
                Path target = destination.resolve(source.relativize(dir));
                Files.createDirectories(target);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Path target = destination.resolve(source.relativize(file));
                Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void deleteRecursively(Path root) throws IOException
    {
        if (!Files.exists(root))
            return;

        Files.walkFileTree(root, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
