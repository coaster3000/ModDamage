package com.ModDamage.Backend.Configuration;

public interface ScriptLineHandler
{
	public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren);
	public void done();
}
