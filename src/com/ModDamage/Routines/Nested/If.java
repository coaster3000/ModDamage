package com.ModDamage.Routines.Nested;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine;
import com.ModDamage.Routines.Conditionals.Conditional;

public class If extends NestedRoutine
{
	public final IDataProvider<Boolean> conditional;
	public final boolean isElse;
	public If elseRoutine = null;
	
	private If(ScriptLine scriptLine, boolean isElse, IDataProvider<Boolean> conditional)
	{
		super(scriptLine);
		this.isElse = isElse;
		this.conditional = conditional;
	}
	
	@Override
	public void run(EventData data) throws BailException
	{
		if (conditional == null) { // else
			routines.run(data);
			return;
		}
		
		Boolean result = conditional.get(data);
		if(result != null) {
			if (result) {
				routines.run(data);
				return;
			}
		}
		
		if (elseRoutine != null)
			elseRoutine.run(data);
	}
	
	
	public static void register()
	{
		Routine.registerRoutine(Pattern.compile("(?:(else\\s*|el)?if\\s+(.*)|(else))", Pattern.CASE_INSENSITIVE), new RoutineFactory());
		Conditional.register();
	}

	protected static class RoutineFactory extends NestedRoutine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher matcher, ScriptLine scriptLine, EventInfo info)
		{
			boolean isElse = matcher.group(1) != null || matcher.group(3) != null;
			
			IDataProvider<Boolean> conditional;
			if (matcher.group(3) == null)
			{
				conditional = DataProvider.parse(info, Boolean.class, matcher.group(2));
				if (conditional == null) return null;
			}
			else
			{
				conditional = null;
			}
			
			ModDamageLogger.info((isElse? "Else " : "") + (matcher.group(3) != null? "" : ("If: " + conditional)));
			
			
			If routine = new If(scriptLine, isElse, conditional);
			return new NestedRoutineBuilder(routine, routine.routines, info);
		}
	}
}
