package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.KoryuObihiro.bukkit.ModDamage.PluginConfiguration.OutputPreset;
import com.KoryuObihiro.bukkit.ModDamage.Backend.TargetEventInfo;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.RoutineAliaser;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Matching.DynamicInteger;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.Calculate;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.ChangeProperty;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.EntityExplode;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.EntityHeal;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.EntityHurt;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.EntityItemAction;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.EntitySpawn;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.EntityUnknownHurt;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Nested.Calculation.McMMOChangeSkill;

abstract public class CalculationRoutine extends NestedRoutine 
{
	private static LinkedHashMap<Pattern, CalculationBuilder> registeredCalculations = new LinkedHashMap<Pattern, CalculationBuilder>();
	protected final static Pattern calculationPattern = Pattern.compile("((?:([\\*\\w]+)effect\\.(.*)))", Pattern.CASE_INSENSITIVE);
	
	protected final DynamicInteger value;
	
	protected CalculationRoutine(String configString, DynamicInteger value)
	{
		super(configString);
		this.value = value;
	}
	
	@Override
	public void run(TargetEventInfo eventInfo)
	{
		int eventValue = eventInfo.eventValue;
			doCalculation(eventInfo, value.getValue(eventInfo));
		eventInfo.eventValue = eventValue;
	}

	abstract protected void doCalculation(TargetEventInfo eventInfo, int input);
	
	public static void register()
	{
		registeredCalculations.clear();
		NestedRoutine.registerRoutine(calculationPattern, new RoutineBuilder());
		
		McMMOChangeSkill.register();

		Calculate.register();
		ChangeProperty.register();
		EntityItemAction.register();
		EntityExplode.register();
		EntityHeal.register();
		EntityHurt.register();
		EntitySpawn.register();
		EntityUnknownHurt.register();
	}	

	public static void registerRoutine(Pattern syntax, CalculationBuilder builder)
	{
		Routine.addBuilderToRegistry(registeredCalculations, syntax, builder);
	}
	
	protected static final class RoutineBuilder extends NestedRoutine.RoutineBuilder
	{
		@Override
		public CalculationRoutine getNew(Matcher calculationMatcher, Object nestedContent)
		{
			if(calculationMatcher.group() != null && nestedContent != null)
			{
				NestedRoutine.paddedLogRecord(OutputPreset.INFO, "Calculation: \"" + calculationMatcher.group() + "\"");
				for(Entry<Pattern, CalculationBuilder> entry : registeredCalculations.entrySet())
				{
					Matcher matcher = entry.getKey().matcher(calculationMatcher.group());
					if(matcher.matches())
					{
						List<Routine> routines = new ArrayList<Routine>();
						if(RoutineAliaser.parseRoutines(routines, nestedContent))
						{
							DynamicInteger match = DynamicInteger.getNew(routines);
							NestedRoutine.paddedLogRecord(OutputPreset.INFO_VERBOSE, "End Calculation \"" + calculationMatcher.group() + "\"");
							return entry.getValue().getNew(matcher, match);
						}
						else
						{
							NestedRoutine.paddedLogRecord(OutputPreset.FAILURE, "Bad content in Calculation \"" + calculationMatcher.group() + "\"");
							return null;
						}
					}
				}
				NestedRoutine.paddedLogRecord(OutputPreset.FAILURE, "Invalid Calculation \"" + calculationMatcher.group() + "\"");
			}
			return null;
		}
	}
	
	abstract protected static class CalculationBuilder
	{
		abstract public CalculationRoutine getNew(Matcher matcher, DynamicInteger integer);
	}
}