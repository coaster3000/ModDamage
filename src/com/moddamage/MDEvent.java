package com.moddamage;

import com.google.common.collect.MapMaker;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.moddamage.Config.LoadState;
import com.moddamage.backend.BailException;
import com.moddamage.backend.EventFinishedListener;
import com.moddamage.backend.ScriptLineHandler;
import com.moddamage.eventinfo.EventData;
import com.moddamage.eventinfo.EventInfo;
import com.moddamage.events.Command;
import com.moddamage.events.Init;
import com.moddamage.events.Repeat;
import com.moddamage.events.block.*;
import com.moddamage.events.chunk.ChunkLoad;
import com.moddamage.events.chunk.ChunkPopulate;
import com.moddamage.events.chunk.ChunkUnload;
import com.moddamage.events.entity.*;
import com.moddamage.events.inventory.*;
import com.moddamage.events.item.*;
import com.moddamage.events.player.*;
import com.moddamage.events.weather.LightingStrike;
import com.moddamage.events.weather.ThunderChange;
import com.moddamage.events.weather.WeatherChange;
import com.moddamage.events.world.StructureGrow;
import com.moddamage.routines.Routines;

public class MDEvent implements Listener
{
	public final static Map<String, MDEvent> allEvents = new HashMap<String, MDEvent>();
	public final static Map<String, List<MDEvent>> eventCategories = new HashMap<String, List<MDEvent>>();
	
	public static void registerVanillaEvents()
	{
		if (!eventCategories.isEmpty())
		{
			for(Entry<String, List<MDEvent>> entries: eventCategories.entrySet())
				if (entries.getValue() != null && entries.getValue().size() > 0)
					for(MDEvent event: entries.getValue())
						HandlerList.unregisterAll(event);
			
			eventCategories.clear();
		}
		
		addEvents("Block",
                new BlockBurn(),
                new BlockFade(),
                new BlockFlow(),
                new BlockForm(),
                new BlockGrow(),
                new BlockDispense(),
                new BlockIgnite(),
                new BlockSpread(),
                new SignChange(),
				new BreakBlock(),
				new PlaceBlock(),
                new LeavesDecay(),
                new FurnaceExtract(),
                new BlockPhysics()
				);
		
		addEvents("Chunk",
                new ChunkLoad(),
                new ChunkPopulate(),
                new ChunkUnload()
				);
		
		addEvents("Entity",
				new Combust(),
				new Damage(),
				new Death(),
				new Explode(),
				new Heal(),
				new HorseJump(),
				new ProjectileHit(),
				new ProjectileLaunch(),
				new ShootBow(),
				new Spawn(),
				new Tame(),
				new Target(),
                new Teleport()
				);

		addEvents("Inventory",
				new InventoryOpen(),
				new InventoryClose(),
				new InventoryClick(),
				new Craft(),
				new PrepareCraft()
				);

		addEvents("Item",
				new DropItem(),
				new PickupItem(),
				new ItemHeld(),
				new Enchant(),
				new PrepareEnchant()
				);

		addEvents("Player",
				new AsyncChat(),
				new Chat(),
				new Consume(),
				new Interact(),
				new InteractEntity(),
				new Join(),
				new Kick(),
				new LevelChange(),
				new Login(),
				new PickupExp(),
				new Quit(),
				new ToggleFlight(),
				new ToggleSneak(),
				new ToggleSprint(),
                new Fish()
				);

		addEvents("World",
				new StructureGrow()
				);
		
		addEvents("Weather",
				new LightingStrike(),
				new ThunderChange(),
				new WeatherChange());

		addEvents("Misc",
				Init.instance,
				Command.instance,
				Repeat.instance
				);
	}
	
	public static boolean disableDeathMessages = false;
	public static boolean disableJoinMessages = false;
	public static boolean disableQuitMessages = false;
	public static boolean disableKickMessages = false;
	
	public static void addEvents(String category, MDEvent... eventsArray)
	{
		addEvents(category, Arrays.asList(eventsArray));
	}
	
	public static void addEvents(String category, Collection<MDEvent> events)
	{
		List<MDEvent> newEvents = new ArrayList<MDEvent>(events);
		if (eventCategories.containsKey(category) && eventCategories.get(category) != null) 
		{
			List<MDEvent> oldEvents = eventCategories.get(category);
			newEvents = new ArrayList<MDEvent>(newEvents);
			newEvents.addAll(0, oldEvents);
		}

		eventCategories.put(category, newEvents);
		
		for (MDEvent event : newEvents)
			allEvents.put(event.name(), event);
	}
	
	public static void addEvent(String category, MDEvent event)
	{
		addEvents(category, event);
	}
	
	protected EventInfo myInfo;
	public EventInfo getInfo() { return myInfo; }
	
	protected MDEvent(EventInfo myInfo)
	{
		this.myInfo = myInfo;
	}
	
	public void runRoutines(EventData data)
	{
		try
		{
			for (Reference<Routines> routines : routines_cached) {
				Routines r;
				if (routines != null && (r = routines.get()) != null) {
					r.run(data); //FIXME: Variable bleeds through scripts...
					return;
				}
			}
			eventFinished(true);
		}
		catch (BailException e)
		{
			ModDamage.reportBailException(e);
		}
        eventFinished(false);
	}
	protected Map<String, Routines> routines = new MapMaker().makeMap();
	protected List<Reference<Routines>> routines_cached = new LinkedList<Reference<Routines>>();
	
	protected Map<String, LoadState> loadStates = new HashMap<String, LoadState>();
	private static Map<String, LoadState> combinedLoadStates = new HashMap<String, LoadState>();
	
	public static LoadState getCombinedLoadStates(BaseConfig config) {
		return combinedLoadStates.containsKey(config.getName())? combinedLoadStates.get(config.getName()) : LoadState.NOT_LOADED;
	}
	
	protected static void setCombinedLoadState(BaseConfig config, LoadState state) {
		combinedLoadStates.put(config.getName(), state);
	}
	
	public LoadState getState(BaseConfig config){ 
			return (loadStates.containsKey(config.getName())) ? loadStates.get(config.getName()) : LoadState.NOT_LOADED;
	}
	
	public String name() { return this.getClass().getSimpleName(); }
	

	public ScriptLineHandler getLineHandler(ConfigScript config)
	{
		String name = config.getName();

		if (config.isEnabled() && !routines.containsKey(name)) {
			Routines newRoutines = new Routines(config);
			routines.put(name, newRoutines);
			routines_cached.add(new WeakReference<Routines>(newRoutines));
		} else if (!config.isEnabled()) {
			if (routines.containsKey(name)) routines.remove(name);
			return null;
		}

		LogUtil.info(config, "on " + name());
		
		loadStates.put(name, LoadState.SUCCESS);
		if (combinedLoadStates.containsKey(name))
			combinedLoadStates.put(name, LoadState.combineStates(combinedLoadStates.get(name), loadStates.get(name)));
		else
			combinedLoadStates.put(name, loadStates.get(name));	
		
		return routines.get(name).getLineHandler(myInfo);
	}
	
	
	public static MDEvent getEvent(String name)
	{
		return allEvents.get(name);
	}
	public static void registerEvents()
	{
		for (Entry<String, MDEvent> entry : allEvents.entrySet()) {
			if (entry.getValue().routines != null && !entry.getValue().routines.isEmpty()) {
				boolean found = false;
				for (Routines r : entry.getValue().routines.values()) //Must iterate to make sure routines exist.
					if (!r.isEmpty()) {
						found = true;
						break;
					}
				if (found)
					Bukkit.getPluginManager().registerEvents(entry.getValue(), ModDamage.getInstance());
				else //Remove unused stuff
					HandlerList.unregisterAll(entry.getValue());
			}
		}
	}

	public static void unregisterEvents()
	{
		for (Entry<String, MDEvent> entry : allEvents.entrySet()) {
			HandlerList.unregisterAll(entry.getValue());
		}
	}
	
	public static void clearEvents(ConfigScript script)
	{
		for (Entry<String, MDEvent> entry : allEvents.entrySet()) {
			MDEvent event = entry.getValue();
			if (event.routines == null) continue;

			Routines routines = event.routines.get(script.getName());
			if (routines == null) continue;

			routines.clear();
		}
	}

    private static List<EventFinishedListener> whenEventFinishesList = new ArrayList<EventFinishedListener>();

    public static void whenEventFinishes(EventFinishedListener task) {
        whenEventFinishesList.add(task);
    }

    private static void eventFinished(boolean success) {
        for (EventFinishedListener task : whenEventFinishesList) {
            try {
                task.eventFinished(success);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        whenEventFinishesList.clear();
    }


};