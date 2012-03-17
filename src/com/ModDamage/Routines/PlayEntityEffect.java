package com.ModDamage.Routines;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.Backend.BailException;
import com.ModDamage.EventInfo.DataRef;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;

public class PlayEntityEffect extends Routine
{
	private final DataRef<Entity> entityRef;
	private final EntityEffect entityEffect;
	protected PlayEntityEffect(String configString, DataRef<Entity> entityRef, EntityEffect entityEffect)
	{
		super(configString);
		this.entityRef = entityRef;
		this.entityEffect = entityEffect;
	}

	@Override
	public void run(EventData data) throws BailException
	{
		Entity entity = entityRef.get(data);
		if (entity == null) return;

		entity.playEffect(entityEffect);
	}

	public static void register()
	{
		Routine.registerRoutine(Pattern.compile("([a-z]+).playentityeffect.(\\w+)", Pattern.CASE_INSENSITIVE), new RoutineBuilder());
	}

	protected static class RoutineBuilder extends Routine.RoutineBuilder
	{
		@Override
		public PlayEntityEffect getNew(Matcher matcher, EventInfo info)
		{ 
			DataRef<Entity> entityRef = info.get(Entity.class, matcher.group(1).toLowerCase());
			if (entityRef == null) return null;
			
			EntityEffect effectType;
			try
			{
				effectType = EntityEffect.valueOf(matcher.group(2).toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				ModDamage.addToLogRecord(OutputPreset.FAILURE, "Bad effect type: \""+matcher.group(2)+"\"");
				return null;
			}
			
			ModDamage.addToLogRecord(OutputPreset.INFO, "PlayEntityEffect: " + entityRef + " " + effectType);
			return new PlayEntityEffect(matcher.group(), entityRef, effectType);
		}
	}
}
