package com.ModDamage.Backend.Minecraft.Properties;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.ModDamage.Backend.Configuration.Parsing.Property.Properties;

public class LocationProps
{
	public static void register()
	{
		
		//Locations
        Properties.register("x", Location.class, "getX");
        Properties.register("y", Location.class, "getY");
        Properties.register("z", Location.class, "getZ");
        Properties.register("yaw", Location.class, "getYaw");
        Properties.register("pitch", Location.class, "getPitch");

        Properties.register("block", Location.class, "getBlock");
        Properties.register("world", Location.class, "getWorld");
        
        //Vectors
        Properties.register("x", Vector.class, "getX", "setX");
        Properties.register("y", Vector.class, "getY", "setY");
        Properties.register("z", Vector.class, "getZ", "setZ");
	}
}