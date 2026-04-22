package vip.megumin.xpSystem.database;

import java.sql.ResultSet;

public interface AsyncQuery extends Runnable
{
	public boolean isCallback();
	public String getQuerey();
	public void setResult(ResultSet set);
	
}
