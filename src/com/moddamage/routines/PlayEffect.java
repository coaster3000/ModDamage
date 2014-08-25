package com.moddamage.routines;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Effect;
import org.bukkit.Location;

import com.moddamage.LogUtil;
import com.moddamage.backend.BailException;
import com.moddamage.backend.ScriptLine;
import com.moddamage.eventinfo.EventData;
import com.moddamage.eventinfo.EventInfo;
import com.moddamage.expressions.LiteralNumber;
import com.moddamage.parsing.DataProvider;
import com.moddamage.parsing.IDataProvider;


public class PlayEffect extends Routine
{
	public static class EffectType {
		public static Map<String, EffectType> effects = new HashMap<String, EffectType>();

		private final Effect effect;

		public EffectType(Effect effect) {
			this.effect = effect;
		}


		public static void registerTypes() {
			effects.clear();
			for (Effect e : Effect.values())
				effects.put(e.name(), new EffectType(e));
		}

		public EffectType[] values() {
			return effects.values().toArray(new EffectType[effects.size()]);
		}

		public static EffectType valueOf(String value) {
			if (effects.containsKey(value)) return effects.get(value);
			throw new IllegalArgumentException("No such value '" + value + "'!");
		}

		public Integer dataForExtra(String extra) {
			return null;
		}
	}

	private final IDataProvider<Location> locDP;
	private final EffectType effectType;
	private final IDataProvider<? extends Number> effectData;
	private final IDataProvider<Integer> radius;

	protected PlayEffect(ScriptLine scriptLine, IDataProvider<Location> locDP, EffectType effectType, IDataProvider<? extends Number> data, IDataProvider<Integer> radius)
	{
		super(scriptLine);
		this.locDP = locDP;
		this.effectType = effectType;
		this.effectData = data;
		this.radius = radius;
	}

	@Override
	public void run(EventData data) throws BailException
	{
		Location loc = locDP.get(data);
		if (loc == null) return;

		Number eData = effectData.get(data);
		if (eData == null) return;

		if (radius == null)
			loc.getWorld().playEffect(loc, effectType.effect, eData.intValue());
		else {
			Number rad = radius.get(data);
			if (rad == null) return;

			loc.getWorld().playEffect(loc, effectType.effect, eData.intValue(), rad.intValue());
		}
	}

	public static void register()
	{
		EffectType.registerTypes();
		Routine.registerRoutine(Pattern.compile("(.+?)\\.playeffect\\.(\\w+)(?:\\.([^.]+))?(?:\\.radius\\.(.+))?", Pattern.CASE_INSENSITIVE), new RoutineFactory());
	}

	protected static class RoutineFactory extends Routine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher matcher, ScriptLine scriptLine, EventInfo info)
		{
			IDataProvider<Location> locDP = DataProvider.parse(scriptLine, info, Location.class, matcher.group(1));
			if (locDP == null) return null;

			EffectType effectType;
			try
			{
				effectType = EffectType.valueOf(matcher.group(2).toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				LogUtil.error(scriptLine, "Bad effect type: \""+matcher.group(2)+"\"");
				return null;
			}
			IDataProvider<? extends Number> data;
			if (matcher.group(3) == null)
				data = new LiteralNumber(0);
			else {
				Integer ndata = effectType.dataForExtra(matcher.group(3));
				if (ndata == null)
				{
					data = DataProvider.parse(scriptLine, info, Integer.class, matcher.group(3));
					
					if (data == null)
					{
						LogUtil.error(scriptLine, "Bad extra data: \""+matcher.group(3)+"\" for " + effectType + " effect.");
						return null;
					}
				}
				else
					data = new LiteralNumber(ndata);
			}
			
			IDataProvider<Integer> radius = null;
			if (matcher.group(4) != null)
			{
				radius = DataProvider.parse(scriptLine, info, Integer.class, matcher.group(4));
				if (radius == null)
				{
					LogUtil.error(scriptLine, "Unable to match expression: \""+matcher.group(4)+"\"");
					return null;
				}
			}
			
			LogUtil.info(scriptLine, "PlayEffect: " + locDP + " " + effectType + " " + data + (radius != null? " " + radius : ""));
			return new RoutineBuilder(new PlayEffect(scriptLine, locDP, effectType, data, radius));
		}
	}
}
