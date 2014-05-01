package com.ModDamage.Routines.Conditionals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class InvertBoolean implements IDataProvider<Boolean>
{
	public static final Pattern pattern = Pattern.compile("!\\s*");
	
	private IDataProvider<Boolean> bool;
	
	public InvertBoolean(IDataProvider<Boolean> bool)
	{
		this.bool = bool;
	}
	
	@Override
	public Boolean get(EventData data) throws BailException
	{
		Boolean b = bool.get(data);
		if (b == null) return true;
		return !b;
	}

	@Override
	public Class<Boolean> provides() { return Boolean.class; }
	
	public static void register()
	{
		DataProvider.register(Boolean.class, pattern, new BaseDataParser<Boolean>()
			{
				@Override
				public IDataProvider<Boolean> parse(EventInfo info, Matcher m, StringMatcher sm)
				{
					IDataProvider<Boolean> bool = DataProvider.parse(info, Boolean.class, sm.spawn());
					if (bool == null) return null;
					
					sm.accept();
					return invert(bool);
				}
			});
	}
	
	// This is a hacky way to fix ! precedence
	public static IDataProvider<Boolean> invert(IDataProvider<Boolean> bool) {

		if (bool instanceof CompoundConditional) {
			CompoundConditional cc = (CompoundConditional) bool;
			
			cc.startDP = invert(cc.startDP);
			
			return cc;
		}
		
		return new InvertBoolean(bool);
	}
	
	@Override
	public String toString()
	{
		return "!" + bool;
	}
}
