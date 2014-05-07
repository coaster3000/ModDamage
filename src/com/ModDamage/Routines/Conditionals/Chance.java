package com.ModDamage.Routines.Conditionals;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class Chance implements IDataProvider<Boolean>
{
	public static final Pattern pattern = Pattern.compile("chance\\.", Pattern.CASE_INSENSITIVE);
	
	protected final Random random = new Random();
	protected final IDataProvider<Integer> probability;
	
	public Chance(IDataProvider<Integer> probability)
	{
		this.probability = probability;
	}

	@Override
	public Boolean get(EventData data) throws BailException
	{
		Integer prob = probability.get(data);
		if (prob == null) return false;
		
		return Math.abs(random.nextInt()%100) < prob;
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
					IDataProvider<Integer> probability = DataProvider.parse(info, Integer.class, sm.spawn());
					if (probability == null) return null;
					
					sm.accept();
					return new Chance(probability);
				}
			});
	}
	
	@Override
	public String toString()
	{
		return "chance." + probability;
	}
}
