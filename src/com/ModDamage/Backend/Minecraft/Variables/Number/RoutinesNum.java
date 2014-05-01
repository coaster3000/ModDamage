package com.ModDamage.Backend.Minecraft.Variables.Number;

import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Configuration.Parsing.ISettableDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.RoutineList;

public class RoutinesNum implements IDataProvider<Number>
{
	private final RoutineList routines;
	private final ISettableDataProvider<Number> defaultDP;
	
	public RoutinesNum(RoutineList routines, EventInfo info)
	{
		this.routines = routines;
		this.defaultDP = info.get(Number.class, "-default");
	}
	
	@Override
	public Number get(EventData data) throws BailException
	{
		routines.run(data);
		
		return defaultDP.get(data);
	}

	@Override
	public Class<Number> provides() { return Number.class; }
	
	@Override
	public String toString()
	{
		return "<routines>"; //TODO Make this a bit better?
	}
}
