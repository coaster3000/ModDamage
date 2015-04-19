package com.moddamage;

import java.util.logging.Level;

import com.moddamage.MDLogger.OutputPreset;
import com.moddamage.backend.ScriptLine;

// Helper utility for Log Files.
public class LogUtil {

	//Instanced Log Helpers
	public static void info(LogHandle logHandle, ScriptLine line, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.INFO, line, message); }
	public static void info_verbose(LogHandle logHandle, ScriptLine line, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.INFO_VERBOSE, line, message); }
	public static void warning(LogHandle logHandle, ScriptLine line, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.WARNING, line, message); }
	public static void warning_strong(LogHandle logHandle, ScriptLine line, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.WARNING_STRONG, line, message); }
	public static void error(LogHandle logHandle, ScriptLine line, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.FAILURE, line, message); }
	public static void constant(LogHandle logHandle, ScriptLine line, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.CONSTANT, line, message); }
	public static void console_only(LogHandle logHandle, ScriptLine line, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.CONSOLE_ONLY, line, message); }

	public static void info(LogHandle logHandle, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.INFO, message);	}
	public static void info_verbose(LogHandle logHandle, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.INFO_VERBOSE, message); }
	public static void warning(LogHandle logHandle, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.WARNING, message); }
	public static void warning_strong(LogHandle logHandle, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.WARNING_STRONG, message); }
	public static void error(LogHandle logHandle, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.FAILURE, message); }
	public static void constant(LogHandle logHandle, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.CONSTANT, ""); }
	public static void console_only(LogHandle logHandle, String message) { logHandle.getLogger().addToLogRecord(OutputPreset.CONSOLE_ONLY, ""); }
	public static void printToLog(LogHandle logHandle, Level level, String message) { logHandle.getLogger().printToLog(level, message); }
	public static void printToLog(LogHandle logHandle, Level level, String message, Throwable thrown) { logHandle.getLogger().printToLog(level, message, thrown); }

	//Global Log Helpers
	public static void info(ScriptLine line, String message) { info(line.origin, line, message); }
	public static void info_verbose(ScriptLine line, String message) { info_verbose(line.origin, line, message); }
	public static void warning(ScriptLine line, String message) { warning(line.origin, line, message); }
	public static void warning_strong(ScriptLine line, String message) { warning_strong(line.origin, line, message); }
	public static void error(ScriptLine line, String message) { error(line.origin, line, message); }
	public static void constant(ScriptLine line, String message) { constant(line.origin, line, message); }
	public static void console_only(ScriptLine line, String message) { console_only(line.origin, line, message); }

	@Deprecated
	public static void info(String message) { ModDamage.addToLogRecord(OutputPreset.INFO, message);	}
	@Deprecated
	public static void info_verbose(String message) { ModDamage.addToLogRecord(OutputPreset.INFO_VERBOSE, message); }
	@Deprecated
	public static void warning(String message) { ModDamage.addToLogRecord(OutputPreset.WARNING, message); }
	@Deprecated
	public static void warning_strong(String message) { ModDamage.addToLogRecord(OutputPreset.WARNING_STRONG, message); }
	@Deprecated
	public static void error(String message) { ModDamage.addToLogRecord(OutputPreset.FAILURE, message); }
	@Deprecated
	public static void constant(String message) { ModDamage.addToLogRecord(OutputPreset.CONSTANT, ""); }
	@Deprecated
	public static void console_only(String message) { ModDamage.addToLogRecord(OutputPreset.CONSOLE_ONLY, ""); }
	@Deprecated
	public static void printToLog(Level level, String message) { ModDamage.printToLog(level, message); }
	@Deprecated
	public static void printToLog(Level level, String message, Throwable thrown) { ModDamage.printToLog(level, message, thrown); }

}
