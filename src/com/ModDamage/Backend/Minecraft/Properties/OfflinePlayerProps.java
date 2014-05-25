package com.ModDamage.Backend.Minecraft.Properties;

import org.bukkit.OfflinePlayer;

import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.Property.Properties;

public class OfflinePlayerProps
{
	public static void register()
	{
		Properties.register("name", OfflinePlayer.class, "getName");
		Properties.register("isOnline", OfflinePlayer.class, "isOnline");
		Properties.register("isBanned", OfflinePlayer.class, "isBanned", "setBanned");
		Properties.register("isWhitelisted", OfflinePlayer.class, "isWhitelisted", "setWhitelisted");
		Properties.register("isOp", OfflinePlayer.class, "isOp", "setOp");
		Properties.register("hasPlayedBefore", OfflinePlayer.class, "hasPlayedBefore");
		Properties.register("firstPlayed", OfflinePlayer.class, "getFirstPlayed");
		Properties.register("lastPlayed", OfflinePlayer.class, "getLastPlayed");
		
        DataProvider.registerTransformer(OfflinePlayer.class, "getPlayer");
	}
}