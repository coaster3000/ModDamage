package com.ModDamage.Backend.Configuration.Alias;

import java.util.Collection;

import com.ModDamage.Backend.Configuration.Alias.Aliaser.CollectionAliaser;
import com.ModDamage.Backend.Minecraft.Matchables.EntityType;

public class TypeAliaser extends CollectionAliaser<EntityType> 
{
	public static TypeAliaser aliaser = new TypeAliaser();
	public static Collection<EntityType> match(String string) { return aliaser.matchAlias(string); }
	
	public TypeAliaser() {super(AliasManager.Type.name()); }

	@Override
	protected EntityType matchNonAlias(String key){ return EntityType.getElementNamed(key); }

	//@Override
	//protected String getObjectName(ModDamageElement object){ return object.name(); }
}