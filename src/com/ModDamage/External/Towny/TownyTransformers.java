package com.ModDamage.External.Towny;

import java.util.regex.Pattern;

import org.bukkit.World;

import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.FunctionParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Configuration.Parsing.Property.Property;
import com.ModDamage.Backend.Configuration.Parsing.Property.PropertyTransformer;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;

public class TownyTransformers {

	public static void register() {
		DataProvider.registerTransformer(TownyWorld.class, World.class, new PropertyTransformer<TownyWorld, World>(new Property<TownyWorld, World>("@transformer", TownyWorld.class, World.class) {
			public TownyWorld get(World start, EventData data) {
				try {
					return TownyUniverse.getDataSource().getWorld(start.getName());
				} catch (NotRegisteredException e) {
					return null;
				}
			}
		}));
	}

}
