package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.Backend.ArmorSet;
import com.KoryuObihiro.bukkit.ModDamage.Backend.AttackerEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.TargetEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.ConditionalRoutine;

public class PlayerWearing extends EntityConditionalStatement<List<ArmorSet>>
{
	public PlayerWearing(boolean inverted, boolean forAttacker, List<ArmorSet> armorSet)
	{  
		super(inverted, forAttacker, armorSet);
	}
	@Override
	public boolean condition(TargetEventInfo eventInfo)
	{
		if((shouldGetAttacker(eventInfo)?((AttackerEventInfo)eventInfo).armorSet_attacker:eventInfo.armorSet_target) != null)
		{
			ArmorSet playerSet = (shouldGetAttacker(eventInfo)?((AttackerEventInfo)eventInfo).armorSet_attacker:eventInfo.armorSet_target);
			for(ArmorSet armorSet : value)
				if(armorSet.contains(playerSet))
					return true;
		}
		return false;
	}
	@Override
	protected List<ArmorSet> getRelevantInfo(TargetEventInfo eventInfo) { return null;}
	
	public static void register(ModDamage routineUtility)
	{
		ConditionalRoutine.registerStatement(routineUtility, PlayerWearing.class, Pattern.compile("(!?)(\\w+)\\.wearing\\.(\\w+)", Pattern.CASE_INSENSITIVE));
	}
	
	public static PlayerWearing getNew(Matcher matcher)
	{
		if(matcher != null)
		{
			List<ArmorSet> armorSet = ModDamage.matchArmorAlias(matcher.group(3));
			if(!armorSet.isEmpty())
				return new PlayerWearing(matcher.group(1).equalsIgnoreCase("!"), (ModDamage.matchesValidEntity(matcher.group(2)))?ModDamage.matchEntity(matcher.group(2)):false, armorSet);
		}
		return null;
	}
}
