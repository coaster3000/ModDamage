package com.ModDamage.External.Towny;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.ModDamage.ModDamageLogger;
import com.palmergames.bukkit.towny.Towny;

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
	}
}
