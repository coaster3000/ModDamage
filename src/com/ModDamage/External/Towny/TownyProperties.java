package com.ModDamage.External.Towny;

import org.bukkit.World;

import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.Property.Properties;
import com.ModDamage.Backend.Configuration.Parsing.Property.Property;
import com.ModDamage.Backend.Configuration.Parsing.Property.PropertyTransformer;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;

public class TownyProperties {
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
		
		Properties.register("isUsingTowny", TownyWorld.class, "isUsingTowny", "setUsingTowny");
		Properties.register("isClaimable", TownyWorld.class, "isClaimable", "setClaimable");
	}
}
