package com.ModDamage.Backend.Minecraft.Variables.Number;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class LocalNum
{	
	public static void register()
	{
		DataProvider.register(Number.class, null, Pattern.compile("\\$(\\w+)", Pattern.CASE_INSENSITIVE), new BaseDataParser<Number>()
				{
					@Override
					public IDataProvider<Number> parse(EventInfo info, Matcher m, StringMatcher sm)
					{
						return info.getLocal(m.group(1).toLowerCase());
					}
				});
	}
}