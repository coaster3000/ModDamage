package com.ModDamage.Routines.Expressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class NestedExpression<T> implements IDataProvider<T>
{
	public static final Pattern openParen = Pattern.compile("\\s*\\(\\s*");
	public static final Pattern closeParen = Pattern.compile("\\s*\\)\\s*");
	
	public static void register()
	{
		DataProvider.register(Object.class, openParen, new BaseDataParser<Object>()
			{
				@Override
				@SuppressWarnings({ "rawtypes", "unchecked" })
				public IDataProvider<Object> parse(EventInfo info, Matcher m, StringMatcher sm)
				{
					IDataProvider<?> nestedDP;
					nestedDP = DataProvider.parse(info, null, sm.spawn(), false, true, closeParen);
					
					if (nestedDP == null || !sm.matchesFront(closeParen)) return null;
					
					sm.accept();
					return (IDataProvider<Object>) new NestedExpression(nestedDP);
				}
			});
	}
	
	IDataProvider<T> inner;
	
	public NestedExpression(IDataProvider<T> inner)
	{
		this.inner = inner;
	}
	
	@Override
	public T get(EventData data) throws BailException
	{
		return inner.get(data);
	}
	
	@Override
	public Class<? extends T> provides() { return inner.provides(); }
	
	@Override
	public String toString()
	{
		return "("+inner+")";
	}
}