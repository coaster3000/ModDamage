package com.ModDamage.Backend.Configuration.Alias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.ModDamageConfigurationHandler.LoadState;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.ScriptLineHandler;
import com.ModDamage.Backend.Minecraft.Matchables.EntityType;

public class TypeNameAliaser extends Aliaser<EntityType, List<String>> 
{
	public static TypeNameAliaser aliaser = new TypeNameAliaser();
	//public static List<String> match(String string) { return aliaser.matchAlias(string); }
	
	protected HashMap<EntityType, List<String>> thisMap = new HashMap<EntityType, List<String>>();
	
	private static final Random random = new Random();

	TypeNameAliaser()
	{
		super(AliasManager.TypeName.name());
		for(EntityType element : EntityType.values())
			thisMap.put(element, new ArrayList<String>());
	}

	@Override
	public ScriptLineHandler handleLine(final ScriptLine nameLine, boolean hasChildren)
	{
		final EntityType entityType = EntityType.getElementNamed(nameLine.line);
		
		return new ScriptLineHandler() {
			List<String> names = new ArrayList<String>();
			boolean hasValue;
			
			@Override
			public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
			{
				String name = line.line;
				names.add(name);
				hasValue = true;
				return null;
			}
			
			@Override
			public void done()
			{
				if (!hasValue) {
					ModDamageLogger.error(nameLine, name+" alias "+nameLine.line+" has no names.");
					return;
				}
				thisMap.put(entityType, names);
			}
		};
	}

	@Override
	public void clear()
	{
		loadState = LoadState.NOT_LOADED;
		thisMap.clear();
	}
	
	public String toString(EntityType element)
	{
		List<String> names = thisMap.get(element);
		return names != null && !names.isEmpty()? names.get(random.nextInt(names.size())) : element.name();
	}
	
	public List<String> matchAlias(EntityType type){
		List<String> list = thisMap.get(type);
		if (list == null)
		{
			list = new ArrayList<String>();
			thisMap.put(type, list);
		}
		return list;
	}
	
	public List<String> matchAlias(String string){
		EntityType type = EntityType.getElementNamed(string);
		if (type == null) return null;
		return matchAlias(type);
	}
	
	@Deprecated
	public boolean completeAlias(String key, Object nestedContent){ return false; }
	@Deprecated
	protected EntityType matchNonAlias(String key){ return null; }
	@Deprecated
	protected String getObjectName(EntityType object){ return null; }
}