package com.ModDamage.External.Towny;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.FunctionParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TownySupport {
	public static Towny towny;
	
	public static void register() {
		Plugin p = Bukkit.getPluginManager().getPlugin("Towny");
		if (p instanceof Towny)
			towny = (Towny) p;
		else {
			towny = null;
			ModDamageLogger.info("Towny not found: Plugin not on server.");
			return;
		}
		
		DataProvider.register(Town.class, Object.class, Pattern.compile("town", Pattern.CASE_INSENSITIVE), new FunctionParser<Town, Object>(String.class) {

			@Override
			protected IDataProvider<Town> makeProvider(EventInfo info, IDataProvider<Object> startDP, IDataProvider[] arguments) {
				final IDataProvider<String> nameDP = (IDataProvider<String>) arguments[0];
				
				return new IDataProvider<Town>() {
					
					@Override
					public Class<? extends Town> provides() {
						return Town.class;
					}
					
					@Override
					public Town get(EventData data) throws BailException {
						try {
							return TownyUniverse.getDataSource().getTown(nameDP.get(data));
						} catch (NotRegisteredException e) {
							return null;
						}
					}
				};
			}
		});
		
		DataProvider.register(Nation.class, Object.class, Pattern.compile("nation", Pattern.CASE_INSENSITIVE), new FunctionParser<Nation, Object>(String.class) {

			@Override
			protected IDataProvider<Nation> makeProvider(EventInfo info, IDataProvider<Object> startDP, IDataProvider[] arguments) {
				final IDataProvider<String> nameDP = (IDataProvider<String>) arguments[0];
				
				return new IDataProvider<Nation>() {
					
					@Override
					public Class<? extends Nation> provides() {
						return Nation.class;
					}
					
					@Override
					public Nation get(EventData data) throws BailException {
						try {
							return TownyUniverse.getDataSource().getNation(nameDP.get(data));
						} catch (NotRegisteredException e) {
							return null;
						}
					}
				};
				
			}
		});
		
		TownyTransformers.register();
		TownyProperties.register();
		TownyConditionals.register();
	}
}
