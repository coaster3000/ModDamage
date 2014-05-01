package com.ModDamage.Backend.Minecraft.Variables.String;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Expressions.StringExpression;

public class EntityAsString extends StringExpression<Entity>
{
	public EntityAsString(IDataProvider<Entity> entityDP)
	{
		super(Entity.class, entityDP);
	}
	
	@Override
	public String get(Entity entity, EventData data)
	{
		if (entity instanceof Player)
			return ((Player) entity).getName();
		return entity.getType().getName();
	}
	
	public static void register()
	{
		DataProvider.registerTransformer(String.class, Entity.class, new IDataTransformer<String, Entity>()
			{
				@Override
				public IDataProvider<String> transform(EventInfo info, IDataProvider<Entity> entityDP)
				{
					return new EntityAsString(entityDP);
				}
			});
	}

	@Override
	public String toString()
	{
		return startDP.toString();
	}
}
