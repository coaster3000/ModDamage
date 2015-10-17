package com.moddamage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.Plugin;

import com.moddamage.MDLogger.DebugSetting;
import com.moddamage.MDLogger.OutputPreset;
import com.moddamage.alias.AliasManager;
import com.moddamage.backend.ExternalPluginManager;
import com.moddamage.backend.ExternalPluginManager.GroupsManager;
import com.moddamage.backend.ScriptLine;
import com.moddamage.backend.ScriptLineHandler;
import com.moddamage.backend.ScriptParser;
import com.moddamage.events.Command;
import com.moddamage.events.Repeat;
import com.moddamage.server.MDServer;

public class PluginConfiguration extends BaseConfigScript
{

	private static final String TM_MAINLOAD = "Entire Reload";
	private static final String TM_EXT_PL_MAN = "External Plugin Manager";
	private static final String TM_SCRIPTLOAD = "Script Loading";
	private static final String TM_MDEvent = "ModDamage Event";
	
	protected static final String configString_defaultConfigPath = "config.mdscript";
	private File scriptDirectory;
	private ScriptManager.LoadMethod loadMethod = ScriptManager.LoadMethod.ENABLED_SETTING;
	private ScriptManager.LoadMethod prevLoadMethod;
	private boolean recurseDirectorties = false;


	public PluginConfiguration(Plugin plugin)
	{
		super(plugin, new File(plugin.getDataFolder(), configString_defaultConfigPath), "config");
	}
	
	int tags_save_interval;
	
	String serverBindaddr;
	int serverPort;
	String serverUsername;
	String serverPassword;

	private void resetDefaultSettings()
	{
		getLogger().currentSetting = DebugSetting.NORMAL;
		logFile = null;
		getLogger().setLogFile(null);
		
		tags_save_interval = 200;

		scriptDirectory = plugin.getDataFolder();

		serverBindaddr = null;
		serverPort = 8765;
		serverUsername = null;
		serverPassword = null;
	}

	@Override
	public ScriptLineHandler getSettingsHandler() {
		return new SettingsLineHandler();
	}

	@Override
	public LoadState getLoadState() {
		return pluginState;
	}

	public File getScriptDirectory() {
		return scriptDirectory;
	}

	public ScriptManager.LoadMethod getLoadMethod() {
		return loadMethod;
	}

	private void setLoadMethod(ScriptManager.LoadMethod loadMethod) {
		if (loadMethod != null) {
			this.prevLoadMethod = this.loadMethod; //Used to determine if full reload is needed.
			this.loadMethod = loadMethod;
		}
	}

	public boolean isRecurseDirectortiesAllowed() {
		return recurseDirectorties;
	}

	private class SettingsLineHandler implements ScriptLineHandler
	{
		private Pattern settingPattern = Pattern.compile("\\s*([^=]+?)\\s*=\\s*(.*?)\\s*");
		
		@Override
		public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
		{
			Matcher m = settingPattern.matcher(line.line);
			if (!m.matches()) {
				LogUtil.error(PluginConfiguration.this, line, "Invalid setting: \"" + line.line + "\"");
				return null;
			}
			
			String name = m.group(1).trim().toLowerCase().replaceAll("\\s+", "-");
			String value = m.group(2).trim();

			LogUtil.info_verbose(PluginConfiguration.this, line, "setting: '"+name+"' = '"+value+"'");
			
			if (name.equals("debugging")) {
				try {
					getLogger().currentSetting = DebugSetting.valueOf(value.toUpperCase());
				}
				catch (IllegalArgumentException e) {
					LogUtil.error(PluginConfiguration.this, line, "Bad debug level: " + value);
				}
			}
			else if (name.equals("log-file")) {
				logFile = new File(plugin.getDataFolder(), value);
			}
			else if (name.equals("append-logs")) {
				appendLog = Boolean.parseBoolean(value);
			}
			else if (name.equals("recursive-scan")) {
				PluginConfiguration.this.recurseDirectorties = Boolean.parseBoolean(value);
			}
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
			else if (name.equals("tags-save-interval")) {
				try {
					tags_save_interval = Integer.parseInt(value);
				}
				catch (NumberFormatException e) {
					LogUtil.error(PluginConfiguration.this, line, "Bad tags save interval: " + value);
				}
			}
			else if (name.equals("server-bindaddr")) {
				serverBindaddr = value;
			}
			else if (name.equals("server-port")) {
				try {
					serverPort = Integer.parseInt(value);
				}
				catch (NumberFormatException e) {
					LogUtil.error(PluginConfiguration.this, line,  "Bad server port: " + value);
				}
			}
			else if (name.equals("server-username")) {
				serverUsername = value;
			}
			else if (name.equals("server-password")) {
				serverPassword = value;
			}
			else if (name.equals("script-directory")) {
				if (value.isEmpty()) scriptDirectory = plugin.getDataFolder();
				else scriptDirectory = new File(plugin.getDataFolder(), value);

				if (scriptDirectory.isFile()) {
					LogUtil.error(PluginConfiguration.this, line, String.format("%s is not a directory! Please correct this in your primary script! Resorting to default directory!", scriptDirectory.getPath()));
					scriptDirectory = plugin.getDataFolder();
				}
			}
			else if (name.equals("load-method")) {
				if (value.isEmpty()) loadMethod = ScriptManager.LoadMethod.ENABLED_SETTING;
				else try {
					setLoadMethod(ScriptManager.LoadMethod.valueOf(value));
				} catch (Exception e) {
					LogUtil.error(PluginConfiguration.this, line, String.format("%s is not a valid load method! Resorting to ENABLED setting.", value));
					setLoadMethod(ScriptManager.LoadMethod.ENABLED_SETTING);
				}
			}
			else if (name.equals("priority")) {
				try {
					setPriority(Integer.parseInt(value));
				} catch (NumberFormatException e) {
					setPriority(-1);
				}
			}
			else if (name.equals("enabled")){
				try {
					setEnabled(Boolean.parseBoolean(value));
				} catch (Exception e) {
					setEnabled(true);
				}
			}
			else {
				LogUtil.error(PluginConfiguration.this, line, "Unknown setting: " + m.group(1));
			}
			
			return null;
		}

		@Override
		public void done()
		{
		}
	}

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
				return getSettingsHandler();
			}
		}
		else
		{
			if (word0.equals("on") && words.length == 2) {
				if (hasChildren) {
					MDEvent e = MDEvent.getEvent(words[1]);
					if (e == null) {
						LogUtil.warning(PluginConfiguration.this, line, word0 + " " + words[1] + "is not valid. Possible that the event is not loaded!");
						return null;
					}
					
					return e.getLineHandler(this);
				}
				return null;
			}
		}
		return null;
	}

	@Override
	public void done()
	{
		log = new MDLogger(this);
	}

	public boolean reload(boolean reloadingAll)
	{
		StopWatch sw = new StopWatch();
		sw.start(TM_MAINLOAD);
		this.pluginState = Config.LoadState.NOT_LOADED;
		resetLoggedMessages();
		resetWorstLogMessageLevel();
		resetDefaultSettings();
		Command.instance.reset();
		Repeat.instance.reset();
		MDEvent.clearEvents(this);
		
		addToLogRecord(OutputPreset.CONSTANT, "[" + plugin.getDescription().getName() + "] v" + plugin.getDescription().getVersion() + " loading...");

		if(reloadingAll)
		{
			sw.start(TM_EXT_PL_MAN);
			MDEvent.registerVanillaEvents();
			ExternalPluginManager.reload();
			if(ExternalPluginManager.getGroupsManager() == GroupsManager.None)
				addToLogRecord(OutputPreset.INFO_VERBOSE, "Permissions: No permissions plugin found.");
			else
				addToLogRecord(OutputPreset.CONSTANT, "Permissions: " + ExternalPluginManager.getGroupsManager().name() + " v" + GroupsManager.getVersion());
			
			if(ExternalPluginManager.regionsManagers.isEmpty())
				addToLogRecord(OutputPreset.INFO_VERBOSE, "Region Plugins: No regional plugins found.");
			else
				addToLogRecord(OutputPreset.CONSTANT, "Region Plugins: " + Utils.joinBy(", ", ExternalPluginManager.regionsManagers));
			
			if(ExternalPluginManager.getMcMMOPlugin() == null)
				addToLogRecord(OutputPreset.INFO_VERBOSE, "mcMMO: Plugin not found.");
			else
				addToLogRecord(OutputPreset.CONSTANT, "mcMMO: Using version " + ExternalPluginManager.getMcMMOPlugin().getDescription().getVersion());
			
			sw.stop(TM_EXT_PL_MAN);
		}

		
		sw.start(TM_SCRIPTLOAD);
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(configFile);
			ScriptParser parser = new ScriptParser(this, stream);
			parser.parseScript(this);
		}
		catch (FileNotFoundException e)
		{
			if(!writeDefaults())
				return false;

			try {
				stream = new FileInputStream(configFile);
				ScriptParser parser = new ScriptParser(this, stream);
				parser.parseScript(this);
				stream.close();
			} catch (IOException e1) {
				try {
					if (stream != null)
						stream.close();
				} catch (IOException e2) {
						printToLog(Level.SEVERE, "Fatal: could not close " + configString_defaultConfigPath + "!");
				}
			}
		}
		catch (IOException e){ printToLog(Level.SEVERE, "Fatal: could not close " + configString_defaultConfigPath + "!"); }
		finally {
			if (stream != null) {
				try {
					stream.close();
				}
				catch (IOException ignored) { }
			}
		}
		sw.stop(TM_SCRIPTLOAD);
		
		sw.start(TM_MDEvent);
		MDEvent.registerEvents();
		sw.stop(TM_MDEvent);
		
		boolean loggingEnabled = false;
			if (logFile != null)
			{
				loggingEnabled = true;
				getLogger().setLogFile(null);
			}
			getLogger().setLogFile(logFile, appendLog);
		
		if (loggingEnabled)
		{
			if (getLogger().log.getHandlers().length > 0)
				addToLogRecord(OutputPreset.INFO, "File Logging for 'config' is enabled.");
			else
				addToLogRecord(OutputPreset.FAILURE, "File logging failed to load for '" + "config" + "'.");
		}
		else
			addToLogRecord(OutputPreset.INFO, "File logging for '" + "config" + "' is disabled.");

		// Default message settings
		if(MDEvent.disableDeathMessages)
			LogUtil.constant(this, "Vanilla death messages disabled.");
		else
			LogUtil.info_verbose(this, "Vanilla death messages enabled.");

		if(MDEvent.disableJoinMessages)
			LogUtil.constant(this, "Vanilla join messages disabled.");
		else
			LogUtil.info_verbose(this, "Vanilla join messages enabled.");

		if(MDEvent.disableQuitMessages)
			LogUtil.constant(this, "Vanilla quit messages disabled.");
		else
			LogUtil.info_verbose(this, "Vanilla quit messages enabled.");

		if(MDEvent.disableKickMessages)
			LogUtil.constant(this, "Vanilla kick messages disabled.");
		else
			LogUtil.info_verbose(this, "Vanilla kick messages enabled.");
		

		if(serverUsername != null && serverPassword != null) {
			LogUtil.constant(this, "Web server starting on port "+ (serverBindaddr != null? serverBindaddr : "*") +":"+ serverPort);
			MDServer.startServer(serverBindaddr, serverPort, serverUsername, serverPassword);
		} else
			LogUtil.info_verbose(this, "Web server not started");
		
		this.pluginState = LoadState.combineStates(MDEvent.getCombinedLoadStates(this), AliasManager.getState());

		if (prevLoadMethod != null && prevLoadMethod != loadMethod) ModDamage.getScriptManager().reload(true);

		double time = sw.stop(TM_MAINLOAD);
		String timer = "(" + time + " \u00b5s) ";
		
		addToLogRecord(OutputPreset.INFO_VERBOSE, "Timings:");
		
		changeIndentation(true);
		
		addToLogRecord(OutputPreset.INFO_VERBOSE, "Event Loading: " + (sw.time(TM_MDEvent)/1000) + " \u00b5s) ");
		addToLogRecord(OutputPreset.INFO_VERBOSE, "External Event Manager: "+ (sw.time(TM_EXT_PL_MAN)/1000) + " \u00b5s");
		addToLogRecord(OutputPreset.INFO_VERBOSE, "Script Loading: " + (sw.time(TM_SCRIPTLOAD)/1000) + " \u00b5s) ");
		
		changeIndentation(false);
		
		switch(this.pluginState)
		{
			case NOT_LOADED:
				addToLogRecord(OutputPreset.CONSTANT, getLogger().logPrepend() + timer + "No configuration loaded.");
				break;
			case FAILURE:
				addToLogRecord(OutputPreset.CONSTANT, getLogger().logPrepend() + timer + "Loaded configuration with one or more errors.");
				break;
			case SUCCESS:
				int worstValue = getWorstLogMessageLevel().intValue();
				
				if (worstValue >= Level.SEVERE.intValue()) {
					addToLogRecord(OutputPreset.CONSTANT, getLogger().logPrepend() + timer + "Finished loading configuration with errors.");
				}
				else if (worstValue >= Level.WARNING.intValue()) {
					addToLogRecord(OutputPreset.CONSTANT, getLogger().logPrepend() + timer + "Finished loading configuration with warnings.");
				}
				else if (worstValue >= Level.INFO.intValue()) {
					addToLogRecord(OutputPreset.CONSTANT, getLogger().logPrepend() + timer + "Finished loading configuration.");
				}
				else {
					addToLogRecord(OutputPreset.CONSTANT, getLogger().logPrepend() + timer + "Weird reload: " + getWorstLogMessageLevel());
				}
				
				break;
				
			default: throw new Error("Unknown state: "+ this.pluginState+" $PC365");
		}
		
		if (getDebugSetting() == DebugSetting.QUIET && getLogger().logMessagesSoFar >= getLogger().maxLogMessagesToShow)
			printToLog(Level.INFO, "Suppressed "+(getLogger().logMessagesSoFar- getLogger().maxLogMessagesToShow)+" error messages");
		
		return true;
	}

	protected String getDefaultFileContents() {
		StringBuilder outputString = new StringBuilder();
		outputString.append("#Auto-generated config at ").append((new Date()).toString()).append(".").append(newline)
				.append("#See the wiki at https://github").append(".com/ModDamage/ModDamage/wiki for more information.").append(newline);


		outputString.append(newline).append(newline).append("Settings");
		outputString.append(newline).append("\t## Port probably has to be larger than 1024");
		outputString.append(newline).append("\t## Uncomment the following to enable the server");
		outputString.append(newline).append("\t## bindaddr should be left empty if you want the server to be accessable from anywhere");
		outputString.append(newline).append("\t#server-bindaddr = ");
		outputString.append(newline).append("\t#server port = 8765");
		outputString.append(newline).append("\t#server username = mdadmin");
		outputString.append(newline).append("\t#server password = nuggets");

		outputString.append(newline);
		outputString.append(newline);
		outputString.append(newline).append("\t## The directory to scan for scripts.");
		outputString.append(newline).append("\t## This excludes this file.");
		outputString.append(newline).append("\t## Default is root folder which is the same place as this file.");
		outputString.append(newline).append("\tscript directory = ");
		outputString.append(newline).append("\t## Allows traversing all directories specified from root to scan for scripts. Value may be true or false.");
		outputString.append(newline).append("\trecursive scan = false");

		outputString.append(newline).append("\t## This defines the type of loading style.");
		outputString.append(newline).append("\t## Valid options are: PRIORITY_PARSE, ENABLED_SETTING, MASTER_LIST");
//		outputString.append(newline).append("\t## MASTER_LIST: uses priorities for execution order and then this file has a list of scripts to load.");
		outputString.append(newline).append("\t## ENABLED_SETTING: Each script has a setting called enabled. If true it will load this file.");
		outputString.append(newline).append("\t## PRIORITY_PARSE: Same as enabled setting but uses the priority number. If the number is 0 or less it will be considered disabled.");
		outputString.append(newline).append("\tload-method = ENABLED_SETTING");

		outputString.append(newline);
		outputString.append(newline);
		outputString.append(newline).append("\tdebugging = normal");

		outputString.append(newline);
		outputString.append(newline).append("\t#The following values may either be true or false.");
		outputString.append(newline).append("\tdisable death messages = false");
		outputString.append(newline).append("\tdisable join messages = false");
		outputString.append(newline).append("\tdisable quit messages = false");
		outputString.append(newline).append("\tdisable kick messages = false");

		outputString.append(newline);
		outputString.append(newline).append("\t#This interval should be tinkered with ONLY if you understand the implications.");
		outputString.append(newline).append("\ttags save interval = ").append(tags_save_interval);

		outputString.append(newline).append(newline).append("\t## File Logging settings.");
		outputString.append(newline).append("\t## To Enable File Logging. Uncomment both lines below.");
		outputString.append(newline).append("\t##log file = config.log");
		outputString.append(newline).append("\t##append logs = true");

//		outputString.append(newline).append(newline).append("#Debug File Logging");
//		outputString.append(newline).append("#Uncomment the following to enable file logging");
//		outputString.append(newline).append("#Logging:");
//		outputString.append(newline).append(String.format("#\tfile: %s.log", getName()));
//		outputString.append(newline).append("#\tappend: true");

		outputString.append(newline).append(newline).append("Aliases");
		for(AliasManager aliasType : AliasManager.values())
		{
			outputString.append(newline).append("\t").append(aliasType.name());
			switch(aliasType)
			{
				case Material:
					String[][] toolAliases = {
							{ "axe", "hoe", "pickaxe", "spade", "sword" },
							{ "WOOD_", "STONE_", "IRON_", "GOLD_", "DIAMOND_" } };
					for(String toolType : toolAliases[0])
					{
						outputString.append(newline).append("\t\t").append(toolType);
						for(String toolMaterial : toolAliases[1])
							outputString.append(newline).append("\t\t\t").append(toolMaterial).append(toolType.toUpperCase());
					}
					break;

				default: break;
			}
		}


		outputString.append(newline).append("# Events");
		for (Map.Entry<String, List<MDEvent>> category : MDEvent.eventCategories.entrySet())
		{
			outputString.append(newline).append("## ").append(category.getKey()).append(" Events");
			for (MDEvent event : category.getValue())
				outputString.append(newline).append("on ").append(event.name());
			outputString.append(newline);
		}
		return outputString.toString();
	}

	// Helper Methods

}
