package com.ModDamage.Routines.Expressions;

import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Configuration.Parsing.SettableDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;

public abstract class SettableIntegerExpression<From> extends SettableDataProvider<Integer, From>
{
	protected SettableIntegerExpression(Class<From> wantStart, IDataProvider<From> startDP)
	{
		super(wantStart, startDP);
		defaultValue = 0;
	}
	
	public final Integer get(From from, EventData data) throws BailException
	{
		try
		{
			return myGet(from, data);
		}
		catch (Throwable t)
		{
			throw new BailException(this, t);
		}
	}
	protected abstract Integer myGet(From from, EventData data) throws BailException;
	
	public final void set(From from, EventData data, Integer value) throws BailException
	{
		try
		{
			mySet(from, data, value);
		}
		catch (Throwable t)
		{
			throw new BailException(this, t);
		}
	}
	protected abstract void mySet(From from, EventData data, Integer value) throws BailException;
	
	@Override
	public Class<Integer> provides() { return Integer.class; }
	
	public boolean isSettable(){ return true; }
}