package com.moddamage.external.tabAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.mcsg.double0negative.tabapi.TabAPI;

import com.moddamage.LogUtil;
import com.moddamage.ModDamage;
import com.moddamage.StringMatcher;
import com.moddamage.backend.BailException;
import com.moddamage.backend.ScriptLine;
import com.moddamage.eventinfo.EventData;
import com.moddamage.eventinfo.EventInfo;
import com.moddamage.parsing.DataProvider;
import com.moddamage.parsing.IDataProvider;
import com.moddamage.routines.Routine;

public class SetTabString extends Routine 
{
	private final IDataProvider<Player> playerDP;
	IDataProvider<Integer> xDP, yDP, pingDP;
	private final IDataProvider<String> string;
	
	private SetTabString(ScriptLine scriptLine, IDataProvider<Player> playerDP, IDataProvider<Integer> xDP, IDataProvider<Integer> yDP, IDataProvider<Integer> pingDP, IDataProvider<String> string)
	{
		super(scriptLine);
		this.playerDP = playerDP;
		this.xDP = xDP;
		this.yDP = yDP;
		this.pingDP = pingDP;
		this.string = string;
	}
	
	@Override
	public void run(EventData data) throws BailException
	{
		Player player = playerDP.get(data);   if (player == null) return;
		
		Integer x = xDP.get(data);   if (x == null) return;
		Integer y = yDP.get(data);   if (y == null) return;
		Integer ping = null;
		if (pingDP != null) {
			ping = pingDP.get(data);   if (ping == null) return;
		}
		
		String str = string.get(data); if (str == null) return;
		
		if (ping == null)
			TabAPI.setTabString(ModDamage.getPluginConfiguration().plugin, player, x, y, str);
		else
			TabAPI.setTabString(ModDamage.getPluginConfiguration().plugin, player, x, y, str, ping);
	}
	
	public static void registerNested()
	{
		Routine.registerRoutine(Pattern.compile("([^\\.]+)\\.settabstring\\.(.+?): (.+)", Pattern.CASE_INSENSITIVE), new RoutineFactory());
	}
	
	private static Pattern seperatorPattern = Pattern.compile("\\s*[\\.,]\\s*");
	
	protected static class RoutineFactory extends Routine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher m, ScriptLine scriptLine, EventInfo info)
		{
			IDataProvider<Player> playerDP = DataProvider.parse(info, Player.class, m.group(1));
			if(playerDP == null) return null;
			

			StringMatcher sm = new StringMatcher(m.group(2));
			
			IDataProvider<Integer> xDP = DataProvider.parse(info, Integer.class, sm.spawn()); if (xDP == null) return null;
			if (!sm.matchesFront(seperatorPattern)) return null;
			IDataProvider<Integer> yDP = DataProvider.parse(info, Integer.class, sm.spawn()); if (yDP == null) return null;
			
			IDataProvider<Integer> pingDP = null;
			if (!sm.isEmpty()) {
				if (!sm.matchesFront(seperatorPattern)) return null;
				pingDP = DataProvider.parse(info, Integer.class, sm.spawn()); if (pingDP == null) return null;
				
				if (!sm.isEmpty()) return null;
			}

			IDataProvider<String> istr = DataProvider.parse(info, String.class, m.group(3));
			

			LogUtil.info("SetTabString: " + playerDP + " " + xDP + ", " + yDP + (pingDP == null? "" : (", " + pingDP)) + ": " + istr);
			
			return new RoutineBuilder(new SetTabString(scriptLine, playerDP, xDP, yDP, pingDP, istr));
		}
	}
}