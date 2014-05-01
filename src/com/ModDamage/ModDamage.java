package com.ModDamage;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.ModDamage.ModDamageLogger.DebugSetting;
import com.ModDamage.ModDamageLogger.OutputPreset;
import com.ModDamage.ModDamagePluginConfiguration.LoadState;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.ScriptLine;
import com.ModDamage.Backend.Minecraft.Events.Command;
import com.ModDamage.Backend.Minecraft.Events.Init;
import com.ModDamage.Backend.Minecraft.Events.MDEvent;
import com.ModDamage.Backend.Minecraft.Events.Repeat;
import com.ModDamage.Backend.Minecraft.Magic.MagicStuff;
import com.ModDamage.External.ExtensionManager;
import com.ModDamage.External.ModDamageExtension;
import com.ModDamage.Server.MDServer;

/**
 * "ModDamage" for Bukkit
 * 
 * @authors Erich Gubler, Matt Peterson <ricochet1k@gmail.com>
 * 
 */
public class ModDamage extends JavaPlugin implements ModDamageExtension
{
	//singleton pattern code
	private static ModDamage instance;
	public static ModDamage getInstance()
	{
		return instance;
	}
	public ModDamage() { instance = this; }
	protected static ModDamagePluginConfiguration configuration;

	public static boolean isEnabled = false;
	private static final String errorString_Permissions = chatPrepend(ChatColor.RED) + "You don't have access to that command.";
	

	// //////////////////////// INITIALIZATION
	@Override
	public void onLoad() {
		super.onLoad(); //Just in case bukkit loads stuff in here.
		configuration = new ModDamagePluginConfiguration(); //Fixes NPE on registering extensions from onLoad in other plugins.
	}

	@Override
	public void onEnable()
	{
		PluginCommand.setPlugin(this);
		isEnabled = true;
		MagicStuff.init();
		reload(true);
		
		try
		{
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} 
		catch (IOException e) 
		{
		    // Failed to submit the stats :-(
		}
	}

	@Override
	public void onDisable()
	{
		MDServer.stopServer();
		Command.instance.reset();
		Repeat.instance.reset();
		MDEvent.unregisterEvents();
		
		TagManager.getInstance().disable();
		isEnabled = false;
		ModDamageLogger.getInstance().setLogFile(null); //Cleanup locks.
		ModDamageLogger.info("Disabled.");
		PluginCommand.setPlugin(null); //Prevents possible memory leaks on /reload command
	}

	public void reload(boolean reloadingAll)
	{
		if((configuration.reload(reloadingAll) && reloadingAll))
		{
			TagManager.getInstance().disable();
			TagManager.getInstance().enable();
		}

        Init.initAll();
	}

	// //COMMAND PARSING ////
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{
		Player player = ((sender instanceof Player) ? ((Player) sender) : null);
		if(args.length == 0) sendCommandUsage(player, false);
		else if(args.length >= 0)
		{
			String commandString = "";
			for(String arg : args)
				commandString += " " + arg;
			PluginCommand.handleCommand(player, commandString);
		}
		return true;
	}

	private enum PluginCommand
	{
		DEBUG(false, "\\sd(?:ebug)?(?:\\s(\\w+))?", "/md (debug | d) [debugType] - change debug type")
		{
			@Override
			protected void handleCommand(Player player, Matcher matcher)
			{
				if(matcher.group(1) != null)
				{
					for(DebugSetting setting : DebugSetting.values())
						if(matcher.group(1).equalsIgnoreCase(setting.name()))
						{
							configuration.setDebugging(player, setting);
							return;
						}
					sendMessage(player, "Invalid debugging mode \"" + matcher.group(1).substring(1) + "\" - modes are \"quiet\", \"normal\", and \"verbose\".", ChatColor.RED);
				}
				else configuration.toggleDebugging(player);
			}
		},
		RELOAD(false, "\\sr(?:eload)?(\\sall)?", "/md (reload | r) [all] - reload configuration.")
		{
			@Override
			protected void handleCommand(Player player, Matcher matcher)
			{
				boolean reloadingAll = matcher.group(1) != null;
				if(player != null) ModDamageLogger.printToLog(Level.INFO, "Reload initiated by user " + player.getName() + "...");
				plugin.reload(reloadingAll);
				if(player != null)
					switch(LoadState.pluginState)
					{
						case SUCCESS:
							int worstValue = ModDamageLogger.getInstance().getWorstLogMessageLevel().intValue();
							
							if (worstValue >= Level.SEVERE.intValue()) {
								player.sendMessage(chatPrepend(ChatColor.YELLOW) + "Reloaded with errors.");
							}
							else if (worstValue >= Level.WARNING.intValue()) {
								player.sendMessage(chatPrepend(ChatColor.YELLOW) + "Reloaded with warnings.");
							}
							else if (worstValue >= Level.INFO.intValue()) {
								player.sendMessage(chatPrepend(ChatColor.GREEN) + "Reloaded!");
							}
							else {
								player.sendMessage(chatPrepend(ChatColor.YELLOW) + "Weird reload: " + ModDamageLogger.getInstance().getWorstLogMessageLevel());
							}
							
							break;
						case FAILURE:
							player.sendMessage(chatPrepend(ChatColor.YELLOW) + "Reloaded with errors.");
							break;
						case NOT_LOADED:
							player.sendMessage(chatPrepend(ChatColor.GRAY) + "No configuration loaded! Are any routines defined?");
							break;
							
						default: throw new Error("Unknown state: "+LoadState.pluginState+" $MD176");
					}
			}
		},
		STATUS(false, "\\s(?:en|dis)able", "/md (disable|enable) - disable/enable ModDamage")
		{
			@Override
			protected void handleCommand(Player player, Matcher matcher)
			{
				ModDamage.setPluginStatus(player, matcher.group().equalsIgnoreCase(" enable"));
			}
		},
		TAGS(true, "\\st(?:ags)?\\s(clear|save)", "/md tags (save|clear) - save/clear tags")
		{
			@Override
			protected void handleCommand(Player player, Matcher matcher)
			{
				if(matcher.group(1).equalsIgnoreCase("clear"))
				{
					TagManager.getInstance().clear();
					sendMessage(player, "Tags cleared.", ChatColor.GREEN);
				}
				else
				{
					TagManager.getInstance().saveFile();;
					sendMessage(player, "Tags saved.", ChatColor.GREEN);
				}
			}
		};
		
		private final boolean needsEnable;
		private final Pattern pattern;
		protected final String help;

		private PluginCommand(boolean needsEnable, String pattern, String help)
		{
			this.needsEnable = needsEnable;
			this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			this.help = help;
		}

		public static void handleCommand(Player player, String commandString)
		{
			for(PluginCommand command : PluginCommand.values())
			{
				Matcher matcher = command.pattern.matcher(commandString);
				if(matcher.matches() && hasPermission(player, "moddamage." + command.name().toLowerCase()))
				{
					if(!command.needsEnable || isEnabled)
						command.handleCommand(player, matcher);
					else sendMessage(player, "ModDamage must be enabled to use that command.", ChatColor.RED);
					return;
				}
			}
			sendCommandUsage(player, true);
		}

		abstract protected void handleCommand(Player player, Matcher matcher);

		private static ModDamage plugin;
		protected static void setPlugin(ModDamage plugin){ PluginCommand.plugin = plugin; }
	}

	private static boolean hasPermission(Player player, String permission)
	{
		if (player == null) return true; // console
		
		boolean has = player.hasPermission(permission);
		if(!has) player.sendMessage(errorString_Permissions);
		return has;
	}

	static void sendMessage(Player player, String message, ChatColor color)
	{
		if(player != null)
			player.sendMessage(chatPrepend(color) + message);
		else ModDamageLogger.info(message);
	}

	static String chatPrepend(ChatColor color){ return color + "[" + ChatColor.DARK_RED + "Mod" + ChatColor.DARK_BLUE + "Damage" + color + "] "; }

	private static void setPluginStatus(Player player, boolean status)
	{
		if(status != isEnabled)
		{
			isEnabled = status;
			ModDamageLogger.info("Plugin " + (isEnabled ? "en" : "dis") + "abled.");
			if(player != null)
				player.sendMessage(chatPrepend(ChatColor.GREEN) + "Plugin " + (isEnabled ? "en" : "dis") + "abled.");
		}
		else sendMessage(player, "Already " + (isEnabled ? "en" : "dis") + "abled!", ChatColor.RED);
	}

	private static void sendCommandUsage(Player player, boolean forError)
	{
		if(player != null)
		{
			if(forError) player.sendMessage(ChatColor.RED + "Error: invalid command syntax.");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "ModDamage commands:");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "[] optional | () required");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "/moddamage | /md - bring up this help message");
			for (PluginCommand cmd:PluginCommand.values())
				player.sendMessage(ChatColor.LIGHT_PURPLE +cmd.help);
		}
		else
		{
			StringBuffer sb = new StringBuffer().append("ModDamage commands:\n").append("/moddamage | /md - bring up this help message");
			for (PluginCommand cmd:PluginCommand.values())
				sb.append("\n").append(cmd.help);
			if(forError) ModDamageLogger.printToLog(Level.SEVERE, "Error: invalid command syntax.");
			ModDamageLogger.printToLog(Level.INFO, sb.toString());
		}
	}

///////////////// HELPER FUNCTIONS
	public static void addToLogRecord(OutputPreset preset, String message){ ModDamageLogger.getInstance().addToLogRecord(preset, message); }
	public static void addToLogRecord(OutputPreset preset, ScriptLine line, String message){ ModDamageLogger.getInstance().addToLogRecord(preset, line, message); }
	
	public static void changeIndentation(boolean forward)
	{
		ModDamageLogger.getInstance().changeIndentation(forward);
	}
	
	public static void printToLog(Level level, String message) {
		ModDamageLogger.printToLog(level, message);
	}

	public static final HashSet<Material> goThroughThese = new HashSet<Material>(Arrays.asList(
			Material.AIR,
			Material.GLASS,
			Material.LADDER,
			Material.TORCH,
			Material.REDSTONE_TORCH_ON,
			Material.REDSTONE_TORCH_OFF,
			Material.STONE_BUTTON,
			Material.SIGN_POST,
			Material.WALL_SIGN,
			Material.FIRE,
			Material.LEVER));
	
///////////////// EXTERNAL PLUGINS

	public interface ModDamageExtension
	{
		public PluginDescriptionFile getDescription(); // For reload data purpose
		public void reloadRoutines();//Register routines with the Routine/NestedRoutine libraries.
	}
	
	public static void registerExtension(ModDamageExtension extension)
	{
		ExtensionManager.registerExtension(extension);
	}

	public static void reportBailException(BailException bailException)
	{
		if (!bailException.suppress)
		{
			System.err.println("A serious error has occurred in ModDamage:\n"+bailException.toString());
			System.err.println("Please report this error.");
		}
	}

}