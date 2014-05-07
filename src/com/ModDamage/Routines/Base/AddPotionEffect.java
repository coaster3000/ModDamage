package com.ModDamage.Routines.Base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Documentation.DocumentedRoutine;
import com.ModDamage.Routines.Routine;

@DocumentedRoutine(Description = "", Example = "", Name = "")
public class AddPotionEffect extends Routine
{	
	private final IDataProvider<LivingEntity> livingDP;
	private final PotionEffectType type;
	private final IDataProvider<Integer> duration, amplifier;
	
	protected AddPotionEffect(ScriptLine scriptLine, IDataProvider<LivingEntity> livingDP, PotionEffectType type,
			IDataProvider<Integer> duration, IDataProvider<Integer> amplifier)
	{
		super(scriptLine);
		this.livingDP = livingDP;
		this.type = type;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	@Override
	public void run(EventData data) throws BailException
	{
		LivingEntity entity = livingDP.get(data);
		if (entity == null) return;
		
		Integer dur = duration.get(data);
		Integer amp = amplifier.get(data);
		if (dur == null || amp == null) return;

		entity.addPotionEffect(new PotionEffect(type, dur, amp), true);
	}

	public static void register()
	{
		Routine.registerRoutine(
				Pattern.compile("(.+?)(?:effect)?\\.addpotioneffect\\.(\\w+)[\\., ](.+)", Pattern.CASE_INSENSITIVE),
				new RoutineFactory());
	}
	
	private static Pattern dotPattern = Pattern.compile("\\s*[\\.,]\\s*");

	protected static class RoutineFactory extends Routine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher matcher, ScriptLine scriptLine, EventInfo info)
		{ 
			IDataProvider<LivingEntity> livingDP = DataProvider.parse(info, LivingEntity.class, matcher.group(1));
			if (livingDP == null) return null; 
			
			PotionEffectType type = PotionEffectType.getByName(matcher.group(2).toUpperCase());
			if (type == null)
			{
				ModDamageLogger.error("Unknown potion effect type '"+matcher.group(2)+"'");
				return null;
			}
			
			StringMatcher sm = new StringMatcher(matcher.group(3));
			IDataProvider<Integer> duration = DataProvider.parse(info, Integer.class, sm.spawn()); if (duration == null) return null;
			if (!sm.matchesFront(dotPattern)) return null;
			IDataProvider<Integer> amplifier = DataProvider.parse(info, Integer.class, sm.spawn()); if (amplifier == null) return null;
			if (!sm.isEmpty()) return null;
			
			ModDamageLogger.info("AddPotionEffect: to " + livingDP + ", " + type.getName() + ", " + duration + ", " + amplifier);
			return new RoutineBuilder(new AddPotionEffect(scriptLine, livingDP, type, duration, amplifier));
		}
	}
}
