package com.ModDamage.Routines.Conditionals;

import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.External.mcMMO.AbilityConditional;
import com.ModDamage.Routines.Expressions.Function.PlayerCanSeeFunction;
import com.ModDamage.Routines.Expressions.Function.PlayerNamedFunction;
import com.ModDamage.Routines.Expressions.Function.WorldNamedFunction;


public abstract class Conditional<S> extends DataProvider<Boolean, S>
{
	public static void register()
	{
		Chance.register();
		Comparison.register();
		Equality.register();
		CompoundConditional.register();
		InvertBoolean.register();
		//Entity
		LocationBiome.register();
		EntityBlockStatus.register();
		EntityHasPotionEffect.register();
		LocationRegion.register();
		EntityStatus.register();
		IsTagged.register();
		PlayerWearing.register();
		PlayerWielding.register();
		//Player
		PlayerHasEnchantment.register();
		PlayerHasItem.register();
		PlayerHasPermission.register();
		PlayerInGroup.register();
        PlayerNamed.register();
		PlayerStatus.register();
        PlayerGameMode.register();
        PlayerCanSee.register();
        PlayerCanSeeFunction.register();
		PlayerNamedFunction.register();
		//Server
		ServerOnlineMode.register();
		//World
		WorldEnvironment.register();
		WorldNamed.register();
		WorldStatus.register();
		WorldNamedFunction.register();
		//Other
		EnumEquals.register();
		ItemMatches.register();
		LivingEntityStatus.register();
		StringConditionals.register();
		StringMatches.register();
		
		//mcMMO
		AbilityConditional.register();
	}
	
	
	protected Conditional(Class<S> wantStart, IDataProvider<S> startDP)
	{
		super(wantStart, startDP);
		defaultValue = false;
	}

	@Override
	public Class<Boolean> provides() { return Boolean.class; }
	
	public abstract String toString();
}