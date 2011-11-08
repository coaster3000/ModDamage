package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Conditional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.Backend.ArmorSet;
import com.KoryuObihiro.bukkit.ModDamage.Backend.EntityReference;
import com.KoryuObihiro.bukkit.ModDamage.Backend.TargetEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.ConditionalRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.ConditionalStatement;

public class EntityWearing extends EntityConditionalStatement
{
	final boolean inclusiveComparison;
	final List<ArmorSet> armorSets;
	public EntityWearing(boolean inverted, boolean inclusiveComparison, EntityReference entityReference, List<ArmorSet> armorSets)
	{  
		super(inverted, entityReference);
		this.inclusiveComparison = inclusiveComparison;
		this.armorSets = armorSets;
	}
	@Override
	public boolean condition(TargetEventInfo eventInfo)
	{
		ArmorSet playerSet = entityReference.getArmorSet(eventInfo);
		if(playerSet != null)
			for(ArmorSet armorSet : armorSets)
				if(inclusiveComparison?armorSet.equals(playerSet):armorSet.contains(playerSet))
					return true;
		return false;
	}
	
	public static void register()
	{
		ConditionalRoutine.registerConditionalStatement(Pattern.compile("(!?)(\\w+)\\.(wearing|wearingonly)\\.([\\*\\w]+)", Pattern.CASE_INSENSITIVE), new StatementBuilder());
	}
	
	protected static class StatementBuilder extends ConditionalStatement.StatementBuilder
	{	
		@Override
		public EntityWearing getNew(Matcher matcher)
		{
			List<ArmorSet> armorSet = ModDamage.matchArmorAlias(matcher.group(4));
			if(!armorSet.isEmpty() && EntityReference.isValid(matcher.group(2)))
				return new EntityWearing(matcher.group(1).equalsIgnoreCase("!"), matcher.group(3).endsWith("only"), EntityReference.match(matcher.group(2)), armorSet);
			return null;
		}
	}
}
