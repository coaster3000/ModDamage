package com.ModDamage.Routines.Nested;

import com.ModDamage.ModDamage;
import com.ModDamage.ModDamageLogger;
import com.ModDamage.ModDamageLogger.OutputPreset;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.ScriptLineHandler;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.External.mcMMO.ModifySkill;
import com.ModDamage.Routines.Routine;
import com.ModDamage.Routines.RoutineList;
import com.ModDamage.Routines.Base.Explode;

public abstract class NestedRoutine extends Routine
{
//	public static LinkedHashMap<Pattern, RoutineFactory> registeredNestedRoutineList = new LinkedHashMap<Pattern, RoutineFactory>();
	
	public final RoutineList routines = new RoutineList();

	protected NestedRoutine(ScriptLine scriptLine){ super(scriptLine); }

	public static void registerVanillaRoutineList()
	{
//		registeredNestedRoutineList.clear();
		If.register();
		While.register();
		With.register();
		Foreach.register();
		For.register();
		Delay.register();
		Knockback.register();
		SwitchRoutine.register();
		Spawn.register();
		EntityItemAction.registerNested();
		DropItem.registerNested();
		Explode.register();
		LaunchProjectile.register();
		Nearby.register();
		ModifySkill.register();
		Command.registerNested();
		PlayerChat.registerNested();
		LogMessage.register();
	}

	protected static class NestedRoutineBuilder extends RoutineBuilder
	{
		RoutineList RoutineList;
		EventInfo info;
		public NestedRoutineBuilder(Routine routine, RoutineList RoutineList, EventInfo info) {
			super(routine);
			this.RoutineList = RoutineList;
			this.info = info;
		}
		@Override
		public ScriptLineHandler getScriptLineHandler()
		{
			return RoutineList.getLineHandler(info);
		}
	}

	public static void paddedLogRecord(OutputPreset preset, String message)
	{		
		ModDamageLogger.console_only("");
		ModDamage.getConfiguration().getLog().addToLogRecord(preset, message);
		ModDamageLogger.console_only("");
	}
}
