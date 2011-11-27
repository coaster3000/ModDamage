package com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.PluginConfiguration.OutputPreset;
import com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing.Aliaser.CollectionAliaser;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.NestedRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

public class RoutineAliaser extends CollectionAliaser<Routine> 
{
	private static final long serialVersionUID = -2744471820826321788L;
	public RoutineAliaser(){ super("Routine");}
	
	@Override
	public boolean completeAlias(String key, Object values)
	{
		if(values instanceof List)
		{
			ModDamage.addToLogRecord(OutputPreset.INFO, "Adding " + name + " alias \"" + key + "\"");
			if(values.toString().contains(key))
			{
				ModDamage.changeIndentation(true);
				ModDamage.addToLogRecord(OutputPreset.WARNING, "Warning: \"" + key + "\" is self-referential!");
				ModDamage.changeIndentation(false);
			}
			
			List<Routine> matchedItems = new ArrayList<Routine>();
			if(!parseRoutines(matchedItems, values))
			{
				ModDamage.addToLogRecord(OutputPreset.FAILURE, "Error adding value " + values.toString());
				return false;
			}
			
			this.get(key).addAll(matchedItems);
			return true;
		}
		ModDamage.addToLogRecord(OutputPreset.FAILURE, "Error adding alias \"" + key + "\" - unrecognized value \"" + values.toString() + "\"");
		return false;
	}

	@Override
	public Collection<Routine> matchAlias(String key)
	{
		return this.containsKey(key)?this.get(key):null;
	}
	
	@Override
	@Deprecated
	protected Routine matchNonAlias(String key){ return null;}
	
	//Parse routine strings recursively
	public static boolean parseRoutines(List<Routine> target, Object object)
	{
		ModDamage.changeIndentation(true);
		boolean returnResult = recursivelyParseRoutines(target, object);
		ModDamage.changeIndentation(false);
		return returnResult;
	}
	@SuppressWarnings("unchecked")
	private static boolean recursivelyParseRoutines(List<Routine> target, Object object)
	{
		boolean encounteredError = false;
		if(object != null)
		{
			if(object instanceof String)
			{
				if(((String)object).startsWith("_"))
				{
					Collection<Routine> aliasedRoutines = AliasManager.matchRoutineAlias((String)object);
					if(aliasedRoutines != null)
					{
						ModDamage.addToLogRecord(OutputPreset.INFO, "Alias: \"" + ((String)object).substring(1) + "\"");
						target.addAll(aliasedRoutines);
					}
					else
					{
						ModDamage.addToLogRecord(OutputPreset.FAILURE, "Invalid routine alias " + ((String)object).substring(1));
						encounteredError = true;
					}
				}
				else
				{
					Routine routine = Routine.getNew((String)object);
					if(routine != null) target.add(routine);
					else
					{
						ModDamage.addToLogRecord(OutputPreset.FAILURE, "Invalid base routine " + " \"" + (String)object + "\"");
						encounteredError = true;
					}
				}
			}
			else if(object instanceof LinkedHashMap)
			{
				LinkedHashMap<String, Object> someHashMap = (LinkedHashMap<String, Object>)object;
				if(someHashMap.keySet().size() == 1)
					for(Entry<String, Object> entry : someHashMap.entrySet())//A properly-formatted nested routine is a LinkedHashMap with only one key.
					{
						NestedRoutine routine = NestedRoutine.getNew(entry.getKey(), entry.getValue());
						if(routine != null) target.add(routine);
						else
						{
							encounteredError = true;
							break;
						}
					}
				else ModDamage.addToLogRecord(OutputPreset.FAILURE, "Parse error: bad nested routine \"" + someHashMap.toString() + "\"");
			}
			else if(object instanceof List)
				for(Object nestedObject : (List<Object>)object)
				{
					if(!recursivelyParseRoutines(target, nestedObject))
						encounteredError = true;
				}
			else
			{
				ModDamage.addToLogRecord(OutputPreset.FAILURE, "Parse error: did not recognize object " + object.toString() + " of type " + object.getClass().getName());
				encounteredError = true;
			}
		}
		else
		{
			ModDamage.addToLogRecord(OutputPreset.FAILURE, "Parse error: null");
			encounteredError = true;
		}
		return !encounteredError;
	}

	@Override
	protected String getObjectName(Routine routine){ return routine.getClass().getSimpleName();}
}