package com.moddamage.alias;

import java.util.Collection;

import org.bukkit.enchantments.Enchantment;

import com.moddamage.alias.Aliaser.CollectionAliaser;
import com.moddamage.backend.ScriptLine;

public class EnchantmentAliaser extends CollectionAliaser<Enchantment> 
{
	public static EnchantmentAliaser aliaser = new EnchantmentAliaser();
	public static Collection<Enchantment> match(ScriptLine scriptLine) { return aliaser.matchAlias(scriptLine); }
	public static Collection<Enchantment> match(ScriptLine scriptLine, String string) { return aliaser.matchAlias(scriptLine, string); }
	
	public EnchantmentAliaser(){ super(AliasManager.Enchantment.name()); }

	@Override
	protected Enchantment matchNonAlias(String key)
	{
		for(Enchantment enchantment : Enchantment.values())
			if(key.equalsIgnoreCase(enchantment.getName()))
				return enchantment;
		return null;
	}

	//@Override
	//protected String getObjectName(Enchantment object){ return object.getName(); }
}