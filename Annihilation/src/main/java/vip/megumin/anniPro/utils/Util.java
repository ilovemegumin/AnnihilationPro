package vip.megumin.anniPro.utils;

import java.io.File;
import java.io.IOException;

public class Util
{
	public static String shortenString(String string, int characters)
	{
		if(string.length() <= characters)
			return string;
		return string.substring(0, characters);
	}
	
	public static boolean tryCreateFile(File file)
	{
		if(!file.exists())
		{
			try
			{
				File parent = file.getParentFile();
				if(parent != null && !parent.exists() && !parent.mkdirs())
					return false;
				file.createNewFile();
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
}
