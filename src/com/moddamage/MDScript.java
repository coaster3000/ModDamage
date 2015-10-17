package com.moddamage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
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

public class MDScript extends BaseConfigScript {

	private static final String TM_MAINLOAD = "Entire Reload";
	private static final String TM_EXT_PL_MAN = "External Plugin Manager";
	private static final String TM_SCRIPTLOAD = "Script Loading";
	private static final String TM_MDEvent = "ModDamage Event";
	private LoadState loadState = LoadState.NOT_LOADED;

	public MDScript(Plugin plugin, File configFile, String name) {
		super(plugin, configFile, name);
	}

	@Override
	public void done()
	{
		log = new MDLogger(this);
	}

	@Override
	protected boolean writeDefaults() {
		return super.writeDefaults();
	}

	public LoadState getLoadState() {
		return loadState;
	}

	@Override
	public boolean reload(boolean reloadAll) {
		StopWatch sw = new StopWatch();
		sw.start(TM_MAINLOAD);
		loadState = LoadState.NOT_LOADED;
		resetLoggedMessages();
		resetWorstLogMessageLevel();
//		resetDefaultSettings(); //What was this again?
		Command.instance.reset(); //FIXME: GOD THIS WILL BREAK A LOT OF STUFF
		Repeat.instance.reset();
		MDEvent.clearEvents(this);

		addToLogRecord(OutputPreset.CONSTANT, "[" + plugin.getDescription().getName() + "] v" + plugin.getDescription().getVersion() + " loading...");

		if(reloadAll)
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
		}
		catch (IOException e){ printToLog(Level.SEVERE, "Fatal: could not close " + getName() + ".mdscript" + "!"); }
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
				log.setLogFile(null);
			}
			log.setLogFile(logFile, appendLog);

		if (loggingEnabled)
		{
			if (log.log.getHandlers().length > 0)
				log.addToLogRecord(OutputPreset.INFO, "File Logging for '" + getName() + ".mdscript' is enabled.");
			else
				log.addToLogRecord(OutputPreset.FAILURE, "File logging failed to load for '" + getName() + ".mdscript'.");
		}
		else
			log.addToLogRecord(OutputPreset.INFO, "File logging for '" + getName() + "' is disabled.");

		loadState = LoadState.combineStates(MDEvent.getCombinedLoadStates(this), AliasManager.getState());

		double time = sw.stop(TM_MAINLOAD);
		String timer = "(" + time + " \u00b5s) ";

		addToLogRecord(OutputPreset.INFO_VERBOSE, "Timings:");

		changeIndentation(true);

		addToLogRecord(OutputPreset.INFO_VERBOSE, "Event Loading: " + (sw.time(TM_MDEvent)/1000) + " \u00b5s) ");
		addToLogRecord(OutputPreset.INFO_VERBOSE, "External Event Manager: "+ (sw.time(TM_EXT_PL_MAN)/1000) + " \u00b5s");
		addToLogRecord(OutputPreset.INFO_VERBOSE, "Script Loading: " + (sw.time(TM_SCRIPTLOAD)/1000) + " \u00b5s) ");

		changeIndentation(false);

		switch(loadState)
		{
			case NOT_LOADED:
				addToLogRecord(OutputPreset.CONSTANT, log.logPrepend() + timer + "No configuration loaded.");
				break;
			case FAILURE:
				addToLogRecord(OutputPreset.CONSTANT, log.logPrepend() + timer + "Loaded configuration with one or more errors.");
				break;
			case SUCCESS:
				int worstValue = log.worstLogMessageLevel.intValue();

				if (worstValue >= Level.SEVERE.intValue()) {
					addToLogRecord(OutputPreset.CONSTANT, log.logPrepend() + timer + "Finished loading configuration with errors.");
				}
				else if (worstValue >= Level.WARNING.intValue()) {
					addToLogRecord(OutputPreset.CONSTANT, log.logPrepend() + timer + "Finished loading configuration with warnings.");
				}
				else if (worstValue >= Level.INFO.intValue()) {
					addToLogRecord(OutputPreset.CONSTANT, log.logPrepend() + timer + "Finished loading configuration.");
				}
				else {
					addToLogRecord(OutputPreset.CONSTANT, log.logPrepend() + timer + "Weird reload: " + log.worstLogMessageLevel);
				}

				break;

			default: throw new Error("Unknown state: "+loadState+" $PC280");
		}

		if (getDebugSetting() == DebugSetting.QUIET && log.logMessagesSoFar >= log.maxLogMessagesToShow)
			printToLog(Level.INFO, "Suppressed "+(log.logMessagesSoFar-log.maxLogMessagesToShow)+" error messages");

		return true;
	}

	@Override
	protected String getDefaultFileContents() {
		StringBuilder outputString = new StringBuilder().append("#Auto-generated script at ").append((new Date()).toString()).append(".").append(newline).append("#See the wiki at https://github.com/ModDamage/ModDamage/wiki for more information.").append(newline);

		outputString.append( newline ).append( newline ).append("Settings");
		outputString.append( newline ).append("\t## Enables the config when load method is set to ENABLED mode.");
		outputString.append( newline ).append("\tenabled = true");outputString.append( newline ).append( "\tdebugging = normal");
		outputString.append( newline );
		outputString.append( newline ).append( "\t##Priority for script order. Runs from Lowest to highest. The lower the earlier it runs. Anything at or below 0 will disable the script if the load method is set to a certain state.");
		outputString.append( newline ).append( "\tpriority = 1");

		outputString.append( newline ).append( "\t## File Logging settings.");
		outputString.append( newline ).append( "\t## To Enable File Logging. Uncomment both lines below.");
		outputString.append( newline ).append( "\t##log file = config.log");
		outputString.append( newline ).append( "\t##append logs = true");

		outputString.append( newline ).append( newline ).append( "# Events");
		for (Entry<String, List<MDEvent>> category : MDEvent.eventCategories.entrySet())
		{
			outputString.append( newline ).append( "## ").append(category.getKey()).append(" Events");
			for (MDEvent event : category.getValue())
				outputString.append( newline ).append( "on " ).append( event.name());
			outputString.append( newline);
		}
		return outputString.toString();
	}

	@Override
	public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren) {
		String[] words = line.line.split("\\s+");
		if (words.length == 0) return null;

		String word0 = words[0].toLowerCase();
		if (words.length == 1)
		{
			if (word0.equals("settings")) {
				return new SettingsLineHandler();
			}
		}
		else
		{
			if (word0.equals("on") && words.length == 2) {
				if (hasChildren) {
					MDEvent e = MDEvent.getEvent(words[1]);
					if (e == null) {
						LogUtil.warning(line, word0 + " " + words[1] + "is not valid. Possible that the event is not loaded!");
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
	public boolean isEnabled() {
		switch (ModDamage.getPluginConfiguration().getLoadMethod()) {
//			case MASTER_LIST:
			case ENABLED_SETTING:
				return super.isEnabled();
			case PRIORITY_PARSE:
				return getPriority() > 0;
			default:
				LogUtil.error(this, "The script had a missed load method... How did this happen?! Disabling the script!");
				return false;
		}

	}

	public ScriptLineHandler getSettingsHandler() {
		return new SettingsLineHandler();
	}

	private class SettingsLineHandler implements ScriptLineHandler
	{
		MDScript $this = MDScript.this;
		private Pattern settingPattern = Pattern.compile("\\s*([^=]+?)\\s*=\\s*(.*?)\\s*");

		@Override
		public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren) {
			Matcher m = settingPattern.matcher(line.line);
			if (!m.matches()) {
				LogUtil.error($this, "Invalid setting: \"" + line.line + "\"");
				return null;
			}

			String name = m.group(1).trim().toLowerCase().replaceAll("\\s+", "-");
			String value = m.group(2).trim();

			LogUtil.info_verbose($this, "setting: '"+name+"' = '"+value+"'");

			if (name.equals("debugging")) {
				try {
					getLogger().currentSetting = DebugSetting.valueOf(value.toUpperCase());
				}
				catch (IllegalArgumentException e) {
					LogUtil.error(line, "Bad debug level: " + value);
				}
			}
			else if (name.equals("log-file")) {
				logFile = new File(plugin.getDataFolder(), value);
			}
			else if (name.equals("append-logs")) {
				appendLog = Boolean.parseBoolean(value);
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
				LogUtil.error(line, "Unknown setting: " + m.group(1));
			}

			return null;
		}

		@Override
		public void done()
		{
		}
	}
}
