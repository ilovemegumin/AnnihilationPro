package vip.megumin.xpSystem.main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

import vip.megumin.xpSystem.database.AsyncQuery;
import vip.megumin.xpSystem.utils.Acceptor;

class QueryXP implements AsyncQuery
{
	private final UUID playerID;
	private final Acceptor<Integer> acceptor;
	
	private int xp = 0;
	
	public QueryXP(UUID playerID, Acceptor<Integer> acceptor)
	{
		this.playerID = Objects.requireNonNull(playerID, "playerID");
		this.acceptor = Objects.requireNonNull(acceptor, "acceptor");
	}
	
	@Override
	public void run()
	{
		acceptor.accept(xp);
	}

	@Override
	public boolean isCallback()
	{
		return true;
	}

	@Override
	public String getQuerey()
	{
		return "SELECT * FROM tbl_player_xp WHERE ID='"+ playerID +"'";

	}

	@Override
	public void setResult(ResultSet set)
	{
		try
		{
			if(set.next())
				xp = set.getInt("XP");
			set.close();
		}
		catch (SQLException e)
		{	
			e.printStackTrace();
		}
	}
}
