package com.ModDamage.External.Towny;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.IDataParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Conditionals.Conditional;
import com.palmergames.bukkit.towny.object.TownyWorld;

public class TownyConditionals {
	public static void register() {
		if (TownySupport.towny == null)
			return;
		
		///////////////////////////////// Worlds //////////////////////////////////
		
		Conditional.register(Boolean.class, TownyWorld.class, Pattern.compile("\\.hasTowns", Pattern.CASE_INSENSITIVE), new IDataParser<Boolean, TownyWorld>() {
			@Override
			public IDataProvider<Boolean> parse(EventInfo info, IDataProvider<TownyWorld> startDP, Matcher m, StringMatcher sm) {
				return new Conditional<TownyWorld>(TownyWorld.class, startDP) {
					
					@Override
					public Boolean get(TownyWorld start, EventData data) throws BailException {
						return start.hasTowns();
					}
					
					@Override
					public String toString() {
						return this.startDP + ".hasTowns";
					}
				};
			}
		});
	}
}
