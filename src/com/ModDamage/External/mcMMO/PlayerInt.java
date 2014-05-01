package com.ModDamage.External.mcMMO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.ModDamage.StringMatcher;
import com.ModDamage.Utils;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Expressions.SettableIntegerExpression;
import com.gmail.nossr50.api.ExperienceAPI;

public class PlayerInt extends SettableIntegerExpression<Player>
{
	public static void register()
	{
		DataProvider.register(Integer.class, Player.class, 
				Pattern.compile("_("+Utils.joinBy("|", PlayerIntProperty.values()) +")", Pattern.CASE_INSENSITIVE),
				new IDataParser<Integer, Player>()
				{
					@Override
					public IDataProvider<Integer> parse(EventInfo info, IDataProvider<Player> playerDP, Matcher m, StringMatcher sm)
					{
						return sm.acceptIf(new PlayerInt(
								playerDP,
								PlayerIntProperty.valueOf(m.group(1).toUpperCase())));
					}
				});
	}
	
	protected final PlayerIntProperty propertyMatch;
	public enum PlayerIntProperty
	{
		POWERLEVEL(false)
		{
			@Override
			public int getValue(Player player) 
			{
				return ExperienceAPI.getPowerLevel(player);
			}
		};
		
		public boolean settable = false;
		private PlayerIntProperty(){}
		private PlayerIntProperty(boolean settable)
		{
			this.settable = settable;
		}
		
		abstract public int getValue(Player player);
		
		public void setValue(Player player, int value) {}
	}
	
	PlayerInt(IDataProvider<Player> playerDP, PlayerIntProperty propertyMatch)
	{
		super(Player.class, playerDP);
		this.propertyMatch = propertyMatch;
	}
	
	@Override
	public Integer myGet(Player player, EventData data) throws BailException
	{
		return propertyMatch.getValue(player);
	}
	
	@Override
	public void mySet(Player player, EventData data, Integer value)
	{
		propertyMatch.setValue(player, value);
	}
	
	@Override
	public boolean isSettable()
	{
		return propertyMatch.settable;
	}
	
	@Override
	public String toString()
	{
		return startDP + "_" + propertyMatch.name().toLowerCase();
	}

}
