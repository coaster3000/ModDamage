package com.ModDamage.Backend.Minecraft.Properties;

import com.ModDamage.Backend.Configuration.Parsing.Property.Properties;

public class MiscProps
{
	public static void register() {
		Properties.register("length", String.class, "length");
		
		Properties.register("class", Object.class, "getClass");
		Properties.register("name", Class.class, "getName");
		Properties.register("simplename", Class.class, "getSimpleName");
	}
}
