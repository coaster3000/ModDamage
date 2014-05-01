package com.ModDamage;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.ChatColor;

import com.ModDamage.Backend.Configuration.ScriptLine;

public class ModDamageLogger {
	
	static private ModDamageLogger singletonInstance = null;
	public static ModDamageLogger getInstance()
	{
		if(singletonInstance == null)
			singletonInstance = new ModDamageLogger();
		return singletonInstance;
	}
	
	public static enum DebugSetting
	{
		QUIET, NORMAL, CONSOLE, VERBOSE;
		public boolean shouldOutput(DebugSetting setting)
		{
			if(setting.ordinal() <= this.ordinal())
				return true;
			return false;
		}
	}

	public static enum OutputPreset
	{
		CONSOLE_ONLY(DebugSetting.CONSOLE, null, Level.INFO),
		CONSTANT(DebugSetting.QUIET, ChatColor.LIGHT_PURPLE, Level.INFO),
		FAILURE(DebugSetting.QUIET, ChatColor.RED, Level.SEVERE),
		INFO(DebugSetting.NORMAL, ChatColor.GREEN, Level.INFO),
		INFO_VERBOSE(DebugSetting.VERBOSE, ChatColor.AQUA, Level.INFO),
		WARNING(DebugSetting.VERBOSE, ChatColor.YELLOW, Level.WARNING),
		WARNING_STRONG(DebugSetting.NORMAL, ChatColor.YELLOW, Level.WARNING);
		
		protected final DebugSetting debugSetting;
		protected final ChatColor color;
		protected final Level level;
		private OutputPreset(DebugSetting debugSetting, ChatColor color, Level level)
		{
			this.debugSetting = debugSetting;
			this.color = color;
			this.level = level;
		}
	}

	
	public Level worstLogMessageLevel = Level.INFO;
	
	public Level getWorstLogMessageLevel() {
		return worstLogMessageLevel;
	}
	
	public void resetWorstLogMessageLevel() {
		worstLogMessageLevel = Level.INFO;
	}
	
	protected DebugSetting currentSetting = DebugSetting.VERBOSE;
	
	private FileHandler filehandle = null;
	
	private int indentation = 0;
	
	private final Logger logger;
	private File logFile;
	
	public int logMessagesSoFar = 0;
	public int maxLogMessagesToShow = 50;
	
	private Formatter formatter;
	
	public ModDamageLogger()
	{
		final ModDamage mdPlugin = ModDamage.getInstance();
		logger = mdPlugin.getLogger();
		formatter = new Formatter() {
			@Override
			public String format(LogRecord record) {
				StringBuilder b = new StringBuilder().append('[').append(mdPlugin.getName()).append("] [").append(String.format("%1$-10s", record.getLevel().toString())).append("] ");
				String name = mdPlugin.getDescription().getPrefix();
				if (name == null)
					name = mdPlugin.getName();
				
				String pat = "\\[" + name + "\\] ";
				b.append(String.format(record.getMessage().replaceFirst(pat, ""), record.getParameters())).append(ModDamagePluginConfiguration.newline).toString(); //StringBuilder is much more effecient then string concat.
				return b.toString();
			}
		};
	}
	
	public void addToLogRecord(OutputPreset preset, ScriptLine line, String message)
	{
		addToLogRecord(preset, line.lineNumber + ": " + message);
	}
	
	public void addToLogRecord(OutputPreset preset, String message)
	{
		if (worstLogMessageLevel == null || preset.level.intValue() > worstLogMessageLevel.intValue())
			worstLogMessageLevel = preset.level;

		if(getDebugSetting().shouldOutput(preset.debugSetting)) {
			if (getDebugSetting() != DebugSetting.QUIET || logMessagesSoFar < maxLogMessagesToShow) {
				String nestIndentation = "";
				for(int i = 0; i < indentation; i++)
					nestIndentation += "    ";
				
				logger.log(preset.level, nestIndentation + message);
			}
			logMessagesSoFar ++;
		}
	}
	
	public DebugSetting getDebugSetting(){ return currentSetting;}
	
	protected int getIndentation(){ return indentation;}
	
	public File getLogFile(){ return logFile; }
	
	public void changeIndentation(boolean inc)
	{
		if (inc)
			indentation++;
		else
			indentation--;
	}
	
	
	public String logPrepend(){ return "[" + ModDamage.getInstance().getDescription().getName() + "] "; }
	
	public void setDebugSetting(DebugSetting level)
	{
		if (level != null)
			currentSetting = level;
	}
	
	public void setLogFile(File file){ setLogFile(file, false); }
	public void setLogFile(File file, boolean append){ setLogFile(((file != null)?file.getPath():null), append); }
	
	public FileHandler craftFileHandler(String path, boolean append)
	{
		FileHandler fh = null;
		try {
			fh = new FileHandler(path, append);
			fh.setFormatter(formatter);
			return fh;
		} catch (IOException e)	{
			return null;
		}
	}
	
	public void setLogFile(String path, boolean append)
	{
		if (filehandle != null)
		{
			filehandle.flush();
			filehandle.close();
			logger.removeHandler(filehandle);
		
		}
		if (path != null)
		{	
			filehandle = craftFileHandler(path, append);
			if (filehandle != null)
				logger.addHandler(filehandle);
		}
	}
	
	
	public void resetLogCount(){
		logMessagesSoFar = 0;
	}

	public static void info(ScriptLine line, String message) { getInstance().addToLogRecord(OutputPreset.INFO, line, message); }
	public static void info_verbose(ScriptLine line, String message) { getInstance().addToLogRecord(OutputPreset.INFO_VERBOSE, line, message); }
	public static void warning(ScriptLine line, String message) { getInstance().addToLogRecord(OutputPreset.WARNING, line, message); }
	public static void warning_strong(ScriptLine line, String message) { getInstance().addToLogRecord(OutputPreset.WARNING_STRONG, line, message); }
	public static void error(ScriptLine line, String message) { getInstance().addToLogRecord(OutputPreset.FAILURE, line, message); }
	public static void constant(ScriptLine line, String message) { getInstance().addToLogRecord(OutputPreset.CONSTANT, line, message); }
	public static void console_only(ScriptLine line, String message) { getInstance().addToLogRecord(OutputPreset.CONSOLE_ONLY, line, message); }
	
	public static void info(String message) { getInstance().addToLogRecord(OutputPreset.INFO, message);	}
	public static void info_verbose(String message) { getInstance().addToLogRecord(OutputPreset.INFO_VERBOSE, message); }
	public static void warning(String message) { getInstance().addToLogRecord(OutputPreset.WARNING, message); }
	public static void warning_strong(String message) { getInstance().addToLogRecord(OutputPreset.WARNING_STRONG, message); }
	public static void error(String message) { getInstance().addToLogRecord(OutputPreset.FAILURE, message); }
	public static void constant(String message) { getInstance().addToLogRecord(OutputPreset.CONSTANT, ""); }
	public static void console_only(String message) { getInstance().addToLogRecord(OutputPreset.CONSOLE_ONLY, ""); }
	public static void printToLog(Level level, String message){ getInstance().logger.log(level, "[" + ModDamage.getInstance().getDescription().getName() + "] " + message); }
}
