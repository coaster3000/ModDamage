package com.ModDamage.Backend.Configuration.Alias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ModDamage.ModDamageLogger;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Configuration.ScriptLineHandler;
import com.ModDamage.Backend.Configuration.Alias.RoutineAliaser.ScriptCapturedLines;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.RoutineList;

public class RoutineAliaser extends Aliaser<ScriptCapturedLines, ScriptCapturedLines>
{
	public static RoutineAliaser aliaser = new RoutineAliaser();
	public static RoutineList match(String string, EventInfo info) { return aliaser.matchAlias(string, info); }
	
	public RoutineAliaser() { super("Routine"); }
	
	
	public static class ScriptCapturedLines implements ScriptLineHandler
	{
		public List<ScriptCapturedLine> children;

		@Override
		public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
		{
			if (children == null)
				children = new ArrayList<RoutineAliaser.ScriptCapturedLine>();
			ScriptCapturedLine child = new ScriptCapturedLine(line);
			children.add(child);
			return child.children;
		}

		@Override
		public void done() { }
		
		public void parse(ScriptLineHandler lineHandler)
		{
			for (ScriptCapturedLine child : children)
			{
				ScriptLineHandler nestedLineHandler = lineHandler.handleLine(child.scriptLine, child.children != null);
				if (nestedLineHandler == null) throw new IllegalArgumentException("nestedLineHandler cannot be null: " + lineHandler);
				
				if (child.children != null) {
					child.children.parse(nestedLineHandler);
				}
				nestedLineHandler.done();
			}
		}
	}
	
	public static class ScriptCapturedLine
	{
		public ScriptLine scriptLine;
		public ScriptCapturedLines children = new ScriptCapturedLines();
		
		public ScriptCapturedLine(ScriptLine scriptLine)
		{
			this.scriptLine = scriptLine;
		}
	}
	
	@Override
	public ScriptLineHandler handleLine(final ScriptLine nameLine, boolean hasChildren)
	{
		return new ScriptLineHandler() {
			ScriptCapturedLines lines = new ScriptCapturedLines();
			boolean hasValue;
			
			@Override
			public ScriptLineHandler handleLine(ScriptLine line, boolean hasChildren)
			{
				hasValue = true;
				return lines.handleLine(nameLine, hasChildren);
			}
			
			@Override
			public void done()
			{
				if (!hasValue) {
					ModDamageLogger.error(nameLine, name+" alias _"+nameLine.line+" has no RoutineList.");
					return;
				}
				putAlias("_"+nameLine.line, lines);
			}
		};
	}
	
	private static boolean isParsingAlias = false;
	public static boolean isParsingAlias() { return isParsingAlias; }
	private static List<Runnable> runWhenDone = new ArrayList<Runnable>();
	
	public static void whenDoneParsingAlias(Runnable runnable) {
		if (isParsingAlias) runWhenDone.add(runnable);
		else runnable.run();
	}
	
	public final Map<InfoOtherPair<String>, RoutineList> aliasedRoutineList = new HashMap<InfoOtherPair<String>, RoutineList>();
	public RoutineList matchAlias(String alias, EventInfo info)
	{
		InfoOtherPair<String> infoPair = new InfoOtherPair<String>(alias, info);
		if (aliasedRoutineList.containsKey(infoPair)) return aliasedRoutineList.get(infoPair);
		
		
		ScriptCapturedLines lines = getAlias(alias);
		if (lines == null)
		{
			ModDamageLogger.error("Unknown alias: \"" + alias + "\"");
			return null;
		}
		ModDamageLogger.info("RoutineList in " + alias);
		
		isParsingAlias = true;
		
		RoutineList RoutineList = new RoutineList();
		ScriptLineHandler RoutineListLineHandler = RoutineList.getLineHandler(info);
		lines.parse(RoutineListLineHandler);
		RoutineListLineHandler.done();
		
		isParsingAlias = false;
		
		aliasedRoutineList.put(infoPair, RoutineList);

		if (!runWhenDone.isEmpty())
		{
			List<Runnable> toRun = runWhenDone;
			runWhenDone = new ArrayList<Runnable>();
			for (Runnable runnable : toRun)
				runnable.run();
		}
		return RoutineList;
	}
	
	@Override
	public void clear()
	{
		super.clear();
		aliasedRoutineList.clear();
	}
}