package com.moddamage;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public interface Config extends LogHandle {
	static String newline = System.getProperty("line.separator");

	public static enum LoadState
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
	}

	/**
	 * Retrieves this config's current load state.
	 * @return {@link com.moddamage.Config.LoadState}
	 */
	public LoadState getLoadState();

	/**
	 * Reloads this config.
	 * @param reloadAll if true reloads all data.
	 * @return true if successful false otherwise.
	 */
	public boolean reload(boolean reloadAll);

	/**
	 * Gets the name of this config.
	 * @return String name
	 */
	public String getName();

	/**
	 * Retrieves the instance of the plugin used for the config.
	 * @return Plugin
	 */
	public Plugin getPlugin();
}
