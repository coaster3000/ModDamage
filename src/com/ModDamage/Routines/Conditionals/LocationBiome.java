package com.ModDamage.Routines.Conditionals;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.block.Biome;

import com.ModDamage.StringMatcher;
import com.ModDamage.Utils;
import com.ModDamage.Backend.Configuration.Alias.BiomeAliaser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class LocationBiome extends Conditional<Location>
{
	public static final Pattern pattern = Pattern.compile("\\.biome\\.(\\w+)", Pattern.CASE_INSENSITIVE);
	protected final Collection<Biome> biomes;
	
	public LocationBiome(IDataProvider<Location> locDP, Collection<Biome> biomes)
	{
		super(Location.class, locDP);
		this.biomes = biomes;
	}
	
	@Override
	public Boolean get(Location loc, EventData data)
	{ 
		return biomes.contains(loc.getBlock().getBiome());
	}
	
	@Override
	public String toString()
	{
		return startDP + ".biome." + Utils.joinBy(",", biomes);
	}
	
	
	public static void register()
	{
		DataProvider.register(Boolean.class, Location.class, pattern, new IDataParser<Boolean, Location>()
			{
				@Override
				public IDataProvider<Boolean> parse(EventInfo info, IDataProvider<Location> locDP, Matcher m, StringMatcher sm)
				{
					Collection<Biome> biomes = BiomeAliaser.match(m.group(1));
					if(biomes.isEmpty()) return null;
					
					return new LocationBiome(locDP, biomes);
				}
			});
	}
}
