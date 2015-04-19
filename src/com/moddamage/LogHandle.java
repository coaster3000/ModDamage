package com.moddamage;

import java.util.logging.Level;

import org.bukkit.entity.Player;

public interface LogHandle {
	/**
	 * Toggles the debug level. <br/>
	 * This will also message the player
	 * <p>
	 *     If player is null it will instead message the console through logs.
	 * </p>
	 * @param player to message
	 */
	void toggleDebugging(Player player);

	/**
	 * Retrieves the current debug setting for the logger.
	 * @return DebugSetting
	 */
	MDLogger.DebugSetting getDebugSetting();

	/**
	 * Sets the debug setting to the setting provided in arguments. <br/>
	 * Will also message the player that is specified of the change.
	 * <p>
	 *     If player is null it will instead message the console.
	 * </p>
	 * @param player to message
	 * @param setting to change to
	 */
	void setDebugging(Player player, MDLogger.DebugSetting setting);

	/**
	 * Adds a new message to the log with the specified output preset
	 * @param preset to output as
	 * @param message to log
	 */
	void addToLogRecord(MDLogger.OutputPreset preset, String message);

	/**
	 * Reset's the worst log message level.
	 */
	void resetWorstLogMessageLevel();

	/**
	 * Reset's the log message count.
	 */
	void resetLoggedMessages();

	/**
	 * Prints a message straight to the log without a preset.
	 * @param level to log as
	 * @param message to log
	 */
	void printToLog(Level level, String message);
	/**
	 * Prints a message straight to the log without a preset.
	 * @param level to log as
	 * @param message to log
	 * @param thrown thrown error
	 */
	void printToLog(Level level, String message, Throwable thrown);

	/**
	 * Change's the indentation level of the log output.
	 * <p>
	 *     if forward is true it will increase indentation.<br/>
	 *     Otherwise it will decrease
	 * </p>
	 * @param forward move up indention or back.
	 */
	void changeIndentation(boolean forward);

	/**
	 * Retrieves the worst log level that has been used since last {@link #resetWorstLogMessageLevel()}
	 * @return {@link java.util.logging.Level}
	 */
	Level getWorstLogMessageLevel();

	/**
	 * Retrieve the logger associated with this config.
	 * @return {@link MDLogger} instance.
	 */
	MDLogger getLogger();
}
