package com.ModDamage.Routines.Base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.Alias.RoutineAliaser;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine;
import com.ModDamage.Routines.RoutineList;

public class AliasedRoutine extends Routine
{
	private RoutineList routines;
	
	public AliasedRoutine(ScriptLine scriptLine, final EventInfo info, final String alias)
	{
		super(scriptLine);
		
		// fetch after, to avoid infinite recursion
		RoutineAliaser.whenDoneParsingAlias(new Runnable() {
				@Override public void run() {
					routines = RoutineAliaser.match(alias, info);
				}
			});
	}

	@Override
	public void run(EventData data) throws BailException
	{
		if (routines != null)
			routines.run(data);
	}

	public static void register()
	{
		Routine.registerRoutine(Pattern.compile("_\\w+", Pattern.CASE_INSENSITIVE), new RoutineFactory());
	}
	
	protected static class RoutineFactory extends Routine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher matcher, ScriptLine scriptLine, EventInfo info)
		{
			String alias = matcher.group();
			ModDamageLogger.info("Routine Alias: \"" + alias + "\"");
			return new RoutineBuilder(new AliasedRoutine(scriptLine, info, alias));
		}
	}

}
