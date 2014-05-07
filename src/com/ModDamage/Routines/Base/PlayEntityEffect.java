package com.ModDamage.Routines.Base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine;

public class PlayEntityEffect extends Routine
{
	private final IDataProvider<Entity> entityDP;
	private final EntityEffect entityEffect;
	protected PlayEntityEffect(ScriptLine scriptLine, IDataProvider<Entity> entityDP, EntityEffect entityEffect)
	{
		super(scriptLine);
		this.entityDP = entityDP;
		this.entityEffect = entityEffect;
	}

	@Override
	public void run(EventData data) throws BailException
	{
		Entity entity = entityDP.get(data);
		if (entity == null) return;

		entity.playEffect(entityEffect);
	}

	public static void register()
	{
		Routine.registerRoutine(Pattern.compile("([a-z]+)\\.playentityeffect\\.(\\w+)", Pattern.CASE_INSENSITIVE), new RoutineFactory());
	}

	protected static class RoutineFactory extends Routine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher matcher, ScriptLine scriptLine, EventInfo info)
		{
			IDataProvider<Entity> entityDP = DataProvider.parse(info, Entity.class, matcher.group(1));
			if (entityDP == null) return null;
			
			EntityEffect effectType;
			try
			{
				effectType = EntityEffect.valueOf(matcher.group(2).toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				ModDamageLogger.error("Bad effect type: \""+matcher.group(2)+"\"");
				return null;
			}
			
			ModDamageLogger.info("PlayEntityEffect: " + entityDP + " " + effectType);
			return new RoutineBuilder(new PlayEntityEffect(scriptLine, entityDP, effectType));
		}
	}
}
