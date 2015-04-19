package com.moddamage;

import java.io.File;

import org.bukkit.plugin.Plugin;

public abstract class BaseConfigScript extends BaseConfig implements ConfigScript {

	protected LoadState pluginState;

	public BaseConfigScript(Plugin plugin,String name, File configFile) {
		super(plugin,name, configFile);
	}
}
