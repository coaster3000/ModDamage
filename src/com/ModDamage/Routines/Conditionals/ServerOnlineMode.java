package com.ModDamage.Routines.Conditionals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class ServerOnlineMode implements IDataProvider<Boolean>
{
	public static final Pattern pattern = Pattern.compile("server\\.onlineMode", Pattern.CASE_INSENSITIVE);
	
	@Override
	public Boolean get(EventData data)
	{
		return Bukkit.getOnlineMode();
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
					return new ServerOnlineMode();
				}
			});
	}
}
