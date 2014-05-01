package com.ModDamage.Backend.Configuration.Alias;

import java.util.Collection;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.Configuration.Alias.Aliaser.CollectionAliaser;
import com.ModDamage.External.ExtensionManager;

public class RegionAliaser extends CollectionAliaser<String> 
{
	public static RegionAliaser aliaser = new RegionAliaser();
	public static Collection<String> match(String string) { return aliaser.matchAlias(string); }
	
	public RegionAliaser() { super(AliasManager.Region.name()); }

	@Override
	protected String matchNonAlias(String key)
	{
		if(!ExtensionManager.getAllRegions().contains(key))
			ModDamageLogger.warning_strong("Warning: region \"" + key + "\" does not currently exist.");
		return key;
	}

	//@Override
	//protected String getObjectName(String object){ return object; }
}
