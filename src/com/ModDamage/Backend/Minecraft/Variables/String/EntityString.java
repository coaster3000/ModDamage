package com.ModDamage.Backend.Minecraft.Variables.String;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Entity;

import com.ModDamage.StringMatcher;
import com.ModDamage.TagManager;
import com.ModDamage.Utils;
import com.ModDamage.Backend.Configuration.Alias.TypeNameAliaser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataParser;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Matchables.EntityType;
import com.ModDamage.External.ExtensionManager;
import com.ModDamage.Routines.Expressions.StringExpression;

public class EntityString extends StringExpression<Entity>
{
	private static Pattern pattern = Pattern.compile("_("+ Utils.joinBy("|", EntityStringProperty.values()) +")", Pattern.CASE_INSENSITIVE);
	
	public enum EntityStringProperty
	{
		REGIONS
		{
			@Override protected String getString(Entity entity)
			{
				return ExtensionManager.getRegions(entity.getLocation()).toString();
			}
		},
		TAGS
		{
			@Override protected String getString(Entity entity)
			{
				return TagManager.getInstance().numTags.onEntity.getTags(entity).toString();
			}
		},
		TYPENAME
		{
			@Override protected String getString(Entity entity)
			{
				return TypeNameAliaser.aliaser.toString(EntityType.get(entity));
			}
		},
		UID
		{
			@Override
			protected String getString(Entity entity) {
				return entity.getUniqueId().toString();
			}
		};
		
		abstract protected String getString(Entity entity);
	}
	

	private final EntityStringProperty propertyMatch;
	
	public EntityString(IDataProvider<Entity> entityDP, EntityStringProperty propertyMatch)
	{
		super(Entity.class, entityDP);
		this.propertyMatch = propertyMatch;
	}
	
	@Override
	public String get(Entity entity, EventData data)
	{
		return propertyMatch.getString(entity);
	}
	
	public static void register()
	{
		DataProvider.register(String.class, Entity.class, pattern, new IDataParser<String, Entity>()
			{
				@Override
				public IDataProvider<String> parse(EventInfo info, IDataProvider<Entity> entityDP, Matcher m, StringMatcher sm)
				{
					return new EntityString(entityDP, EntityStringProperty.valueOf(m.group(1).toUpperCase()));
				}
			});
	}

	@Override
	public String toString()
	{
		return startDP + "_" + propertyMatch.name().toLowerCase();
	}
}
