package com.ModDamage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.ModDamage.ModDamageLogger.DebugSetting;
import com.ModDamage.ModDamageLogger.OutputPreset;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.ScriptLineHandler;
import com.ModDamage.Backend.Configuration.ScriptParser;
import com.ModDamage.Backend.Configuration.Alias.AliasManager;
import com.ModDamage.Backend.Minecraft.Events.Command;
import com.ModDamage.Backend.Minecraft.Events.MDEvent;
import com.ModDamage.Backend.Minecraft.Events.Repeat;
import com.ModDamage.External.ExtensionManager;
import com.ModDamage.External.ExtensionManager.GroupsManager;
import com.ModDamage.WebServer.MDServer;

public class ModDamageConfigurationHandler implements ScriptLineHandler
{
	protected static final String configString_defaultConfigPath = "config.mdscript";

	private static final String TM_MAINLOAD = "Entire Reload";
	private static final String TM_EXT_PL_MAN = "External Plugin Manager";
	private static final String TM_SCRIPTLOAD = "Script Loading";
	private static final String TM_MDEvent = "ModDamage Event";

	private final File configFile = new File(ModDamage.getInstance().getDataFolder(), configString_defaultConfigPath);
	public static final String newline = System.getProperty("line.separator");

	
	/*
	 * Enum that reflects the plugin configuration load state. Helper members and functions are for log.setLogFile output and state merging,
	 *   when combining reports from ModDamage's several modules.
	 * Merging rules are simple: FAILURE overrides any SUCCESS, which overrides any NOT_LOADED states.
	 */
	public enum LoadState
	{
		NOT_LOADED(ChatColor.GRAY + "NO  "), FAILURE(ChatColor.RED + "FAIL"), SUCCESS(ChatColor.GREEN + "YES ");

		private String string;
		private LoadState(String string){ this.string = string; }

		public String statusString(){ return string; }

		public static LoadState combineStates(LoadState... states)
		{
			return combineStates(Arrays.asList(states));
		}

		public static LoadState combineStates(Collection<LoadState> loadStates)
		{
			return loadStates.contains(FAILURE) ? LoadState.FAILURE : (loadStates.contains(SUCCESS) ? LoadState.SUCCESS : LoadState.NOT_LOADED);
		}

		protected static LoadState pluginState = LoadState.NOT_LOADED;
	}
	
	int tags_save_interval;
	
	//Web server info
	String serverBindaddr;
	int serverPort;
	String serverUsername;
	String serverPassword;
	
	private ModDamageLogger log;
	
	boolean appendLog;
	File logFile;
	
	public ModDamageConfigurationHandler() {
		this(ModDamage.getInstance());
	}
	
	public ModDamageConfigurationHandler(ModDamage instance) {
		this.log = new ModDamageLogger(this);
		this.plugin = instance;
	}

	protected Plugin plugin;
	
	public static final int DEFAULT_TAGSAVEINTERVAL = 200;
	public static final int DEFAULT_SERVERPORT = 8765;
	
	/**
	 * Sets the default generic configuration values.
	 */
	private void resetDefaultSettings()
	{
		getLog().currentSetting = DebugSetting.NORMAL;
		logFile = null;
		getLog().setLogFile(logFile);
		
		tags_save_interval = DEFAULT_TAGSAVEINTERVAL;
		
		serverBindaddr = null;
		serverPort = DEFAULT_SERVERPORT;
		serverUsername = null;
		serverPassword = null;
	}
	
	private class SettingsLineHandler implements ScriptLineHandler
	{
		private Pattern settingPattern = Pattern.compile("\\s*([^=]+?)\\s*=\\s*(.*?)\\s*");
		
		@Override
		public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
		{
			Matcher m = settingPattern.matcher(line.line);
			if (!m.matches()) {
				getLog().error("Invalid setting: \"" + line.line + "\"");
				return null;
			}
			
			String name = m.group(1).trim().toLowerCase().replaceAll("\\s+", "-");
			String value = m.group(2).trim();
			getLog().info_verbose("setting: '" + name + "' = '"+value+"'");
			
			//Debug mode parsing
			if (name.equals("debugging")) {
				try {
					getLog().currentSetting = DebugSetting.valueOf(value.toUpperCase());
				}
				catch (IllegalArgumentException e) {
					getLog().error("Bad debug level: " + value);
				}
			}
			//Logfile settings
			else if (name.equals("log.setLogFile-file")) {
				logFile = new File(ModDamage.getInstance().getDataFolder(), value);
			}
			else if (name.equals("append-logs")) {
				appendLog = Boolean.parseBoolean(value);
			}
			//Miscellaneous self-explanatory boolean config values
			else if (name.equals("disable-death-messages")) {
				MDEvent.disableDeathMessages = Boolean.parseBoolean(value);
			}
			else if (name.equals("disable-join-messages")) {
				MDEvent.disableJoinMessages = Boolean.parseBoolean(value);
			}
			else if (name.equals("disable-quit-messages")) {
				MDEvent.disableQuitMessages = Boolean.parseBoolean(value);
			}
			else if (name.equals("disable-kick-messages")) {
				MDEvent.disableKickMessages = Boolean.parseBoolean(value);
			}
			//Tags save interval (in ticks)
			else if (name.equals("tags-save-interval")) {
				try {
					tags_save_interval = Integer.parseInt(value);
				}
				catch (NumberFormatException e) {
					getLog().error("Bad tags save interval: " + value);
				}
			}
			//Web server access configuration
			else if (name.equals("server-bindaddr")) {
				serverBindaddr = value;
			}
			else if (name.equals("server-port")) {
				try {
					serverPort = Integer.parseInt(value);
				}
				catch (NumberFormatException e) {
					getLog().error("Bad server port: " + value);
				}
			}
			else if (name.equals("server-username")) {
				serverUsername = value;
			}
			else if (name.equals("server-password")) {
				serverPassword = value;
			}
			else {
				getLog().error("Unknown setting: " + m.group(1));
			}
			
			return null;
		}

		@Override
		public void done()
		{
		}
	}

	/*
	 * Returns ScriptLineHandler based upon the detected type of configuration.
	 * (non-Javadoc)
	 * @see com.ModDamage.Backend.ScriptLineHandler#handleLine(com.ModDamage.Backend.ScriptLine, boolean)
	 */
	@Override
	public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
	{
		String[] words = line.line.split("\\s+");
		if (words.length == 0) return null;
		
		String word0 = words[0].toLowerCase();
		if (words.length == 1)
		{
			if (word0.equals("aliases")) {
				return AliasManager.getLineHandler();
			}
			else if (word0.equals("settings")) {
				return new SettingsLineHandler();
			}
		}
		else
		{
			if (word0.equals("on") && words.length == 2) {
				if (hasChildren) {
					MDEvent e = MDEvent.getEvent(words[1]);
					if (e == null) {
						getLog().warning(line, word0 + " " + words[1] + "is not valid. Possible that the event is not loaded!");
						return null;
					}
					
					return e.getLineHandler();
				}
				return null;
			}
		}
		return null;
	}

	@Override
	public void done()
	{
		//TODO Something?
	}
	
	public boolean reload(boolean reloadingAll)
	{
		StopWatch stopwatch = new StopWatch();
		stopwatch.start(TM_MAINLOAD);
		LoadState.pluginState = LoadState.NOT_LOADED;
		getLog().resetLogCount();
		getLog().resetWorstLogMessageLevel();
		resetDefaultSettings();
		Command.instance.reset();
		Repeat.instance.reset();
		MDEvent.unregisterEvents();
		MDEvent.clearEvents();
		
		getLog().addToLogRecord(OutputPreset.CONSTANT, "[" + ModDamage.getInstance().getDescription().getName() + "] v" + ModDamage.getInstance().getDescription().getVersion() + " loading...");

		if(reloadingAll)
		{
			stopwatch.start(TM_EXT_PL_MAN);
			MDEvent.registerVanillaEvents();
			ExtensionManager.reload();
			if(ExtensionManager.getGroupsManager() == GroupsManager.None)
				getLog().addToLogRecord(OutputPreset.INFO_VERBOSE, "Permissions: No permissions plugin found.");
			else
				getLog().addToLogRecord(OutputPreset.CONSTANT, "Permissions: " + ExtensionManager.getGroupsManager().name() + " v" + GroupsManager.getVersion());
			
			if(ExtensionManager.regionsManagers.isEmpty())
				getLog().addToLogRecord(OutputPreset.INFO_VERBOSE, "Region Plugins: No regional plugins found.");
			else
				getLog().addToLogRecord(OutputPreset.CONSTANT, "Region Plugins: " + Utils.joinBy(", ", ExtensionManager.regionsManagers));
			
			if(ExtensionManager.getMcMMOPlugin() == null)
				getLog().addToLogRecord(OutputPreset.INFO_VERBOSE, "mcMMO: Plugin not found.");
			else
				getLog().addToLogRecord(OutputPreset.CONSTANT, "mcMMO: Using version " + ExtensionManager.getMcMMOPlugin().getDescription().getVersion());
			
			stopwatch.stop(TM_EXT_PL_MAN);
		}

		
		stopwatch.start(TM_SCRIPTLOAD);
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(configFile);
			ScriptParser parser = new ScriptParser(stream);
			parser.parseScript(this);
		}
		catch (FileNotFoundException e)
		{
			if(!writeDefaults())
				return false;
		}
		catch (IOException e){ getLog().printToLog(Level.SEVERE, "Fatal: could not close " + configString_defaultConfigPath + "!"); }
		finally {
			if (stream != null) {
				try {
					stream.close();
				}
				catch (IOException e) { }
			}
		}
		stopwatch.stop(TM_SCRIPTLOAD);
		
		stopwatch.start(TM_MDEvent);
		MDEvent.registerEvents();
		stopwatch.stop(TM_MDEvent);
		
		boolean loggingEnabled = false;
			if (logFile != null)
			{
				loggingEnabled = true;
				getLog().setLogFile(null);
			}
			getLog().setLogFile(logFile, appendLog);
		
		if (loggingEnabled)
		{
			if (ModDamage.getInstance().getLogger().getHandlers().length > 0)
				getLog().addToLogRecord(OutputPreset.INFO, "File Logging for 'config.yml' is enabled.");
			else
				getLog().addToLogRecord(OutputPreset.FAILURE, "File logging failed to load for '" + "config.yml" + "'.");
		}
		else
			getLog().addToLogRecord(OutputPreset.INFO, "File logging for '" + "config.yml" + "' is disabled.");
		
		// Default message settings
		if(MDEvent.disableDeathMessages)
			getLog().constant("Vanilla death messages disabled.");
		else
			getLog().info_verbose("Vanilla death messages enabled.");
		
		if(MDEvent.disableJoinMessages)
			getLog().constant("Vanilla join messages disabled.");
		else
			getLog().info_verbose("Vanilla join messages enabled.");
		
		if(MDEvent.disableQuitMessages)
			getLog().constant("Vanilla quit messages disabled.");
		else
			getLog().info_verbose("Vanilla quit messages enabled.");
		
		if(MDEvent.disableKickMessages)
			getLog().constant("Vanilla kick messages disabled.");
		else
			getLog().info_verbose("Vanilla kick messages enabled.");
		

		if(serverUsername != null && serverPassword != null) {
			getLog().constant("Web server starting on port "+ (serverBindaddr != null? serverBindaddr : "*") +":"+ serverPort);
			MDServer.startServer(serverBindaddr, serverPort, serverUsername, serverPassword);
		} else
			getLog().info_verbose("Web server not started");
		
		
		LoadState.pluginState = LoadState.combineStates(MDEvent.getCombinedLoadState(), AliasManager.getState());
		
		double time = stopwatch.stop(TM_MAINLOAD);
		String timer = "(" + time + " \u00b5s) ";
		
		getLog().addToLogRecord(OutputPreset.INFO_VERBOSE, "Timings:");
		
		getLog().changeIndentation(true);
		
		getLog().addToLogRecord(OutputPreset.INFO_VERBOSE, "Event Loading: " + (stopwatch.time(TM_MDEvent)/1000) + " \u00b5s) ");
		getLog().addToLogRecord(OutputPreset.INFO_VERBOSE, "External Event Manager: "+ (stopwatch.time(TM_EXT_PL_MAN)/1000) + " \u00b5s");
		getLog().addToLogRecord(OutputPreset.INFO_VERBOSE, "Script Loading: " + (stopwatch.time(TM_SCRIPTLOAD)/1000) + " \u00b5s) ");
		
		getLog().changeIndentation(false);
		
		switch(LoadState.pluginState)
		{
			case NOT_LOADED:
				getLog().addToLogRecord(OutputPreset.CONSTANT, getLog().logPrepend() + timer + "No configuration loaded.");
				break;
			case FAILURE:
				getLog().addToLogRecord(OutputPreset.CONSTANT, getLog().logPrepend() + timer + "Loaded configuration with one or more errors.");
				break;
			case SUCCESS:
				int worstValue = getLog().worstLogMessageLevel.intValue();
				
				if (worstValue >= Level.SEVERE.intValue()) {
					getLog().addToLogRecord(OutputPreset.CONSTANT, getLog().logPrepend() + timer + "Finished loading configuration with errors.");
				}
				else if (worstValue >= Level.WARNING.intValue()) {
					getLog().addToLogRecord(OutputPreset.CONSTANT, getLog().logPrepend() + timer + "Finished loading configuration with warnings.");
				}
				else if (worstValue >= Level.INFO.intValue()) {
					getLog().addToLogRecord(OutputPreset.CONSTANT, getLog().logPrepend() + timer + "Finished loading configuration.");
				}
				else {
					getLog().addToLogRecord(OutputPreset.CONSTANT, getLog().logPrepend() + timer + "Weird reload: " + getLog().worstLogMessageLevel);
				}
				
				break;
				
			default: throw new Error("Unknown state: "+LoadState.pluginState+" $PC280");
		}
		
		if (getLog().getDebugSetting() == DebugSetting.QUIET && getLog().logMessagesSoFar >= getLog().maxLogMessagesToShow)
			getLog().printToLog(Level.INFO, "Suppressed "+(getLog().logMessagesSoFar-getLog().maxLogMessagesToShow)+" error messages");
		
		return true;
	}
	
	private boolean writeDefaults()
	{
		getLog().addToLogRecord(OutputPreset.INFO, getLog().logPrepend() + "No configuration file found! Writing a blank config in " + configString_defaultConfigPath + "...");
		if(!configFile.exists())
		{
			try
			{
				if(!(configFile.getParentFile().exists() || configFile.getParentFile().mkdirs()) || !configFile.createNewFile())
				{
					getLog().printToLog(Level.SEVERE, "Fatal error: could not create " + configString_defaultConfigPath + ".");
					return false;
				}
			}
			catch (IOException e)
			{
				getLog().printToLog(Level.SEVERE, "Error: could not create new " + configString_defaultConfigPath + ".");
				e.printStackTrace();
				return false;
			}
		}
		String outputString = "#Auto-generated config at " + (new Date()).toString() + "." + newline + "#See the wiki at https://github.com/ModDamage/ModDamage/wiki for more information." + newline;
		

		outputString += newline + newline +  "Settings";
		outputString += newline + "\t## Port probably has to be larger than 1024";
		outputString += newline + "\t## Uncomment the following to enable the server";
		outputString += newline + "\t## bindaddr should be left empty if you want the server to be accessable from anywhere";
		outputString += newline + "\t#server-bindaddr = ";
		outputString += newline + "\t#server port = 8765";
		outputString += newline + "\t#server username = mdadmin";
		outputString += newline + "\t#server password = nuggets";

		outputString += newline;
		outputString += newline + "\tdebugging = normal";
		outputString += newline + "\tdisable death messages = no";
		outputString += newline + "\tdisable join messages = no";
		outputString += newline + "\tdisable quit messages = no";
		outputString += newline + "\tdisable kick messages = no";
		outputString += newline + "\t#This interval should be tinkered with ONLY if you understand the implications.";
		outputString += newline + "\ttags save interval = " + tags_save_interval;
		
		outputString += newline + "\t## File Logging settings.";
		outputString += newline + "\t## To Enable File Logging. Uncomment both lines below.";
		outputString += newline + "\t##log.setLogFile file = config.log.setLogFile";
		outputString += newline + "\t##append logs = yes";
		
		//TODO Can this work?
//		outputString += newline + newline + "#Debug File Logging";
//		outputString += newline + "#Uncomment the following to enable file logging";
//		outputString += newline + "#Logging:";
//		outputString += newline + "#    " + "file: " + "config" + ".log.setLogFile";
//		outputString += newline + "#    " + "append: true";
		
		outputString += newline + newline + "Aliases";
		for(AliasManager aliasType : AliasManager.values())
		{
			outputString += newline + "\t" + aliasType.name() + "";
			switch(aliasType)
			{
				case Material:
					String[][] toolAliases = {
							{ "axe", "hoe", "pickaxe", "spade", "sword" },
							{ "WOOD_", "STONE_", "IRON_", "GOLD_", "DIAMOND_" } };
					for(String toolType : toolAliases[0])
					{
						outputString += newline + "\t\t" + toolType + "";
						for(String toolMaterial : toolAliases[1])
							outputString += newline + "\t\t\t" + toolMaterial + toolType.toUpperCase();
					}
					break;
					
				default: break;
			}
		}
		

		outputString += newline + "# Events";
		for (Entry<String, List<MDEvent>> category : MDEvent.eventCategories.entrySet())
		{
			outputString += newline + "## "+category.getKey()+" Events";
			for (MDEvent event : category.getValue())
				outputString += newline + "on " + event.name();
			outputString += newline;
		}
		
		
		getLog().printToLog(Level.INFO, "Completed auto-generation of " + configString_defaultConfigPath + ".");

		try
		{
			Writer writer = new FileWriter(configFile);
			writer.write(outputString);
			writer.close();

			FileInputStream stream = new FileInputStream(configFile);
			ScriptParser parser = new ScriptParser(stream);
			parser.parseScript(this);
			stream.close();
		}
		catch (IOException e)
		{
			getLog().printToLog(Level.SEVERE, "Error writing to " + configString_defaultConfigPath + ".");
		}
		return true;
	}
	

	
	
	private static boolean replaceOrAppendInFile(File file, String targetRegex, String replaceString)
	{
		Pattern targetPattern = Pattern.compile(targetRegex, Pattern.CASE_INSENSITIVE);
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Matcher matcher;
			StringBuffer contents = new StringBuffer((int) file.length());
			String line;
			boolean changedFlag = false;
			while (reader.ready())
			{
				line = reader.readLine();
				matcher = targetPattern.matcher(line);
				if(matcher.matches())
				{
					changedFlag = true;
					contents.append(matcher.replaceAll(replaceString));
				}
				else contents.append(line);
				contents.append(newline);
			}
			reader.close();
			if(!changedFlag)
				contents.append(replaceString + newline);

			FileWriter writer = new FileWriter(file);
			writer.write(String.valueOf(contents));
			writer.close();
		}
		catch (FileNotFoundException e)
		{

		}
		catch (IOException e)
		{
		}
		return true;
	}

	public void toggleDebugging(Player player)
	{
		switch(getLog().getDebugSetting())
		{
			case QUIET:
				setDebugging(player, DebugSetting.NORMAL);
				break;
			case NORMAL:
				setDebugging(player, DebugSetting.VERBOSE);
				break;
			case VERBOSE:
				setDebugging(player, DebugSetting.QUIET);
				break;
			default:
				break;
		}
	}
	
	public void setDebugging(Player player, DebugSetting setting)
	{
		if(setting != null)
		{
			if(!getLog().getDebugSetting().equals(setting))
			{
				if(replaceOrAppendInFile(configFile, "debugging:.*", "debugging: " + setting.name().toLowerCase()))
				{
					ModDamage.sendMessage(player, "Changed debug from " + getLog().getDebugSetting().name().toLowerCase() + " to " + setting.name().toLowerCase(), ChatColor.GREEN);
					getLog().setDebugSetting(setting);
				}
				else if(player != null)
					player.sendMessage(ModDamage.chatPrepend(ChatColor.RED) + "Couldn't save changes to " + configString_defaultConfigPath + ".");
			}
			else ModDamage.sendMessage(player, "Debug already set to " + setting.name().toLowerCase() + "!", ChatColor.RED);
		}
		else getLog().printToLog(Level.SEVERE, "Error: bad debug setting sent. Valid settings: normal, quiet, verbose");// shouldn't																								// happen
	}

	public String name() {
		return "config.mdscript";
	}

	public ModDamageLogger getLog() {
		return log;
	}
}
