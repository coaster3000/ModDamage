package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nestable.Switch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.KoryuObihiro.bukkit.ModDamage.Backend.ArmorSet;
import com.KoryuObihiro.bukkit.ModDamage.Backend.DamageEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.SpawnEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.RoutineUtility;

public class ArmorSetSwitch extends EntitySwitchCalculation<ArmorSet>
{
	public ArmorSetSwitch(boolean forAttacker, LinkedHashMap<String, List<Routine>> switchStatements){ super(forAttacker, switchStatements);}

	@Override
	protected ArmorSet getRelevantInfo(DamageEventInfo eventInfo){ return (forAttacker?eventInfo.armorSetString_attacker:eventInfo.armorSetString_target);}

	@Override
	protected ArmorSet getRelevantInfo(SpawnEventInfo eventInfo){ return null;}
	
	@Override
	protected ArmorSet matchCase(String switchCase)
	{ 
		ArmorSet armorSet = new ArmorSet(switchCase);
		return (armorSet.isEmpty()?null:armorSet);
	}
	
	public static void register(RoutineUtility routineUtility)
	{
		SwitchRoutine.registerStatement(routineUtility, BiomeSwitch.class, Pattern.compile(RoutineUtility.entityPart + "environment", Pattern.CASE_INSENSITIVE));
	}
	
	public static ArmorSetSwitch getNew(Matcher matcher, LinkedHashMap<String, List<Routine>> switchStatements)
	{
		boolean forAttacker = matcher.group(1).equalsIgnoreCase("attacker");
		ArmorSetSwitch routine = new ArmorSetSwitch(forAttacker, switchStatements);
		return (routine.isLoaded?routine:null);
	}

}
