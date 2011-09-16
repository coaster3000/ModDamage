package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Calculation;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.LivingEntity;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.Backend.EntityReference;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.CalculationRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

public class EntitySetHealth extends LivingEntityCalculationRoutine
{
	public EntitySetHealth(String configString, EntityReference entityReference, List<Routine> routines)
	{
		super(configString, entityReference, routines);
	}

	@Override
	protected void applyEffect(LivingEntity affectedObject, int input) 
	{
		affectedObject.setHealth(input);
	}
	
	public static void register(ModDamage routineUtility)
	{
		CalculationRoutine.registerStatement(EntitySetHealth.class, Pattern.compile("(\\w+)effect\\.sethealth", Pattern.CASE_INSENSITIVE));
	}
	
	public static EntitySetHealth getNew(Matcher matcher, List<Routine> routines)
	{
		if(matcher != null && routines != null && EntityReference.isValid(matcher.group(1)))
			return new EntitySetHealth(matcher.group(), EntityReference.match(matcher.group(1)), routines);
		return null;
	}
}