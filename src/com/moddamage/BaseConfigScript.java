package com.moddamage;

import java.io.File;

import org.bukkit.plugin.Plugin;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public abstract class BaseConfigScript extends BaseConfig implements ConfigScript {

	protected LoadState pluginState;
	private boolean enabled = true;
	private int priority = 0;

	public BaseConfigScript(Plugin plugin, File configFile, String name) {
		super(plugin, configFile, name);
	}

	protected final void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	protected final void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int compareTo(ConfigScript o) {
		if (o.isEnabled() && isEnabled()) {
			if (o.getPriority() > getPriority()) return -1;
			else if (o.getPriority() < getPriority()) return 1;
			else return o.getName().compareTo(getName());
		} else if (o.isEnabled()) return 1;
		else if (isEnabled()) return -1;
		else return 0;
	}
}
