package com.ModDamage.Routines.Expressions.Function;

import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.ModDamage.Utils;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.FunctionParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class BlockFunction extends DataProvider<Block, World>
{
	private final IDataProvider<Integer>[] args;

	private BlockFunction(IDataProvider<World> worldDP, IDataProvider<Integer>[] args)
	{
		super(World.class, worldDP);
		this.args = args;
	}

	@Override
	public Block get(World world, EventData data) throws BailException
	{
		int[] argValues = new int[args.length];

		for (int i = 0; i < argValues.length; i++) {
			Integer value = args[i].get(data);
			if (value == null)
				return null;
			
			argValues[i] = value;
		}

		return world.getBlockAt(argValues[0], argValues[1], argValues[2]);
	}

	@Override
	public Class<Block> provides() { return Block.class; }

	public static void register()
	{
		DataProvider.register(Block.class, World.class, Pattern.compile("_block"), new FunctionParser<Block, World>(Integer.class, Integer.class, Integer.class)
			{
				@SuppressWarnings("unchecked")
				@Override
				protected IDataProvider<Block> makeProvider(EventInfo info, IDataProvider<World> worldDP, @SuppressWarnings("rawtypes") IDataProvider[] arguments)
				{
					return new BlockFunction(worldDP, arguments);
				}
			});
	}

	@Override
	public String toString()
	{
		return startDP + "_block(" + Utils.joinBy(", ", args) + ")";
	}
}
