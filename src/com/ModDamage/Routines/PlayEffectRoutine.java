package com.ModDamage.Routines;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Effect;
import org.bukkit.entity.Entity;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.Utils;
import com.ModDamage.Backend.EntityReference;
import com.ModDamage.Backend.TargetEventInfo;
import com.ModDamage.Backend.Matching.DynamicInteger;
import com.ModDamage.Backend.Matching.DynamicIntegers.ConstantInteger;

public class PlayEffectRoutine extends Routine
{
	enum EffectType {
		BOW_FIRE(Effect.BOW_FIRE),
		CLICK1(Effect.CLICK1),
		CLICK2(Effect.CLICK2),
	    DOOR_TOGGLE(Effect.DOOR_TOGGLE),
	    EXTINGUISH(Effect.EXTINGUISH),
	    RECORD_PLAY(Effect.RECORD_PLAY),
	    GHAST_SHRIEK(Effect.GHAST_SHRIEK),
	    GHAST_SHOOT(Effect.GHAST_SHOOT),
	    BLAZE_SHOOT(Effect.BLAZE_SHOOT),
	    SMOKE(Effect.SMOKE),
	    BLOCK_BREAK(Effect.STEP_SOUND),
	    POTION_BREAK(Effect.POTION_BREAK),
	    ENDER_SIGNAL(Effect.ENDER_SIGNAL),
	    MOBSPAWNER_FLAMES(Effect.MOBSPAWNER_FLAMES);
		
		public final static String regexString = Utils.joinBy("|", values());
		
		final Effect effect;
		private EffectType(Effect effect) { this.effect = effect; }
		public Integer dataForExtra(String extra) { return null; }
	}
	
	final EntityReference entityReference;
	final EffectType effectType;
	final DynamicInteger data;
	final DynamicInteger radius;
	protected PlayEffectRoutine(String configString, EntityReference entityReference, EffectType effectType, DynamicInteger data, DynamicInteger radius)
	{
		super(configString);
		this.entityReference = entityReference;
		this.effectType = effectType;
		this.data = data;
		this.radius = radius;
	}

	@Override
	public void run(TargetEventInfo eventInfo)
	{
		Entity entity = entityReference.getEntity(eventInfo);
		if (entity == null) return;
		
		if (radius == null)
			entity.getWorld().playEffect(entity.getLocation(), effectType.effect, data.getValue(eventInfo));
		else
			entity.getWorld().playEffect(entity.getLocation(), effectType.effect, data.getValue(eventInfo), radius.getValue(eventInfo));
	}

	public static void register()
	{
		Routine.registerRoutine(Pattern.compile("("+EntityReference.regexString+").playeffect.("+EffectType.regexString+")(?:\\.([^.]+))?(?:\\.radius\\.(.+))?", Pattern.CASE_INSENSITIVE), new RoutineBuilder());
	}

	protected static class RoutineBuilder extends Routine.RoutineBuilder
	{
		@Override
		public PlayEffectRoutine getNew(Matcher matcher)
		{ 
			EntityReference entityReference = EntityReference.valueOf(matcher.group(1).toUpperCase());
			EffectType effectType = EffectType.valueOf(matcher.group(2).toUpperCase());
			DynamicInteger data;
			if (matcher.group(3) == null)
				data = new ConstantInteger(0);
			else {
				Integer ndata = effectType.dataForExtra(matcher.group(3));
				if (ndata == null)
				{
					data = DynamicInteger.getNew(matcher.group(3));
					
					if (data == null)
					{
						ModDamage.addToLogRecord(OutputPreset.FAILURE, "Bad extra data: \""+matcher.group(3)+"\" for " + effectType + " effect.");
						return null;
					}
				}
				else
					data = new ConstantInteger(ndata);
			}
			
			DynamicInteger radius = null;
			if (matcher.group(4) != null)
			{
				radius = DynamicInteger.getNew(matcher.group(4));
				if (radius == null)
				{
					ModDamage.addToLogRecord(OutputPreset.FAILURE, "Unable to match expression: \""+matcher.group(4)+"\"");
					return null;
				}
			}
			
			ModDamage.addToLogRecord(OutputPreset.INFO, "PlayEffect: " + entityReference + " " + effectType + " " + data + (radius != null? " " + radius : ""));
			return new PlayEffectRoutine(matcher.group(), entityReference, effectType, data, radius);
		}
	}
}