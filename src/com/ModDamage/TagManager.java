package com.ModDamage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.yaml.snakeyaml.Yaml;

import com.ModDamage.ModDamageConfigurationHandler.LoadState;
import com.ModDamage.Tags.TagsHolder;

public class TagManager
{
	//Singleton pattern code
	static private TagManager singletonInstance = null;
	public static TagManager getInstance()
	{
		if(singletonInstance == null)
			singletonInstance = new TagManager();
		return singletonInstance;
	}
	
	private boolean initialized = false;
	
	public static final int DEFAULT_SAVEINTERVAL = 200;	
	public static final File DEFAULT_FILE = new File(ModDamage.getInstance().getDataFolder(), "tags.yml");
	
    public final TagsHolder<Number> numTags = new TagsHolder<Number>();
	public final TagsHolder<String> stringTags = new TagsHolder<String>();
	
	private long saveInterval = DEFAULT_SAVEINTERVAL;
	private int saveTaskID;

	public File file = DEFAULT_FILE;
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File newFile;
	public File oldFile;
	private InputStream reader = null;
	private FileWriter writer = null;
	private Yaml yaml = new Yaml();

	public TagManager()
	{
		setCopyPaths();
	}
	
	public TagManager(File file, long saveInterval)
	{
		this.file = file;
		this.saveInterval = saveInterval;
		setCopyPaths();
	}
	
	/**
	 * Sets helper filepaths which are used in tag database collision cases
	 */
	private void setCopyPaths()
	{
		newFile = new File(file.getParent(), file.getName()+".new");
		oldFile = new File(file.getParent(), file.getName()+".old");
	}
	
	/**
	 * Enables the TagManager async task.
	 * @return True if already running.
	 */
	public boolean enable()
	{		
		if(!initialized)
		{
			loadFile();
			saveTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(ModDamage.getInstance(), new Runnable() {
				@Override public void run()
				{
					saveFile();
				}
			}, saveInterval, saveInterval);
			initialized = true;
		}
		return initialized;
	}
	
	private boolean isDirty = false;
    public void setDirtyState() { isDirty = true; }

    
	@SuppressWarnings("unchecked")//For YAML parsing.
	public void loadFile()
	{
		try
		{
			if(!file.exists())
			{
				ModDamageLogger.info("No tags file found at " + file.getAbsolutePath() + ", generating a new one...");
				if(!file.createNewFile())
				{
					ModDamageLogger.error("Couldn't make new tags file! Tags will not have persistence between reloads.");
					return;
				}
			}
			reader = new FileInputStream(file);
			Object tagFileObject = yaml.load(reader);
			reader.close();
			if(tagFileObject == null || !(tagFileObject instanceof Map)) return;
			
			Map<UUID, Entity> entities = new HashMap<UUID, Entity>();
			for(World world : Bukkit.getWorlds())
			{
				for (Entity entity : world.getEntities())
					if (!(entity instanceof OfflinePlayer))
						entities.put(entity.getUniqueId(), entity);
			}
			
			Map<String, Object> tagMap = (Map<String, Object>)tagFileObject;
			
			if (!tagMap.containsKey("string")) // Old style tags.yml
			{
				numTags.loadTags(tagMap, entities);
				saveFile(); // upgrade the file
			}
			else // New way
			{
				{
					if (tagMap.containsKey("int"))
						numTags.loadTags((Map<String, Object>) tagMap.get("int"), entities);
					else
						numTags.loadTags((Map<String, Object>) tagMap.get("num"), entities);
					stringTags.loadTags((Map<String, Object>) tagMap.get("string"), entities);
				}
			}
		}
		catch(Exception e){ ModDamageLogger.error("Error loading tags: "+e.toString()); }
	}
	
	
	/**
	 * Saves all tags to a file.
	 */
	public void saveFile()
	{
		if(file != null && isDirty)
		{
			Set<Entity> entities = new HashSet<Entity>();
			for (World world : Bukkit.getWorlds())
				entities.addAll(world.getEntities());

			
			Map<String, Object> saveMap = new HashMap<String, Object>();
			
			saveMap.put("tagsVersion", 2);
			saveMap.put("num", numTags.saveTags(entities));
			saveMap.put("string", stringTags.saveTags(entities));
			
			try
			{
				writer = new FileWriter(newFile);
				writer.write(yaml.dump(saveMap));
				writer.close();
			}
			catch (IOException e){
				ModDamageLogger.printToLog(Level.WARNING, "Error saving tags at " + newFile.getAbsolutePath() + "!");
				return;
			}
			
			oldFile.delete();
			file.renameTo(oldFile);
			newFile.renameTo(file);
		}
	}
	
	/**
	 * Flushes tags in memory.
	 */
	void disable()
	{
		if(initialized)
		{
			saveFile();
			if(initialized)
			{
				if(file != null)
				{
					if(saveTaskID != 0) Bukkit.getScheduler().cancelTask(saveTaskID);
				}
				initialized = false;
			}
		}
	}
	
	/**
	 * @return LoadState reflecting the file's load state.
	 */
	public LoadState getLoadState(){ return file != null? LoadState.SUCCESS : LoadState.NOT_LOADED; }

	public void clear()
	{
		numTags.clear();
		stringTags.clear();
	}
}