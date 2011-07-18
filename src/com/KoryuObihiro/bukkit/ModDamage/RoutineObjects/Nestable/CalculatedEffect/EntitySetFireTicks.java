package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nestable.CalculatedEffect;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.entity.LivingEntity;

import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.RoutineUtility;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

public class EntitySetFireTicks extends EntityCalculatedEffectRoutine
{
	public EntitySetFireTicks(boolean forAttacker, List<Routine> calculations){ super(forAttacker, calculations);}

	@Override
	void applyEffect(LivingEntity affectedObject, int input) 
	{
		affectedObject.setFireTicks(input);
	}

	public static void register(RoutineUtility routineUtility)
	{
		routineUtility.registerBase(EntitySetFireTicks.class, Pattern.compile(RoutineUtility.entityPart + "effect\\.setfireticks", Pattern.CASE_INSENSITIVE));
	}
}
