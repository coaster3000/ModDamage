package com.moddamage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.moddamage.backend.ScriptLine;

public abstract class BaseConfig implements Config {

	public final Plugin plugin;
	protected final File configFile;
	private final String name;
	protected MDLogger log;
	boolean appendLog;
	File logFile;

	public BaseConfig(Plugin plugin, File configFile, String name) {
		this.plugin = plugin;
		this.configFile = configFile;
		this.name = name;
		log = new MDLogger(this);
	}

	protected boolean writeDefaults()
	{
		addToLogRecord(MDLogger.OutputPreset.INFO, getLogger().logPrepend() + "No configuration file found! Writing a blank config in " + getName() + "...");
		if(!configFile.exists())
		{
			try
			{
				if(!(configFile.getParentFile().exists() || configFile.getParentFile().mkdirs()) || !configFile.createNewFile())
				{
					printToLog(Level.SEVERE, "Fatal error: could not create " + getName() + ".");
					return false;
				}
			}
			catch (IOException e)
			{
				printToLog(Level.SEVERE, "Error: could not create new " + getName() + ".");
				e.printStackTrace();
				return false;
			}
		}
		String outputString = getDefaultFileContents();
		try
		{
			Writer writer = new FileWriter(configFile);
			writer.write(outputString);
			writer.close();

		}
		catch (IOException e)
		{
			printToLog(Level.SEVERE, "Error writing to " + getName() + ".");
		}

		printToLog(Level.INFO, "Completed auto-generation of " + getName() + ".");
		return true;
	}

	protected abstract String getDefaultFileContents();

	public static boolean replaceOrAppendInFile(File file, String targetRegex, String replaceString)
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

	public String getName() {
		return name;
	}

	public MDLogger getLogger() { return log; }

	public Plugin getPlugin() {
		return plugin;
	}

	@Override
	public void toggleDebugging(Player player)
	{
		switch(getDebugSetting())
		{
			case QUIET:
				setDebugging(player, MDLogger.DebugSetting.NORMAL);
				break;
			case NORMAL:
				setDebugging(player, MDLogger.DebugSetting.VERBOSE);
				break;
			case VERBOSE:
				setDebugging(player, MDLogger.DebugSetting.QUIET);
				break;
		}
	}

	@Override
	public void setDebugging(Player player, MDLogger.DebugSetting setting)
	{
		if(setting != null)
		{
			if(!getDebugSetting().equals(setting))
			{
				if(replaceOrAppendInFile(configFile, "debugging:.*", "debugging: " + setting.name().toLowerCase()))
				{
					ModDamage.sendMessage(player, "Changed debug from " + getDebugSetting().name().toLowerCase() + " to " + setting.name().toLowerCase(), ChatColor.GREEN);
					log.setDebugSetting(setting);
				}
				else if(player != null)
					player.sendMessage(ModDamage.chatPrepend(ChatColor.RED) + "Couldn't save changes to " + PluginConfiguration.configString_defaultConfigPath + ".");
			}
			else ModDamage.sendMessage(player, "Debug already set to " + setting.name().toLowerCase() + "!", ChatColor.RED);
		}
		else printToLog(Level.SEVERE, "Error: bad debug setting sent. Valid settings: normal, quiet, verbose");// shouldn't																								// happen
	}

	public void addToLogRecord(MDLogger.OutputPreset preset, ScriptLine line, String message) { log.addToLogRecord(preset, line.lineNumber + ": " + message); }

	@Override
	public void addToLogRecord(MDLogger.OutputPreset preset, String message) { log.addToLogRecord(preset, message); }

	@Override
	public MDLogger.DebugSetting getDebugSetting() { return log.getDebugSetting(); }

	@Override
	public void resetWorstLogMessageLevel() { log.resetWorstLogMessageLevel(); }

	@Override
	public void resetLoggedMessages() { log.resetLogCount(); }

	@Override
	public void printToLog(Level level, String message){ log.printToLog(level, message); }
	public void printToLog(Level level, String message, Throwable thrown){ log.printToLog(level, message, thrown); }

	@Override
	public void changeIndentation(boolean forward) { log.changeIndentation(forward); }

	@Override
	public Level getWorstLogMessageLevel() {
		return log.worstLogMessageLevel;
	}
}
