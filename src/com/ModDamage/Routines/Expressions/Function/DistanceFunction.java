package com.ModDamage.Routines.Expressions.Function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class DistanceFunction implements IDataProvider<Integer>
{
	private final IDataProvider<Location> first, second;
	
	private DistanceFunction(IDataProvider<Location> first, IDataProvider<Location> second)
	{
		this.first = first;
		this.second = second;
	}

	@Override
	public Integer get(EventData data) throws BailException
	{
		Location f = first.get(data);
		Location s = second.get(data);
		if (f == null || s == null) return 0;
		
		return (int) f.distance(s);
	}

	@Override
	public Class<Integer> provides()
	{
		return Integer.class;
	}
	
	static final Pattern commaPattern = Pattern.compile("\\s*,\\s*");
	static final Pattern endPattern = Pattern.compile("\\s*\\)");
	public static void register()
	{
		DataProvider.register(Integer.class, Pattern.compile("(dist(?:ance)?)\\s*\\("), new BaseDataParser<Integer>()
			{
				@Override
				public IDataProvider<Integer> parse(EventInfo info, Matcher m, StringMatcher sm)
				{
					@SuppressWarnings("unchecked")
					IDataProvider<Location>[] args = new IDataProvider[2];
					
					for (int i = 0; i < 2; i++)
					{
						IDataProvider<Location> arg = DataProvider.parse(info, Location.class, sm.spawn());
						if (arg == null)
						{
							ModDamageLogger.error("Unable to match expression: \"" + sm.string + "\"");
							return null;
						}
						
						args[i] = arg;
						
						if ((sm.matchFront(commaPattern) == null) != (i == 1))
						{
							ModDamageLogger.error("Wrong number of parameters for " + m.group(1) + " function: "+i);
							return null;
						}
					}
					
					
					Matcher endMatcher = sm.matchFront(endPattern);
					if (endMatcher == null)
					{
						ModDamageLogger.error("Missing end paren: \"" + sm.string + "\"");
						return null;
					}
					
					return sm.acceptIf(new DistanceFunction(args[0], args[1]));
				}
			});
	}

	@Override
	public String toString()
	{
		return "distance(" + first + ", " + second + ")";
	}
}