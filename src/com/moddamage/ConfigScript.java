package com.moddamage;

import com.moddamage.backend.ScriptLine;
import com.moddamage.backend.ScriptLineHandler;

public interface ConfigScript extends Config, ScriptLineHandler, Comparable<ConfigScript> {
	/**
	 * Retrieves the settings handler for this ConfigScript
	 * @return ScriptLineHandler
	 */
	public ScriptLineHandler getSettingsHandler();

	void addToLogRecord(MDLogger.OutputPreset preset, ScriptLine line, String message);

	int getPriority();

	boolean isEnabled();
}
