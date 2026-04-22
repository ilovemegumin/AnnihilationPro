package vip.megumin.xpSystem.main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.xpSystem.database.AsyncQuery;
import vip.megumin.xpSystem.utils.Acceptor;

class LoadKits implements AsyncQuery
{
	private final AnniPlayer player;
	private final Acceptor<AnniPlayer> postLoad;
	private List<String> kits;
	
	public LoadKits(AnniPlayer p, Acceptor<AnniPlayer> postLoad)
	{
		this.player = Objects.requireNonNull(p, "player");
		this.postLoad = postLoad;
		this.kits = new ArrayList<String>();
	}
	
	@Override
	public void run()
	{
		player.setData("Kits", kits);
		if(postLoad != null)
			postLoad.accept(player);
	}

	@Override
	public String getQuerey() 
	{	
		return "SELECT * FROM tbl_player_kits WHERE ID='"+player.getID()+"';";
	}

	@Override
	public boolean isCallback() 
	{
		return true;
	}

	@Override
	public void setResult(ResultSet result) 
	{
		kits = new ArrayList<String>();
		try
		{
			while(result.next())
			{
				kits.add(result.getString("Kit").toLowerCase());
			}
			result.close();
		}
		catch(SQLException e)
		{
			
		}
	}
}
