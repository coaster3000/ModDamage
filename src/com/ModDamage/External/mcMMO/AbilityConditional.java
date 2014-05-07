package com.ModDamage.External.mcMMO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Conditionals.Conditional;
import com.gmail.nossr50.api.AbilityAPI;

public class AbilityConditional extends Conditional<Player>
{
	public enum Ability
	{
		Berserk
			{
				@Override public boolean isActivated(Player player)
				{
					return AbilityAPI.berserkEnabled(player);
				}
			},
		GigaDrillBreaker
			{
				@Override public boolean isActivated(Player player)
				{
					return AbilityAPI.gigaDrillBreakerEnabled(player);
				}
			},
		GreenTerra
			{
				@Override public boolean isActivated(Player player)
				{
					return AbilityAPI.greenTerraEnabled(player);
				}
			},
		SkullSplitter
			{
				@Override public boolean isActivated(Player player)
				{
					return AbilityAPI.skullSplitterEnabled(player);
				}
			},
		SerratedStrikes
			{
				@Override public boolean isActivated(Player player)
				{
					return AbilityAPI.serratedStrikesEnabled(player);
				}
			},
		SuperBreaker
			{
				@Override public boolean isActivated(Player player)
				{
					return AbilityAPI.superBreakerEnabled(player);
				}
			},
		TreeFeller
			{
				@Override public boolean isActivated(Player player)
				{
					return AbilityAPI.treeFellerEnabled(player);
				}
			};

		abstract public boolean isActivated(Player player);
	}
	
	public static final Pattern pattern = Pattern.compile("\\.hasactive\\.(\\w+)", Pattern.CASE_INSENSITIVE);
	
	protected final Ability ability;
	
	protected AbilityConditional(IDataProvider<Player> playerDP, Ability ability) 
	{
		super(Player.class, playerDP);
		this.ability = ability;
	}

	@Override
	public Boolean get(Player player, EventData data)
	{
		return ability.isActivated(player);
	}
	
	@Override
	public String toString()
	{
		return startDP + ".hasactive." + ability.name().toLowerCase();
	}
	
	
	public static void register()
	{
		DataProvider.register(Boolean.class, Player.class, pattern, new IDataParser<Boolean, Player>()
			{
				@Override
				public IDataProvider<Boolean> parse(EventInfo info, IDataProvider<Player> playerDP, Matcher m, StringMatcher sm)
				{
					Ability mcMMOability = null;
					for(Ability ability : Ability.values())
						if(m.group(1).equalsIgnoreCase(ability.name()))
							mcMMOability = ability;
					if(mcMMOability == null)
					{
						ModDamageLogger.error("Invalid McMMO ability \"" + m.group(3) + "\"");
						return null;
					}
					
					return new AbilityConditional(playerDP, mcMMOability);
				}
			});
	}
}
