package com.ModDamage.Backend.Minecraft.Variables.String;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.ModDamage.StringMatcher;
import com.ModDamage.Utils;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.ArmorSet;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.External.ExtensionManager;
import com.ModDamage.Routines.Expressions.StringExpression;

public class PlayerString extends StringExpression<Player>
{
	private static Pattern pattern = Pattern.compile("_("+ Utils.joinBy("|", PlayerStringProperty.values()) +")", Pattern.CASE_INSENSITIVE);
	
	public enum PlayerStringProperty
	{
		ARMORSET
		{
			@Override protected String getString(Player player)
			{
				return new ArmorSet(player).toString();
			}
		},
		GROUP
		{
			@Override protected String getString(Player player)
			{
				return ExtensionManager.getGroupsManager().getGroups(player).toString();
			}
		},
		WIELDING
		{
			@Override protected String getString(Player player)
			{
				return player.getItemInHand().getType().name();
			}
		},
		ENCHANTMENTS
		{
			@Override protected String getString(Player player)
			{
				return HELD_ENCHANTMENTS.getString(player) + " " + ARMOR_ENCHANTMENTS.getString(player);
			}
		},
		HELD_ENCHANTMENTS
		{
			@Override protected String getString(Player player)
			{
				return player.getItemInHand().getEnchantments().toString();
			}
		},
		ARMOR_ENCHANTMENTS
		{
			@Override protected String getString(Player player)
			{
				return HELMET_ENCHANTMENTS.getString(player) + " " + 
					   CHESTPLATE_ENCHANTMENTS.getString(player) + " " + 
					   LEGGINGS_ENCHANTMENTS.getString(player) + " " + 
					   BOOTS_ENCHANTMENTS.getString(player);
			}
		},
		HELMET_ENCHANTMENTS
		{
			@Override protected String getString(Player player)
			{
				return player.getInventory().getHelmet().getEnchantments().toString();
			}
		},
		CHESTPLATE_ENCHANTMENTS
		{
			@Override protected String getString(Player player)
			{
				return player.getInventory().getChestplate().getEnchantments().toString();
			}
		},
		LEGGINGS_ENCHANTMENTS
		{
			@Override protected String getString(Player player)
			{
				return player.getInventory().getLeggings().getEnchantments().toString();
			}
		},
		BOOTS_ENCHANTMENTS
		{
			@Override protected String getString(Player player)
			{
				return player.getInventory().getBoots().getEnchantments().toString();
			}
		};
		
		abstract protected String getString(Player player);
	}
	

	private final PlayerStringProperty propertyMatch;
	
	public PlayerString(IDataProvider<Player> playerDP, PlayerStringProperty propertyMatch)
	{
		super(Player.class, playerDP);
		this.propertyMatch = propertyMatch;
	}
	
	@Override
	public String get(Player player, EventData data)
	{
		return propertyMatch.getString(player);
	}
	
	public static void register()
	{
		DataProvider.register(String.class, Player.class, pattern, new IDataParser<String, Player>()
			{
				@Override
				public IDataProvider<String> parse(EventInfo info, IDataProvider<Player> playerDP, Matcher m, StringMatcher sm)
				{
					sm.accept();
					return new PlayerString(playerDP, PlayerStringProperty.valueOf(m.group(1).toUpperCase()));
				}
			});
	}

	@Override
	public String toString()
	{
		return startDP + "_" + propertyMatch.name().toLowerCase();
	}
}
