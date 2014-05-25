package com.ModDamage.Routines.Expressions.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.StringMatcher;
import com.ModDamage.Utils;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class FormatFunction implements IDataProvider<String>
{
	private final IDataProvider<String> formatDP;
	private final List<IDataProvider<Object>> argsDP;

	private FormatFunction(IDataProvider<String> formatDP, List<IDataProvider<Object>> argsDP)
	{
		this.formatDP = formatDP;
		this.argsDP = argsDP;
	}

	@Override
	public String get(EventData data) throws BailException
	{
		String format = formatDP.get(data);
		if (format == null) return null;
		
		Object[] args = new Object[argsDP.size()];
		
		for (int i = 0; i < args.length; i++) {
			args[i] = argsDP.get(i).get(data);
			if (args[i] == null) return null;
		}
		
		return String.format(format, args);
	}

	@Override
	public Class<String> provides() { return String.class; }

	static final Pattern endPattern = Pattern.compile("\\s*(?:\\)|(,)\\s*)");
	public static void register()
	{
		DataProvider.register(String.class, Pattern.compile("format\\("), new BaseDataParser<String>()
			{
				@Override
				public IDataProvider<String> parse(EventInfo info, Matcher m, StringMatcher sm)
				{
					IDataProvider<String> formatDP = DataProvider.parse(info, String.class, sm.spawn(), false, true, endPattern);
					if (formatDP == null) return null;
					
					List<IDataProvider<Object>> argsDP = new ArrayList<IDataProvider<Object>>();

					while(true)
					{
						Matcher em = sm.matchFront(endPattern);
						if (em == null) {
							ModDamageLogger.error("Expected , or ) at \"" + sm.string + "\"");
							return null;
						}
						
						if (em.group(1) == null) // not a comma
							break;
						
						
						IDataProvider<Object> arg = DataProvider.parse(info, null, sm.spawn(), false, true, endPattern);
						if (arg == null) return null;

						argsDP.add(arg);
					}

					return sm.acceptIf(new FormatFunction(formatDP, argsDP));
				}
			});
	}

	@Override
	public String toString()
	{
		return "format(" + formatDP + ", " + Utils.joinBy(", ", argsDP) + ")";
	}
}