package vip.megumin.anniPro.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;

import vip.megumin.anniPro.main.AnnihilationMain;

public final class ResourceImages
{
	private ResourceImages()
	{
	}

	public static BufferedImage read(String fileName)
	{
		BufferedImage image = readFromPath("assets/" + fileName);
		if(image != null)
			return image;
		return readFromPath("Images/" + fileName);
	}

	private static BufferedImage readFromPath(String path)
	{
		InputStream stream = AnnihilationMain.getInstance().getResource(path);
		if(stream == null)
			return null;

		try
		{
			BufferedImage image = ImageIO.read(stream);
			if(image == null)
				Bukkit.getLogger().warning("[Annihilation] Resource is not a readable image: " + path);
			return image;
		}
		catch (IOException e)
		{
			Bukkit.getLogger().warning("[Annihilation] Could not load image resource " + path + ": " + e.getMessage());
			return null;
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (IOException ignored)
			{
			}
		}
	}
}
