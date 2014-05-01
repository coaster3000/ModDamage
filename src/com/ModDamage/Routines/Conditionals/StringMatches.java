package com.ModDamage.Routines.Conditionals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class StringMatches extends Conditional<String> 
{
	public static final Pattern pattern = Pattern.compile("\\.(i)?matches\\.(?:\"([^\"]+)\"|'([^']+)')", Pattern.CASE_INSENSITIVE);
	
	private final Pattern matchPattern;
	
	public StringMatches(IDataProvider<String> stringDP, Pattern matchPattern)
	{
		super(String.class, stringDP);
		this.matchPattern = matchPattern;
	}
	@Override
	public Boolean get(String str, EventData data) throws BailException
	{
		return matchPattern.matcher(str).matches();
	}
	
	@Override
	public String toString()
	{
		return startDP + ".matches." + matchPattern.pattern();
	}
	
	
	public static void register()
	{
		DataProvider.register(Boolean.class, String.class, pattern, new IDataParser<Boolean, String>()
			{
				@Override
				public IDataProvider<Boolean> parse(EventInfo info, IDataProvider<String> stringDP, Matcher m, StringMatcher sm)
				{
					String p = m.group(2);
					if (p == null)
						p = m.group(3);
					
					Pattern matchPattern;
					try {
						if (m.group(1) != null)
							matchPattern = Pattern.compile(p, Pattern.CASE_INSENSITIVE);
						else
							matchPattern = Pattern.compile(p);
					}
					catch (PatternSyntaxException ps)
					{
						ModDamageLogger.error("Invalid regex: " + ps.getDescription());
						return null;
					}
					
					return new StringMatches(stringDP, matchPattern);
				}
			});
	}
}
