package com.moddamage;

import org.apache.commons.lang.Validate;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.Plugin;

import com.moddamage.backend.Nullable;

public class ScriptManager {

	public static enum LoadMethod {
		ENABLED_SETTING, /*MASTER_LIST,*/ PRIORITY_PARSE
	}
	
	private final static FilenameFilter fileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File arg0, String arg1) {
			Matcher matcher = fileNamePattern.matcher(arg1);
			return matcher.matches() && matcher.group(1) == null && matcher.group(2) != null;
		}
	};

	public final static Pattern fileNamePattern = Pattern.compile("(config\\.mdscript)|(.*\\.mdscript)");
	
	private PluginConfiguration globalConfig;
	private Set<String> scriptOrderCached = new LinkedHashSet<String>(); //TODO: Replace with a sorted list system.
	private Map<String, ConfigScript> scripts = new HashMap<String, ConfigScript>();
	
	protected Collection<String> _getScriptNames() {
		return scriptOrderCached;
	}
	
	public void add(ConfigScript config) {
		Validate.notNull(config);

		if (config instanceof PluginConfiguration) {
			if (globalConfig == null) { //Ensure we don't already have one...
				globalConfig = (PluginConfiguration) config;
			} else {
				LogUtil.error("Global config already loaded!");
			}
		} else if (config instanceof MDScript) {
			MDScript script = (MDScript)config;
			scripts.put(script.getName(), script);
			update();
		} else
			LogUtil.printToLog(Level.SEVERE, "Error: ", new IllegalArgumentException("The script manager cannot handle stuff other than PluginConfiguration and MDScript objects." + System.lineSeparator() + "The supplied object is " + config.getClass().getSimpleName()));
	}

	/**
	 * Retrieves a script with the specified name.
	 * <p>
	 *     If the script does not exist, it will return null.
	 * </p>
	 * @param name of script
	 * @return script object, or null if non existent.
	 */
	public ConfigScript get(String name) {
		Validate.notEmpty(name);

		if (scripts.containsKey(name))
			return scripts.get(name);
		else
			return null;
	}

	protected List<ConfigScript> getAllScripts() {
		List<ConfigScript> ret = new ArrayList<ConfigScript>(scripts.values());
		Collections.sort(ret);

		return ret;
	}

	public List<ConfigScript> getDisabledScripts() {
		List<ConfigScript> ret = getAllScripts();
		for (Iterator<ConfigScript> iterator = ret.iterator(); iterator.hasNext();) {
			ConfigScript baseConfig = iterator.next();
			if (baseConfig.isEnabled())
				iterator.remove();
		}

		return ret;
	}

	public List<ConfigScript> getEnabledScripts() {
		List<ConfigScript> ret = getAllScripts();
		for (Iterator<ConfigScript> iterator = ret.iterator(); iterator.hasNext();) {
			ConfigScript baseConfig = iterator.next();
			if (!baseConfig.isEnabled())
				iterator.remove();
		}

		Collections.sort(ret);
		return ret;
	}

	public PluginConfiguration getMasterConfig() {
		if (globalConfig == null) add(new PluginConfiguration(ModDamage.getInstance()));
		return globalConfig;
	}

	public Collection<String> getScriptNames() {
		return Collections.unmodifiableCollection(scriptOrderCached);
	}

	private File[] listFilesRecursively(@Nullable FilenameFilter filter) {
		return listFilesRecursively(filter, ModDamage.getInstance().getDataFolder());
	}

	private void addFilesRecursively(FilenameFilter filter, List<File> files, File file) {
		if (file == null || files == null)
			return;

//		if (file.isDirectory()) {
//			if (ModDamage.getPluginConfiguration().isRecurseDirectortiesAllowed()) {
//				for (File dF : file.listFiles()) {
//					if (dF.isDirectory()) addFilesRecursively(filter, files, dF);
//					else files.add(dF); //Is isFile check needed?
//				}
//			} else {
//				for (File dF : file.listFiles(filter)) {
//					if (!dF.isDirectory()) files.add(dF);
//				}
//			}
//		}
		if (file.isDirectory() && file.listFiles() != null) {
			if (ModDamage.getPluginConfiguration().isRecurseDirectortiesAllowed()) {
				for (File dF : file.listFiles())
					addFilesRecursively(filter, files, dF);
			} else {
				for (File dF : file.listFiles(filter))
					if (!dF.isDirectory()) files.add(dF);
			}
		} else if (fileFilter.accept(null, file.getName())){
			files.add(file);
		} else {
			LogUtil.error("File failed match: " + file.getPath());
		}

	}

	private File[] listFilesRecursively(FilenameFilter filter, File file) {
		List<File> files = new ArrayList<File>();
		addFilesRecursively(filter, files, file);

		return files.toArray(new File[files.size()]);
	}

	public ConfigScript loadScript(Plugin plugin, File file, String name) {
		ConfigScript ret = get(name);

		if (name.endsWith(".mdscript")) // length of match is 9
			return loadScript(plugin, file, name.substring(0, name.length()-9)); // Minus the match plus one due to array logic. Arrays start at index 0.

		if (ret == null) {
			ret = new MDScript(plugin, file, name);
			ret.reload(false);

			add(ret);
		}

		return ret;
	}

	public ConfigScript loadScript(Plugin plugin, File file) {
		String name = file.getName();

		if (name.equals("config.mdscript")) return getMasterConfig();

		return loadScript(plugin, file, name);
	}

	public boolean reload(ConfigScript config, boolean reloadAll) {
		return config != null && config.reload(reloadAll);
	}
	
	
	public boolean reload(boolean reloadAll) {
		boolean ret = getMasterConfig().reload(reloadAll);
		for (String name : _getScriptNames())
			ret = (reload(name, reloadAll) && ret);
		
		return ret;
	}
	
	public boolean reload(String name, boolean reloadAll) {
		return reload(get(name), reloadAll);
	}
	
	public void remove(ConfigScript config) {
		Validate.notNull(config);
		Validate.isTrue(!(config instanceof PluginConfiguration), "Cannot remove the primary config!");

		scriptOrderCached.remove(config.getName());
		scripts.remove(config.getName());
	}
	
	public void remove(String name) {
		remove(get(name));
	}
	
	public void scanForScripts() {
		String strip = getMasterConfig().getScriptDirectory().getPath();
		for (File file : listFilesRecursively(fileFilter, getMasterConfig().getScriptDirectory())) {
			if (file.getName().equals("config.mdscript")) continue;
			loadScript(ModDamage.getInstance(), file, file.getPath().startsWith(strip) ? file.getPath().substring(strip.length()+1) : file.getName());
		}
	}
	
	private void update() {
		scriptOrderCached.clear();
		for (ConfigScript co : getAllScripts())
			scriptOrderCached.add(co.getName());
	}
}
