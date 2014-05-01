package com.ModDamage.Routines.Expressions.Function;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.FunctionParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class WorldNamedFunction implements IDataProvider<World>
{
	private final IDataProvider<String> nameDP;

	private WorldNamedFunction(IDataProvider<String> nameDP)
	{
		this.nameDP = nameDP;
	}

	@Override
	public World get(EventData data) throws BailException
	{
		String name = nameDP.get(data);
		if (name == null) return null;
		
		return Bukkit.getWorld(name);
	}

	@Override
	public Class<World> provides() { return World.class; }

	public static void register()
	{
		DataProvider.register(World.class, null, Pattern.compile("worldnamed", Pattern.CASE_INSENSITIVE), new FunctionParser<World, Object>(String.class)
			{
				@SuppressWarnings("unchecked")
				@Override
				protected IDataProvider<World> makeProvider(EventInfo info, IDataProvider<Object> nullDP, @SuppressWarnings("rawtypes") IDataProvider[] arguments)
				{
					if (nullDP != null) return null;
					
					return new WorldNamedFunction((IDataProvider<String>)arguments[0]);
				}
			});
	}

	@Override
	public String toString()
	{
		return "worldnamed(" + nameDP + ")";
	}
}
