package com.moddamage.external.tabAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.mcsg.double0negative.tabapi.TabAPI;

import com.moddamage.LogUtil;
import com.moddamage.backend.BailException;
import com.moddamage.backend.ScriptLine;
import com.moddamage.eventinfo.EventData;
import com.moddamage.eventinfo.EventInfo;
import com.moddamage.parsing.DataProvider;
import com.moddamage.parsing.IDataProvider;
import com.moddamage.routines.Routine;
import com.moddamage.routines.nested.NestedRoutine;

public class UpdateTab extends NestedRoutine 
{
	private final IDataProvider<Player> playerDP;
	
	private UpdateTab(ScriptLine scriptLine, IDataProvider<Player> playerDP)
	{
		super(scriptLine);
		this.playerDP = playerDP;
	}
	
	@Override
	public void run(EventData data) throws BailException
	{
		Player player = playerDP.get(data);   if (player == null) return;
		
		TabAPI.updatePlayer(player);
	}
	
	public static void registerRoutine()
	{
		Routine.registerRoutine(Pattern.compile("([^\\.]+)\\.updatetab", Pattern.CASE_INSENSITIVE), new RoutineFactory());
	}
	
	protected static class RoutineFactory extends Routine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher m, ScriptLine scriptLine, EventInfo info)
		{
			IDataProvider<Player> playerDP = DataProvider.parse(scriptLine, info, Player.class, m.group(1));
			if(playerDP == null) return null;
			
			LogUtil.info("UpdateTab: " + playerDP);
			
			return new RoutineBuilder(new UpdateTab(scriptLine, playerDP));
		}
	}
}