package com.ModDamage.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.ModDamage.ModDamage;
import com.ModDamage.PluginConfiguration;
import com.ModDamage.PluginConfiguration.OutputPreset;
import com.ModDamage.Alias.RoutineAliaser;
import com.ModDamage.Backend.BailException;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.EventInfo.SimpleEventInfo;
import com.ModDamage.Routines.Routines;


public class Command
{
	static Map<String, List<CommandInfo>> commandMap = new HashMap<String, List<CommandInfo>>();
	
	@SuppressWarnings("unchecked")
	public static void reload()
	{
		commandMap.clear();
		
		LinkedHashMap<String, Object> entries = ModDamage.getPluginConfiguration().getConfigMap();
		Object commands = PluginConfiguration.getCaseInsensitiveValue(entries, "Command");
		
		if(commands == null)
			return;
	
		if (!(commands instanceof List))
		{
			ModDamage.addToLogRecord(OutputPreset.FAILURE, "Expected List, got "+commands.getClass().getSimpleName()+"for Command event");
			return;
		}
		
		List<LinkedHashMap<String, Object>> commandConfigMaps = (List<LinkedHashMap<String, Object>>) commands;
		if(commandConfigMaps == null || commandConfigMaps.size() == 0)
			return;
		
		ModDamage.addToLogRecord(OutputPreset.CONSOLE_ONLY, "");
		ModDamage.addToLogRecord(OutputPreset.INFO_VERBOSE, "Loading commands...");
		
		ModDamage.changeIndentation(true);
		
		entryLoop: for (LinkedHashMap<String, Object> commandConfigMap : commandConfigMaps)
		for (Entry<String, Object> commandEntry : commandConfigMap.entrySet())
		{
			String[] commandSpec = commandEntry.getKey().split("\\s+");
			String name = commandSpec[0];
			Argument[] args;
			boolean catchAll = false;
			
			if (commandSpec.length > 1 && commandSpec[commandSpec.length-1].equals("*"))
			{
				args = new Argument[commandSpec.length - 2];
				catchAll = true;
			}
			else
				args = new Argument[commandSpec.length - 1];
			
			StringBuilder logSB = new StringBuilder();
			
			for (int i = 1; i < commandSpec.length - (catchAll?1:0); i++)
			{				
				Argument arg = Argument.get(commandSpec[i]);
				if (arg == null) {
					ModDamage.addToLogRecord(OutputPreset.FAILURE, 
							"Please prefix command arguments with # for number or & for player, or [a-z] for raw, not "
							+commandSpec[i].substring(0, 1));
					continue entryLoop;
				}
				args[i-1] = arg;
				logSB.append(" "+arg.name+"("+arg.type+")");
			}
			if (catchAll) 
				logSB.append(" *");
			
			CommandInfo command = new CommandInfo(name, args, catchAll);
			ModDamage.addToLogRecord(OutputPreset.INFO, "Command ["+command.name+"]: "+logSB.toString());
			command.routines = RoutineAliaser.parseRoutines(commandEntry.getValue(), command.eventInfo);
			if (command.routines == null)
				continue;
			
			List<CommandInfo> cmds = commandMap.get(name);
			if (cmds == null)
			{
				cmds = new ArrayList<CommandInfo>();
				commandMap.put(name, cmds);
			}
			cmds.add(command);
		}

		ModDamage.changeIndentation(false);
	}
	
	
	static class CommandInfo
	{
		String name;
		Argument[] args;
		
		EventInfo eventInfo;
		Routines routines;
		
		boolean catchAll;
		
		public CommandInfo(String name, Argument[] args, boolean catchAll)
		{
			this.name = name;
			this.args = args;
			this.catchAll = catchAll;
			
			// build info list for my eventInfo object
			List<Object> infoList = new ArrayList<Object>(2*args.length + 4);
			infoList.add(Player.class);
			infoList.add("sender");
			infoList.add(World.class);
			infoList.add("world");
			
			for (Argument arg : args)
				arg.addToEventInfoList(infoList);
			
			eventInfo = new SimpleEventInfo(infoList.toArray(), false);
		}
	}
	
	static abstract class Argument
	{
		String type;
		String name;
		
		public Argument(String name, String type)
		{
			this.name = name;
			this.type = type;
		}
		
		public static Argument get(String string)
		{
			if (string.startsWith("&"))
				return new Argument(string.substring(1), "Player") {
					@Override
					public boolean addToEventDataList(List<Object> dataList, String arg)
					{
						Player player = Bukkit.getPlayer(arg);
						if (player == null) return false;
						dataList.add(player);
						return true;
					}
					public void addToEventInfoList(List<Object> list)
					{
						list.add(Player.class);
						super.addToEventInfoList(list);
					}
					
				};
			if (string.startsWith("#"))
				return new Argument(string.substring(1), "Number") {
					@Override
					public boolean addToEventDataList(List<Object> dataList, String arg)
					{
						Player player = Bukkit.getPlayer(arg);
						if (player == null) return false;
						dataList.add(player);
						return true;
					}
					public void addToEventInfoList(List<Object> list)
					{
						list.add(Player.class);
						super.addToEventInfoList(list);
					}
				};
			if (string.matches("^[a-zA-Z].*"))
				return new Argument(string, "Word") {
					@Override
					public boolean addToEventDataList(List<Object> dataList, String arg)
					{
						return arg.equalsIgnoreCase(name);
					}
					public void addToEventInfoList(List<Object> list)
					{
					}
				};
			return null;
		}

		public abstract boolean addToEventDataList(List<Object> dataList, String arg);
		
		public void addToEventInfoList(List<Object> list)
		{
			list.add(name);
		}
	}
	
	public static class CommandEventHandler implements Listener
	{
		@EventHandler(priority=EventPriority.LOW)
		public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event)
		{
			if (event.isCancelled()) return;
			
			String[] words = event.getMessage().split("\\s+");
			if (words.length == 0) return;
			
			List<CommandInfo> commands = commandMap.get(words[0]);
			if (commands == null) return;
			commandLoop: for (CommandInfo cmd : commands)
			{
				if (!(cmd.catchAll? words.length - 1 >= cmd.args.length : words.length - 1 == cmd.args.length))
					continue;
				
				List<Object> dataArgs = new ArrayList<Object>(cmd.args.length + 1); // estimate
				dataArgs.add(event.getPlayer());
				dataArgs.add(event.getPlayer().getWorld());
				
				for (int i = 1; i < words.length; i++)
				{
					if (i-1 >= cmd.args.length)
						break;
					
					if (!cmd.args[i-1].addToEventDataList(dataArgs, words[i]))
						continue commandLoop;
				}
				
				EventData data = cmd.eventInfo.makeData(dataArgs.toArray(), false);
				try
				{
					if (cmd.routines != null)
						cmd.routines.run(data);
				}
				catch (BailException e)
				{
					ModDamage.reportBailException(e);
				}
				
				event.setCancelled(true);
				
				return;
			}
			
		}
	}
}