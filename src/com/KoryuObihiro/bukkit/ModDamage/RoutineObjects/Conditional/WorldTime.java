package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.World;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.SpawnEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.ConditionalRoutine;

public class WorldTime extends WorldConditionalStatement
{
	private final int beginningTime;
	private final int endTime;

	public WorldTime(boolean inverted, World world, int beginningTime, int endTime)
	{
		super(beginningTime > endTime | inverted, world);
		this.beginningTime = beginningTime;
		this.endTime = endTime;
	}
	public WorldTime(boolean inverted, int beginningTime, int endTime)
	{
		super(beginningTime > endTime | inverted);
		this.beginningTime = beginningTime;
		this.endTime = endTime;
	}
	
	@Override
	public boolean condition(DamageEventInfo eventInfo){ return (world.getTime() > beginningTime && world.getTime() < endTime);}
	@Override
	public boolean condition(SpawnEventInfo eventInfo){ return (world.getTime() > beginningTime && world.getTime() < endTime);}
	
	public static void register(ModDamage routineUtility)
	{
		ConditionalRoutine.registerStatement(routineUtility, WorldTime.class, Pattern.compile("world\\.time\\.([0-9]+)\\.([0-9]+)", Pattern.CASE_INSENSITIVE));
	}
	
	public static WorldTime getNew(Matcher matcher)
	{
		if(matcher != null)
		{
			
		}
		return null;
	}
}
