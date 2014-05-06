package com.ModDamage.External.Towny;

import java.util.regex.Pattern;

import com.ModDamage.Backend.BailException;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.Parsing.DataProvider;
import com.ModDamage.Parsing.FunctionParser;
import com.ModDamage.Parsing.IDataProvider;

public class TownySupport {
	public static void register() {
		
		DataProvider.register(Nation.class, Object.class, Pattern.compile("nation"), new FunctionParser<Nation, Object>(String.class) {
			@SuppressWarnings("rawtypes")
			protected IDataProvider<Nation> makeProvider(EventInfo info, IDataProvider<Object> startDP, IDataProvider[] arguments) {
				@SuppressWarnings("unchecked")
				final IDataProvider<String> nameDP = (IDataProvider<String>) arguments[0];
				
				return new IDataProvider<Nation>() {
						public Nation get(EventData data) throws BailException {
							String name = nameDP.get(data);
							if (name == null) return null;
							
							return new Nation(name);
						}
						
						public Class<? extends Nation> provides() {
							return Nation.class;
						}
					};
			}
		});
		
		DataProvider.register(Town.class, Object.class, Pattern.compile("town"), new FunctionParser<Town, Object>(String.class) {
			@SuppressWarnings("rawtypes")
			protected IDataProvider<Town> makeProvider(EventInfo info, IDataProvider<Object> startDP, IDataProvider[] arguments) {
				@SuppressWarnings("unchecked")
				final IDataProvider<String> nameDP = (IDataProvider<String>) arguments[0];
				
				return new IDataProvider<Town>() {
						public Town get(EventData data) throws BailException {
							String name = nameDP.get(data);
							if (name == null) return null;
							
							return new Town(name);
						}
						
						public Class<? extends Town> provides() {
							return Town.class;
						}
					};
			}
		});
		
		
	}
}
