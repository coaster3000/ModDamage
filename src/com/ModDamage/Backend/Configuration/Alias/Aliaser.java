package com.ModDamage.Backend.Configuration.Alias;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.ModDamagePluginConfiguration.LoadState;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.ScriptLineHandler;

abstract public class Aliaser<Type, StoredInfoClass> implements com.ModDamage.Backend.Configuration.ScriptLineHandler
{
	private Map<String, StoredInfoClass> thisMap = new HashMap<String, StoredInfoClass>();
	protected final String name;
	protected LoadState loadState = LoadState.NOT_LOADED;
	
	Aliaser(String name){ this.name = name; }
	
	/*public StoredInfoClass matchAlias(String key)
	{
		if(thisMap.containsKey(key))
			return thisMap.get(key);
		return null;
		/*
		Type value = matchNonAlias(key);
		if(value != null) return getNewStorageClass(value);
		ModDamageLogger.error("No matching " + name + " alias or value \"" + key + "\"");
		return getDefaultValue();* /
	}*/

//	abstract public boolean completeAlias(String key, Object nestedContent);

	//abstract protected Type matchNonAlias(String key);
	
	//abstract protected String getObjectName(Type object);
	
	public String getName(){ return name; }

	public LoadState getLoadState(){ return this.loadState; }

	public boolean hasAlias(String key)
	{
		return thisMap.containsKey(key);
	}
	
	public StoredInfoClass getAlias(String key)
	{
		return thisMap.get(key);
	}
	
	public void putAlias(String key, StoredInfoClass obj)
	{
		thisMap.put(key, obj);
	}
	
	public void clear()
	{
		thisMap.clear();
		loadState = LoadState.NOT_LOADED;
	}
	

	@Override
	public void done()
	{
	}
	
//	public void load(LinkedHashMap<String, Object> rawAliases)
//	{
//		clear();
//		ModDamageLogger.console_only("");
//		if(rawAliases != null && !rawAliases.isEmpty())
//		{
//			loadState = LoadState.SUCCESS;
//			ModDamage.addToLogRecord(OutputPreset.INFO_VERBOSE, this.name + " aliases found, parsing...");
//			for(String alias : rawAliases.keySet())
//				thisMap.put("_" + alias, getDefaultValue());
//			for(Entry<String, Object> entry : rawAliases.entrySet())
//			{
//				ModDamageLogger.console_only("");
//				if(entry.getValue() != null)
//				{
//					if(!this.completeAlias("_" + entry.getKey(), entry.getValue()))
//						this.loadState = LoadState.FAILURE;
//				}
//				else ModDamage.addToLogRecord(OutputPreset.INFO_VERBOSE, "Found empty " + this.name.toLowerCase() + " alias \"" + entry.getKey() + "\", ignoring...");
//			}
//			for(String alias : thisMap.keySet())
//				if(thisMap.get(alias) == null)
//					thisMap.remove(alias);
//		}
//	}
	
	//abstract protected StoredInfoClass getNewStorageClass(Type value);
	protected StoredInfoClass getDefaultValue(){ return null; }

	abstract public static class SingleValueAliaser<Type> extends Aliaser<Type, Type>
	{
		SingleValueAliaser(String name){ super(name); }
		
		@Override
		public ScriptLineHandler handleLine(final ScriptLine nameLine, boolean hasChildren)
		{
			return new ScriptLineHandler() {
				Type value;
				boolean hasValue;
				
				@Override
				public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
				{
					if (hasValue) {
						ModDamageLogger.error(line, name+" alias _"+nameLine.line+" cannot have multiple values.");
						return null;
					}
					value = matchAlias(line.line);
					hasValue = true;
					return null;
				}
				
				@Override
				public void done()
				{
					if (!hasValue) {
						ModDamageLogger.error(nameLine, name+" alias _"+nameLine.line+" has no value.");
						return;
					}
					putAlias("_"+nameLine.line, value);
				}
			};
		}
		
		public Type matchAlias(String key)
		{
			if(hasAlias(key))
				return getAlias(key);
			
			
			Type value = matchNonAlias(key);
			ModDamageLogger.error("No matching " + name + " alias or value \"" + key + "\"");
			return value;
		}
		
		abstract Type matchNonAlias(String string);
	}
	
	abstract public static class CollectionAliaser<InfoType> extends Aliaser<InfoType, Collection<InfoType>>
	{
		CollectionAliaser(String name){ super(name); }
		
		public void putAllAliases(String key, Collection<InfoType> items)
		{
			Collection<InfoType> aliases = getAlias(key);
			if (aliases == null)
			{
				putAlias(key, items);
				return;
			}
			
			aliases.addAll(items);
		}
		

		@Override
		public ScriptLineHandler handleLine(final ScriptLine nameLine, boolean hasChildren)
		{
			return new ScriptLineHandler() {
				Collection<InfoType> values = new ArrayList<InfoType>();
				boolean hasValue;
				
				@Override
				public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
				{
					Collection<InfoType> subvalues = matchAlias(line.line);
					if (subvalues != null)
						values.addAll(subvalues);
					else {
						InfoType value = matchNonAlias(line.line);
						values.add(value);
					}
					hasValue = true;
					return null;
				}
				
				@Override
				public void done()
				{
					if (!hasValue) {
						ModDamageLogger.error(nameLine, name+" alias _"+nameLine.line+" has no value.");
						return;
					}
					putAlias("_"+nameLine.line, values);
				}
			};
		}
		
		public Collection<InfoType> matchAlias(String key)
		{
			if(hasAlias(key))
				return getAlias(key);
			
			
			boolean failFlag = false;
			List<InfoType> values = new ArrayList<InfoType>();
			if (key != null)
			{
				for(String valueString : key.split(","))
				{
					InfoType value = matchNonAlias(valueString);
					if(value != null) values.add(value);
					else failFlag = true;
				}
			}
			if(!failFlag && !values.isEmpty()) return values;
			ModDamageLogger.error("No matching " + name + " alias or value \"" + key + "\"");
			return getDefaultValue();
		}

		abstract protected InfoType matchNonAlias(String valueString);

		@Override
		protected Collection<InfoType> getDefaultValue(){ return new ArrayList<InfoType>(); }
	}
}