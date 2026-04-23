package vip.megumin.xpSystem.main;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import vip.megumin.xpSystem.database.SQLiteDatabase;
import org.bukkit.configuration.ConfigurationSection;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.kits.Kit;
import vip.megumin.xpSystem.database.AsyncLogQuery;
import vip.megumin.xpSystem.database.Database;
import vip.megumin.xpSystem.utils.Acceptor;

public class XPSystem
{
	private Database database = null;
	
	public XPSystem(ConfigurationSection databaseSection)
	{
		Objects.requireNonNull(databaseSection, "databaseSection");
		database = loadDatabase(databaseSection);
		if(database != null)
		{
			try
			{
				database.updateSQL("CREATE TABLE IF NOT EXISTS tbl_player_xp (ID VARCHAR(40), XP INTEGER, UNIQUE (ID))");
				database.updateSQL("CREATE TABLE IF NOT EXISTS tbl_player_kits (ID VARCHAR(40), Kit VARCHAR(20))");
			}
			catch(Throwable t)
			{
				database = null;
			}
		}
		
	}
	
	public boolean isActive()
	{
		return database != null;
	}
	
	public void disable()
	{
		if(database != null)
			database.closeConnection();
		database = null;
	}
	
	private Database loadDatabase(ConfigurationSection section)
	{
		Objects.requireNonNull(section, "section");
		
		String type = section.getString("Type", "mysql"); // Default to mysql for backward compatibility
		String host = section.getString("Host");
		String port = section.getString("Port");
		String data = section.getString("Database");
		String username = section.getString("Username");
		String password = section.getString("Password");
		
		if ("sqlite".equalsIgnoreCase(type)) {
			// For SQLite, the host parameter is the database file path
			return Database.getDatabase(type, host, port, data, username, password);
		} else {
			// Default to MySQL for backward compatibility
			return Database.getDatabase(type, host, port, data, username, password);
		}
	}
	
	public void giveXP(final UUID playerID, final int XP)
	{
		if(XP > 0)
		{
			String query;
			if (database instanceof SQLiteDatabase) {
				// SQLite syntax for upsert
				query = "INSERT INTO tbl_player_xp (ID, XP) VALUES ('"+playerID.toString()+"', "+XP+") "
				        + "ON CONFLICT(ID) DO UPDATE SET XP=XP+excluded.XP;";
			} else {
				// MySQL syntax
				query = "INSERT INTO tbl_player_xp (ID, XP) VALUES ('"+playerID.toString()+"', "+XP+") "
				        + "ON DUPLICATE KEY UPDATE XP=XP+VALUES(XP);";
			}
			database.addNewAsyncLogQuery(new AsyncLogQuery()
			{
				@Override
				public String getQuery()
				{
					return query;
				}
			});	
		}
	}
	
	public void removeXP(final UUID playerID, final int XP)
	{
		if(XP > 0)
		{
			String query;
			if (database instanceof SQLiteDatabase) {
				query = "INSERT INTO tbl_player_xp (ID, XP) VALUES ('"+playerID.toString()+"', 0) "
				        + "ON CONFLICT(ID) DO UPDATE SET XP=MAX(XP-"+XP+",0);";
			} else {
				query = "INSERT INTO tbl_player_xp (ID, XP) VALUES ('"+playerID.toString()+"', 0) "
				        + "ON DUPLICATE KEY UPDATE XP=GREATEST(XP-"+XP+",0);";
			}
			database.addNewAsyncLogQuery(new AsyncLogQuery()
			{
				@Override
				public String getQuery()
				{
					return query;
				}
			});	
		}
	}
	
	public void getXP(UUID playerID, Acceptor<Integer> acceptor)
	{
		if(database != null)
			database.addNewAsyncQuery(new QueryXP(playerID,acceptor));
	}
	
	public void loadKits(AnniPlayer player, Acceptor<AnniPlayer> postLoad)
	{
		if(database != null)
			database.addNewAsyncQuery(new LoadKits(player,postLoad));
	}
	
	@SuppressWarnings("unchecked")
	public void removeKit(final UUID id, final Kit kit)
	{
		AnniPlayer player = AnniPlayer.getPlayer(id);
		if(player != null)
		{
			Object obj = player.getData("Kits");
			if(obj != null && obj instanceof List)
			{
				List<String> str = (List<String>)obj;
				str.remove(kit.getName().toLowerCase());
			}
		}
		database.addNewAsyncLogQuery(new AsyncLogQuery(){
			@Override
			public String getQuery()
			{

				return "DELETE FROM tbl_player_kits WHERE ID='"+id.toString()+"' AND Kit='"+escapeSql(kit.getName())+"';";
			}});
	}
	
	@SuppressWarnings("unchecked")
	public void addKit(final UUID id, final Kit kit)
	{
		AnniPlayer player = AnniPlayer.getPlayer(id);
		if(player != null)
		{
			Object obj = player.getData("Kits");
			if(obj != null && obj instanceof List)
			{
				((List<String>)obj).add(kit.getName().toLowerCase());
			}
		}
		
		database.addNewAsyncLogQuery(new AsyncLogQuery(){
			@Override
			public String getQuery()
			{
				return "INSERT INTO tbl_player_kits (ID,Kit) VALUES ('"+id.toString()+"','"+escapeSql(kit.getName())+"');";
			}});
	}

	private static String escapeSql(String value)
	{
		return value == null ? "" : value.replace("'", "''");
	}
}
