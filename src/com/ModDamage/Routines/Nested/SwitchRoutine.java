package com.ModDamage.Routines.Nested;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.ModDamageLogger.OutputPreset;
import com.ModDamage.Utils;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.ScriptLineHandler;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine;
import com.ModDamage.Routines.RoutineList;

public class SwitchRoutine extends NestedRoutine
{
	protected final boolean all;
	protected final List<IDataProvider<Boolean>> switchCases; // = new ArrayList<IDataProvider<Boolean>>();
	protected final List<RoutineList> switchRoutines; // = new ArrayList<Routines>();
//	public final boolean isLoaded;
	public final List<String> failedCases = new ArrayList<String>();
	
	protected SwitchRoutine(ScriptLine scriptLine, boolean all, EventInfo info, List<IDataProvider<Boolean>> switchCases, List<RoutineList> switchRoutines)
	{
		super(scriptLine);
		this.all = all;
		
		assert(switchCases.size() == switchRoutines.size());
		this.switchCases = switchCases;
		this.switchRoutines = switchRoutines;
	}
	
	@Override
	public void run(EventData data) throws BailException 
	{
		for(int i = 0; i < switchCases.size(); i++)
		{
			IDataProvider<Boolean> condition = switchCases.get(i);
			Boolean result = condition.get(data);
			if (result == null) continue;
			
			if(result)
			{
				try
				{
					switchRoutines.get(i).run(data);
				}
				catch (BailException e)
				{
					throw new BailException("In case "+ condition.getClass().getSimpleName()
							+" "+ Utils.safeToString(condition), e);
				}
				if (!all) return;
			}
		}
	}
	
	public static void register()
	{
		NestedRoutine.registerRoutine(Pattern.compile("switch(all)?[\\. ](.*)", Pattern.CASE_INSENSITIVE), new RoutineFactory());
	}
	

	public static class SwitchRoutineBuilder implements IRoutineBuilder, ScriptLineHandler
	{
		final ScriptLine scriptLine;
		final String switchType;
		final boolean all;
		final EventInfo info;
		
		final List<IDataProvider<Boolean>> switchCases = new ArrayList<IDataProvider<Boolean>>();
		final List<RoutineList> switchRoutines = new ArrayList<RoutineList>();
		
		public SwitchRoutineBuilder(ScriptLine scriptLine, String switchType, boolean all, EventInfo info)
		{
			this.scriptLine = scriptLine;
			this.switchType = switchType;
			this.all = all;
			this.info = info;
		}

		@Override
		public ScriptLineHandler getScriptLineHandler()
		{
			return this;
		}

		@Override
		public Routine buildRoutine()
		{
			return new SwitchRoutine(scriptLine, all, info, switchCases, switchRoutines);
		}

		@Override
		public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
		{
			IDataProvider<Boolean> matchedCase = DataProvider.parse(info, Boolean.class, switchType + "." + line.line);
			if(matchedCase == null) return null;
			
			RoutineList routines = new RoutineList();
			
			switchCases.add(matchedCase);
			switchRoutines.add(routines);
			
			ModDamageLogger.info(" case " + matchedCase + ":");
			
			return routines.getLineHandler(info);
		}

		@Override
		public void done()
		{
		}

	}
	
	protected static class RoutineFactory extends NestedRoutine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher matcher, ScriptLine scriptLine, EventInfo info)
		{
			NestedRoutine.paddedLogRecord(OutputPreset.INFO, "Switch: \"" + matcher.group(2) + "\"");
			return new SwitchRoutineBuilder(scriptLine, matcher.group(2), matcher.group(1) != null, info);
		}
	}
}
