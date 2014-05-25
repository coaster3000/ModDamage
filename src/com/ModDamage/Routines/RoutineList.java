package com.ModDamage.Routines;

import java.util.ArrayList;
import java.util.List;

import com.ModDamage.ModDamage;
import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.ScriptLineHandler;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine.IRoutineBuilder;
import com.ModDamage.Routines.Nested.If;

public class RoutineList
{
	public List<Routine> routines;
	
	public RoutineList()
	{
		this(new ArrayList<Routine>());
	}
	
	public RoutineList(List<Routine> routines)
	{
		this.routines = routines;
	}
	
	public void run(EventData data) throws BailException
	{
		if (routines != null)
			for (Routine routine : routines)
			{
				try
				{
					routine.run(data);
				}
				catch (Throwable e)
				{
					throw new BailException(routine, e);
				}
			}
	}

	public boolean isEmpty()
	{
		return routines == null || routines.isEmpty();
	}
	
	public RoutinesLineHandler getLineHandler(EventInfo info)
	{
		ModDamage.changeIndentation(true);
		return new RoutinesLineHandler(info);
	}
	
	protected class RoutinesLineHandler implements ScriptLineHandler
	{
		IRoutineBuilder lastBuilder = null;
		EventInfo info;
		
		public RoutinesLineHandler(EventInfo info) {
			this.info = info;
		}
		
		private void buildLast()
		{
			if (lastBuilder != null) {
				Routine routine = lastBuilder.buildRoutine();
				if (routine != null) {
					if (routine instanceof If && ((If) routine).isElse) {
						Routine prev = null;
						if (!routines.isEmpty())
							prev = routines.get(routines.size()-1);
						
						if (prev == null || !(prev instanceof If))
							ModDamageLogger.error("Else not after If");
						else if (((If) prev).conditional == null)
							ModDamageLogger.error("Illegal Else after Else");
						else {
							If prevIf = (If) prev;
							
							while (prevIf.elseRoutine != null)
								prevIf = prevIf.elseRoutine;
							
							prevIf.elseRoutine = (If) routine;
						}
					}
					else
						routines.add(routine);
				}
				lastBuilder = null;
			}
		}

		@Override
		public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
		{
			buildLast();
			lastBuilder = Routine.getNew(line, info);
			if (lastBuilder != null)
				return lastBuilder.getScriptLineHandler();
			return null;
		}

		@Override
		public void done()
		{
			buildLast();
			ModDamage.changeIndentation(false);
		}
	}
}