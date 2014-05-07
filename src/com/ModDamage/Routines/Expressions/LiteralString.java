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

public class LiteralString implements IDataProvider<String>
{
	public static void register()
	{
		DataProvider.register(String.class, Pattern.compile("\"((?:[^\\\\\"]+|\\\\.)*)\"|'((?:[^\\\\\']+|\\\\.)*)'"), new BaseDataParser<String>()
			{
				@Override
				public IDataProvider<String> parse(EventInfo info, Matcher m, StringMatcher sm)
				{
					String v = m.group(1);
					if (v == null) v = m.group(2);
					StringBuilder sb = new StringBuilder(v.length());
					int i = 0;
					int len = v.length();
					while (i < len)
					{
						char c = v.charAt(i++);
						
						if (c == '\\') {
							c = v.charAt(i++);
							if (c != '"') {
								if (c == '&') {
									sb.append("\u00a7");
									continue;
								}
								sb.append('\\');
							}
						}
						
						sb.append(c);
					}
					
					
					return sm.acceptIf(new LiteralString(new InterpolatedString(sb.toString(), info, false)));
				}
			});
	}
	
	InterpolatedString value;
	
	public LiteralString(InterpolatedString value)
	{
		this.value = value;
	}
	
	@Override
	public String get(EventData data) throws BailException
	{
		return value.get(data);
	}
	
	@Override
	public Class<String> provides() { return String.class; }
	
	public void colorize()
	{
		value.colorize();
	}
	
	@Override
	public String toString()
	{
		return "\""+value.toString()+"\"";
	}
}
