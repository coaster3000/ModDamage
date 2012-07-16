package com.ModDamage.Conditionals;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.ModDamage.StringMatcher;
import com.ModDamage.Alias.EnchantmentAliaser;
import com.ModDamage.EventInfo.DataProvider;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.EventInfo.IDataProvider;

public class PlayerHasEnchantment extends Conditional<Player>
{
	public static final Pattern pattern = Pattern.compile("\\.hasenchantment\\.(\\w+)", Pattern.CASE_INSENSITIVE);
	
	protected final Collection<Enchantment> enchantments;
	
	public PlayerHasEnchantment(IDataProvider<?> playerDP, Collection<Enchantment> enchantments)
	{
		super(Player.class, playerDP);
		this.enchantments = enchantments;
	}

	@Override
	public Boolean get(Player player, EventData data)
	{
		for(Enchantment enchantment : enchantments)
			if(player.getItemInHand().containsEnchantment(enchantment))
				return true;
		return false;
	}
	
	public static void register()
	{
		DataProvider.register(Boolean.class, Entity.class, pattern, new IDataParser<Boolean>()
			{
				@Override
				public IDataProvider<Boolean> parse(EventInfo info, IDataProvider<?> playerDP, Matcher m, StringMatcher sm)
				{
					Collection<Enchantment> enchantments = EnchantmentAliaser.match(m.group(1));
					if(enchantments.isEmpty())	return null;
					
					return new PlayerHasEnchantment(playerDP, enchantments);
				}
			});
	}
}
