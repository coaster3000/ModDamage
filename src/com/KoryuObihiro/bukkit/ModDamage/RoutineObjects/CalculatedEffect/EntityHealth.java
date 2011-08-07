package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.CalculatedEffect;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.LivingEntity;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

public class EntityHealth extends EntityCalculatedEffectRoutine
{
	public EntityHealth(boolean forAttacker, List<Routine> routines){ super(forAttacker, routines);}

	@Override
	protected void applyEffect(LivingEntity affectedObject, int input){ affectedObject.setHealth(input);}
	
	public static void register(ModDamage routineUtility)
	{
		ModDamage.registerEffect(EntityHealth.class, Pattern.compile(ModDamage.entityRegex + "effect\\.heal", Pattern.CASE_INSENSITIVE));
	}
	
	public static EntityHealth getNew(Matcher matcher, List<Routine> routines)
	{
		if(matcher != null && routines != null)
			return new EntityHealth(matcher.group(1).equalsIgnoreCase("attacker"), routines);
		return null;
	}
}
