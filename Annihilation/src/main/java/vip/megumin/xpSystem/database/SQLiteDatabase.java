package vip.megumin.xpSystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase extends Database
{
	private final String databasePath;

	public SQLiteDatabase(String databasePath)
	{
		this.databasePath = databasePath;
	}

	@Override
	public Connection openConnection() throws SQLException, ClassNotFoundException
	{
		if (checkConnection())
		{
			return connection;
		}
		
		try
		{
			Class.forName("org.sqlite.JDBC");
		}
		catch (ClassNotFoundException e)
		{
			// Try alternative class name
			Class.forName("sqlite.JDBC");
		}
		
		// Ensure the directory exists
		java.io.File dbFile = new java.io.File(databasePath);
		java.io.File parentDir = dbFile.getParentFile();
		if (parentDir != null && !parentDir.exists())
		{
			parentDir.mkdirs();
		}
		
		connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
		return connection;
	}
}