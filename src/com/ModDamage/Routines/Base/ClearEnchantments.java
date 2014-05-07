package com.ModDamage.Routines.Base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.EnchantmentsRef;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Routine;

public class ClearEnchantments extends Routine
{
	private final IDataProvider<EnchantmentsRef> enchantmentsDP;
	
	protected ClearEnchantments(ScriptLine scriptLine, IDataProvider<EnchantmentsRef> enchantmentsDP)
	{
		super(scriptLine);
		this.enchantmentsDP = enchantmentsDP;
	}


	@Override
	public void run(EventData data) throws BailException
	{
		EnchantmentsRef ench = enchantmentsDP.get(data);
		if (ench == null) return;
		
		ench.map.clear();
	}
	
	public static void register()
	{
		Routine.registerRoutine(Pattern.compile("clearenchant(?:ment)?s", Pattern.CASE_INSENSITIVE), new RoutineFactory());
	}
	
	protected static class RoutineFactory extends Routine.RoutineFactory
	{
		@Override
		public IRoutineBuilder getNew(Matcher matcher, ScriptLine scriptLine, EventInfo info)
		{
			IDataProvider<EnchantmentsRef> enchantmentsDP = DataProvider.parse(info, EnchantmentsRef.class, "enchantments");
			if(enchantmentsDP == null) return null;
			
			ModDamageLogger.info("Clear Enchantments");
			return new RoutineBuilder(new ClearEnchantments(scriptLine, enchantmentsDP));
		}
	}
}
