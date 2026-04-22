package vip.megumin.anniPro.main;

import org.bukkit.command.CommandSender;

import vip.megumin.anniPro.itemMenus.MenuItem;

public interface AnniArgument
{
	String getHelp();
	boolean useByPlayerOnly();
	String getArgumentName();
	void executeCommand(CommandSender sender, String label, String[] args);
	String getPermission();
	MenuItem getMenuItem();
}
