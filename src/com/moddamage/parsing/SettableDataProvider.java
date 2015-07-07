package com.moddamage.parsing;

import java.util.regex.Pattern;

import com.moddamage.LogUtil;
import com.moddamage.StringMatcher;
import com.moddamage.backend.BailException;
import com.moddamage.backend.ScriptLine;
import com.moddamage.eventinfo.EventData;
import com.moddamage.eventinfo.EventInfo;

public abstract class SettableDataProvider<T, S> extends DataProvider<T, S> implements ISettableDataProvider<T>
{
	protected SettableDataProvider(Class<S> wantStart, IDataProvider<S> startDP)
	{
		super(wantStart, startDP);
	}
	@SuppressWarnings("unchecked")
	@Override
	public void set(EventData data, T value) throws BailException
	{
		Object ostart = startDP.get(data);
		if (ostart != null && wantStart.isInstance(ostart))
			set((S) ostart, data, value);
	}
	public abstract void set(S start, EventData data, T value) throws BailException;
	
	
	public static <T> ISettableDataProvider<T> parse(ScriptLine scriptLine, EventInfo info, Class<T> want, String s)
	{
		return parse(scriptLine, info, want, s, true, true);
	}
	
	public static <T> ISettableDataProvider<T> parse(ScriptLine scriptLine, EventInfo info, Class<T> want, String s, boolean finish, boolean complain)
	{
		return parse(scriptLine, info, want, new StringMatcher(s), finish, complain, null);
	}

	public static <T> ISettableDataProvider<T> parse(ScriptLine scriptLine, EventInfo info, Class<T> want, StringMatcher sm)
	{
		return parse(scriptLine, info, want, sm, false, true, null);
	}
	
	public static <T> ISettableDataProvider<T> parse(ScriptLine scriptLine, EventInfo info, Class<T> want, StringMatcher sm, boolean finish, boolean complain, Pattern endPattern)
	{
		IDataProvider<T> dp = DataProvider.parse(scriptLine, info, want, sm, finish, complain, endPattern);
		if (dp == null) return null;
		
		if (!(dp instanceof ISettableDataProvider) || !((ISettableDataProvider<?>)dp).isSettable())
		{
			LogUtil.error(scriptLine, dp+" is not settable");
			return null;
		}
		return (ISettableDataProvider<T>) dp;
	}
}