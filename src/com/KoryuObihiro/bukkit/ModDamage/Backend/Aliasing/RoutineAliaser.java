package com.KoryuObihiro.bukkit.ModDamage.Backend.Aliasing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.KoryuObihiro.bukkit.ModDamage.ModDamage;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage.DebugSetting;
import com.KoryuObihiro.bukkit.ModDamage.ModDamage.LoadState;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.CalculationRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.ConditionalRoutine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;
import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.SwitchRoutine;

public class RoutineAliaser extends Aliaser<Routine> 
{
	private static final long serialVersionUID = -2744471820826321788L;
	public RoutineAliaser(){ super("Routine");}

	//Predefined pattern strings
	//TODO 0.9.6 - integrate these into their respective classes when the NestedRoutine stuff works out.
		public static final String statementPart = "(?:!?(?:[\\*\\w]+)(?:\\.[\\*\\w]+)*)";
		private static final Pattern calculationPattern = Pattern.compile("((?:([\\*\\w]+)effect\\." + statementPart + ")|set)", Pattern.CASE_INSENSITIVE);//TODO 0.9.6 - Make a design decision here. Should Calculations only be "bleheffect"?
		private static final Pattern conditionalPattern = Pattern.compile("(if|if_not)\\s+(" + statementPart + "(?:\\s+([\\*\\w]+)\\s+" + statementPart + ")*)", Pattern.CASE_INSENSITIVE);
		private static final Pattern switchPattern = Pattern.compile("switch\\.(" + statementPart + ")", Pattern.CASE_INSENSITIVE);
		
	
	public boolean addAlias(String key, List<Object> values)
	{
		if(this.containsKey(key)) return false;
		ModDamage.addToLogRecord(DebugSetting.NORMAL, 0, "Adding " + name + " alias \"" + key + "\"", LoadState.SUCCESS);
		if(values.toString().contains("_" + key))
		{
			ModDamage.addToLogRecord(DebugSetting.QUIET, 1, "Error adding value \"_" + key + "\" - value is self-referential!", loadState);
			return false;
		}
		LoadState[] addStateMachine = {LoadState.SUCCESS};
		List<Routine> matchedItems = parse(values, 1, addStateMachine);
		if(!addStateMachine[0].equals(LoadState.SUCCESS))
		{
			ModDamage.addToLogRecord(DebugSetting.QUIET, 1, "Error adding value " + values.toString(), loadState);
			return false;
		}
		this.put("_" + key, matchedItems);
		return true;
	}
	
	public List<Routine> matchAlias(String key, int nestCount)
	{
		if(this.containsKey(key))
		{
			ModDamage.addToLogRecord(DebugSetting.NORMAL, nestCount, "Alias: " + key.substring(1), LoadState.SUCCESS);
			return this.get(key);
		}
		List<Routine> value = parse(key, nestCount, new LoadState[1]);//FIXME Does this work?
		if(!value.isEmpty()) return value;
		return new ArrayList<Routine>();
	}
	
	@Override
	@Deprecated
	public List<Routine> matchAlias(String key){ return null;}
	@Override
	@Deprecated
	protected Routine matchNonAlias(String key){ return null;}
	

	//Parse routine strings recursively
	@SuppressWarnings("unchecked")
	public List<Routine> parse(Object object, int nestCount, LoadState[] resultingState)
	{
		nestCount += 1;
		LoadState currentState = LoadState.SUCCESS;
		List<Routine> routines = new ArrayList<Routine>();
		if(object != null)
		{
			if(object instanceof String)
			{
				if(((String)object).startsWith("_")) routines.addAll(this.matchAlias((String)object, nestCount));
				if(routines.isEmpty())
				{
					Routine routine = null;
					for(Pattern pattern : Routine.registeredBaseRoutines.keySet())
					{
						Matcher matcher = pattern.matcher((String)object);
						if(matcher.matches())
						{
							try
							{
								routine = (Routine)Routine.registeredBaseRoutines.get(pattern).invoke(null, matcher);
								if(routine != null)
								{
									routines.add(routine);
									ModDamage.addToLogRecord(DebugSetting.NORMAL, nestCount, "Routine: \"" + (String)object + "\"", currentState);
								}
								else
								{
									//TODO: Catch what routine matched, if/when it failed.
									currentState = LoadState.FAILURE;
									ModDamage.addToLogRecord(DebugSetting.VERBOSE, 0, "Bad parameters for new " + Routine.registeredBaseRoutines.get(pattern).getClass().getSimpleName() + " \"" + (String)object + "\"", currentState);
								}
								break;
							}
							catch(Exception e){ e.printStackTrace();}
						}
					}
				}
				if(routines.isEmpty())
				{
					currentState = LoadState.FAILURE;
					ModDamage.addToLogRecord(DebugSetting.QUIET, 0, "Couldn't match base routine string" + " \"" + (String)object + "\"", currentState);
				}
			}
			else if(object instanceof LinkedHashMap)
			{
				HashMap<String, Object> someHashMap = (HashMap<String, Object>)object;//A properly-formatted nested routine is a LinkedHashMap with only one key.
				if(someHashMap.keySet().size() == 1)
					for(String key : someHashMap.keySet())
					{
						//TODO 0.9.6 - This is where the API comes in handy. :3
						Matcher conditionalMatcher = conditionalPattern.matcher(key);
						Matcher switchMatcher = switchPattern.matcher(key);
						Matcher effectMatcher = calculationPattern.matcher(key);
						if(conditionalMatcher.matches())
						{
							ModDamage.addToLogRecord(DebugSetting.CONSOLE, nestCount, "", LoadState.SUCCESS);
							ModDamage.addToLogRecord(DebugSetting.NORMAL, nestCount, "Conditional: \"" + key + "\"", LoadState.SUCCESS);
							ConditionalRoutine routine = ConditionalRoutine.getNew(conditionalMatcher, parse(someHashMap.get(key), nestCount + 1, resultingState));
							if(routine != null)
							{
								routines.add(routine);
								ModDamage.addToLogRecord(DebugSetting.VERBOSE, nestCount, "End Conditional \"" + key + "\"\n", currentState);
							}
							else
							{
								currentState = LoadState.FAILURE;
								ModDamage.addToLogRecord(DebugSetting.QUIET, 0, "Invalid Conditional"+ " \"" + key + "\"", currentState);
							}
						}
						else if(effectMatcher.matches())
						{
							ModDamage.addToLogRecord(DebugSetting.CONSOLE, nestCount, "", LoadState.SUCCESS);
							ModDamage.addToLogRecord(DebugSetting.NORMAL, nestCount, "CalculatedEffect: \"" + key + "\"", LoadState.SUCCESS);
							CalculationRoutine<?> routine = CalculationRoutine.getNew(effectMatcher, parse(someHashMap.get(key), nestCount + 1, resultingState));
							if(routine != null)
							{
								routines.add(routine);
								ModDamage.addToLogRecord(DebugSetting.VERBOSE, nestCount, "End CalculatedEffect \"" + key + "\"\n", currentState);
							}
							else
							{
								currentState = LoadState.FAILURE;
								ModDamage.addToLogRecord(DebugSetting.QUIET, 0, "Invalid CalculatedEffect \"" + key + "\"", currentState);
							}
						}
						else if(switchMatcher.matches())
						{					
							LinkedHashMap<String, Object> anotherHashMap = (someHashMap.get(key) instanceof LinkedHashMap?(LinkedHashMap<String, Object>)someHashMap.get(key):null);
							if(anotherHashMap != null)
							{
								ModDamage.addToLogRecord(DebugSetting.CONSOLE, nestCount, "", LoadState.SUCCESS);
								ModDamage.addToLogRecord(DebugSetting.NORMAL, nestCount, "Switch: \"" + key + "\"", LoadState.SUCCESS);
								LinkedHashMap<String, List<Routine>> routineHashMap = new LinkedHashMap<String, List<Routine>>();
								SwitchRoutine<?> routine = null;
								for(String anotherKey : anotherHashMap.keySet())
								{
									ModDamage.addToLogRecord(DebugSetting.CONSOLE, nestCount, "", LoadState.SUCCESS);
									ModDamage.addToLogRecord(DebugSetting.NORMAL, nestCount, " case: \"" + anotherKey + "\"", LoadState.SUCCESS);
									routineHashMap.put(anotherKey, parse(anotherHashMap.get(anotherKey), nestCount + 1, resultingState));
									ModDamage.addToLogRecord(DebugSetting.VERBOSE, nestCount, "End case \"" + anotherKey + "\"\n", LoadState.SUCCESS);
								}
								routine = SwitchRoutine.getNew(switchMatcher, routineHashMap);
								if(routine != null)
								{
									if(routine.isLoaded) routines.add(routine);
									else 
									{
										currentState = LoadState.FAILURE;
										for(String caseName : routine.failedCases)
											ModDamage.addToLogRecord(DebugSetting.QUIET, 0, "Error: invalid case \"" + caseName + "\"", currentState);
									}
									ModDamage.addToLogRecord(DebugSetting.VERBOSE, nestCount, "End Switch \"" + key + "\"", LoadState.SUCCESS);
								}
								else
								{
									currentState = LoadState.FAILURE;
									ModDamage.addToLogRecord(DebugSetting.QUIET, 0, "Error: invalid Switch \"" + key + "\"", currentState);
								}
							}
						}
						else 
						{
							currentState = LoadState.FAILURE;
							ModDamage.addToLogRecord(DebugSetting.QUIET, 0, " No match found for nested node \"" + key + "\"", currentState);							
						}
					}
				else
				{
					currentState = LoadState.FAILURE;
					ModDamage.addToLogRecord(DebugSetting.QUIET, nestCount, "Parse error: bad nested routine.", currentState);				
				} 
			}
			else if(object instanceof List)
			{
				for(Object nestedObject : (List<Object>)object)
					routines.addAll(parse(nestedObject, nestCount, resultingState));
			}
			else
			{
				currentState = LoadState.FAILURE;
				ModDamage.addToLogRecord(DebugSetting.QUIET, nestCount, "Parse error: object " + object.toString() + " of type " + object.getClass().getName(), currentState);
			}
		}
		else 
		{
			currentState = LoadState.FAILURE;
			ModDamage.addToLogRecord(DebugSetting.QUIET, nestCount, "Parse error: null", currentState);
		}
		if(currentState.equals(LoadState.FAILURE))
			resultingState[0] = LoadState.FAILURE;
		return routines;
	}

	@Override
	protected String getObjectName(Routine routine){ return routine.getClass().getSimpleName();}

}