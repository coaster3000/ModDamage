package com.ModDamage.Routines.Expressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class LiteralNumber implements IDataProvider<Number>
{
	public static void register()
	{
		DataProvider.register(Number.class, Pattern.compile("[0-9]+(\\.[0-9]+)?"), new BaseDataParser<Number>()
			{
				@Override
				public IDataProvider<Number> parse(EventInfo info, Matcher m, StringMatcher sm)
				{
					if (m.group(1) != null)
						return sm.acceptIf(new LiteralNumber(Double.parseDouble(m.group(0))));
					else
						return sm.acceptIf(new LiteralNumber(Integer.parseInt(m.group(0))));
				}
			});
	}
	
	Number value;
	
	public LiteralNumber(Number value)
	{
		this.value = value;
	}
	
	@Override
	public Number get(EventData data)
	{
		return value;
	}
	
	@Override
	public Class<? extends Number> provides() { return value.getClass(); }
	
	@Override
	public String toString()
	{
		return ""+value;
	}
}
