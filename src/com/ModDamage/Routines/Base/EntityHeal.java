package com.ModDamage.Routines.Base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.LivingEntity;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine;

public class EntityHeal extends Routine
{
	private final IDataProvider<LivingEntity> livingDP;
	private final IDataProvider<Number> heal_amount;
	
	public EntityHeal(ScriptLine scriptLine, IDataProvider<LivingEntity> livingDP, IDataProvider<Number> heal_amount)
	{
		super(scriptLine);
		this.livingDP = livingDP;
		this.heal_amount = heal_amount;
	}
	
	@Override
	public void run(EventData data) throws BailException
	{
		Number ha = heal_amount.get(data);
		if (ha == null) return;
		
		LivingEntity entity = livingDP.get(data);
		if (entity == null) return;
		
		entity.setHealth(Math.min(entity.getHealth() + ha.doubleValue(), entity.getMaxHealth()));
	}

	public static void register()
	{
		Routine.registerRoutine(Pattern.compile("(.*?)(?:effect)?\\.heal(?::|\\s+by\\s|\\s)\\s*(.+)", Pattern.CASE_INSENSITIVE), new RoutineFactory());
	}
	
	protected static class RoutineFactory extends Routine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher matcher, ScriptLine scriptLine, EventInfo info)
		{
			String name = matcher.group(1).toLowerCase();
			
			IDataProvider<LivingEntity> livingDP = DataProvider.parse(info, LivingEntity.class, name);
            if (livingDP == null) return null;
            
			IDataProvider<Number> heal_amount = DataProvider.parse(info, Number.class, matcher.group(2));
            if (heal_amount == null) return null;

            ModDamageLogger.info("Heal "+livingDP+" by "+heal_amount);

			return new RoutineBuilder(new EntityHeal(scriptLine, livingDP, heal_amount));
		}
	}
}