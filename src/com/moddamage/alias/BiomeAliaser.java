package com.moddamage.alias;

import java.util.Collection;

import org.bukkit.block.Biome;

import com.moddamage.alias.Aliaser.CollectionAliaser;
import com.moddamage.backend.ScriptLine;

public class BiomeAliaser extends CollectionAliaser<Biome> 
{
	public static BiomeAliaser aliaser = new BiomeAliaser();
	public static Collection<Biome> match(ScriptLine scriptLine) { return aliaser.matchAlias(scriptLine); }
	public static Collection<Biome> match(ScriptLine scriptLine, String key) { return aliaser.matchAlias(scriptLine, key); }
	
	public BiomeAliaser(){ super(AliasManager.Biome.name()); }
	@Override
	protected Biome matchNonAlias(String key)
	{
		for(Biome biome : Biome.values())
			if(key.equalsIgnoreCase(biome.name()))
				return biome;
		return null;
	}
	//@Override
	//protected String getObjectName(Biome object){ return object.name(); }
}
