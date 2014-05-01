package com.ModDamage.External.TabAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.mcsg.double0negative.tabapi.TabAPI;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine;
import com.ModDamage.Routines.Nested.NestedRoutine;

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
			IDataProvider<Player> playerDP = DataProvider.parse(info, Player.class, m.group(1));
			if(playerDP == null) return null;
			
			ModDamageLogger.info("UpdateTab: " + playerDP);
			
			return new RoutineBuilder(new UpdateTab(scriptLine, playerDP));
		}
	}
}