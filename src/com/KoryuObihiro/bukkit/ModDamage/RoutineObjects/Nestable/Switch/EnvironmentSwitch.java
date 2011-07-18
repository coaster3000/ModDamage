package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nestable.Switch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.World.Environment;

import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.SpawnEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.RoutineUtility;

public class EnvironmentSwitch extends SwitchRoutine<Environment>
{	
	public EnvironmentSwitch(LinkedHashMap<String, List<Routine>> switchStatements){ super(switchStatements);}

	@Override
	protected Environment getRelevantInfo(DamageEventInfo eventInfo){ return eventInfo.environment;}

	@Override
	protected Environment getRelevantInfo(SpawnEventInfo eventInfo){ return eventInfo.environment;}	

	@Override
	protected Environment matchCase(String switchCase){ return RoutineUtility.matchEnvironment(switchCase);}
	
	public static void register(RoutineUtility routineUtility)
	{
		SwitchRoutine.registerStatement(routineUtility, EnvironmentSwitch.class, Pattern.compile(RoutineUtility.entityPart + "environment", Pattern.CASE_INSENSITIVE));
	}
	
	public static EnvironmentSwitch getNew(Matcher matcher, LinkedHashMap<String, List<Routine>> switchStatements)
	{
		EnvironmentSwitch routine = new EnvironmentSwitch(switchStatements);
		return (routine.isLoaded?routine:null);
	}
}
