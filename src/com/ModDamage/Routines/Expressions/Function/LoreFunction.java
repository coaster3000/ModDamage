package com.ModDamage.Routines.Expressions.Function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Configuration.Parsing.SettableDataProvider;
import com.ModDamage.Backend.Minecraft.ItemHolder;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class LoreFunction extends SettableDataProvider<String, ItemHolder>
{
	private final IDataProvider<Integer> indexDP;

	private LoreFunction(IDataProvider<ItemHolder> stringDP, IDataProvider<Integer> indexDP)
	{
		super(ItemHolder.class, stringDP);
		this.indexDP = indexDP;
	}

	@Override
	public String get(ItemHolder holder, EventData data) throws BailException
	{
		Integer index = indexDP.get(data);
		if (index == null) return null;
		
		return holder.getLore(index);
	}

	@Override
	public void set(ItemHolder holder, EventData data, String value) throws BailException
	{
		Integer index = indexDP.get(data);
		if (index == null) return;
		
		holder.setLore(index, value);
	}

	@Override
	public boolean isSettable()
	{
		return true;
	}

	@Override
	public Class<String> provides() { return String.class; }

	static final Pattern endPattern = Pattern.compile("\\s*\\)");
	public static void register()
	{
		DataProvider.register(String.class, ItemHolder.class, Pattern.compile("_lore\\("), new IDataParser<String, ItemHolder>()
			{
				@Override
				public IDataProvider<String> parse(EventInfo info, IDataProvider<ItemHolder> holderDP, Matcher m, StringMatcher sm)
				{
					IDataProvider<Integer> indexDP = DataProvider.parse(info, Integer.class, sm.spawn());
					if (indexDP == null) return null;

					Matcher endMatcher = sm.matchFront(endPattern);
					if (endMatcher == null)
					{
						ModDamageLogger.error("Missing end paren: \"" + sm.string + "\"");
						return null;
					}

					return sm.acceptIf(new LoreFunction(holderDP, indexDP));
				}
			});
	}

	@Override
	public String toString()
	{
		return startDP + "_lore(" + indexDP + ")";
	}
}