package com.ModDamage.Backend.Minecraft.Events;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.ModDamage.ModDamage;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.SimpleEventInfo;

public class Init extends MDEvent
{
	private Init() { super(myInfo); }
	
	static final EventInfo myInfo = new SimpleEventInfo(
			Entity.class,	"entity",
			World.class,	"world");
	
	public static Init instance = new Init();
	
	public static void onInit(Entity entity)
	{
		if(!ModDamage.isEnabled) return;
		
		EventData data = myInfo.makeData(
				entity,
				entity.getWorld());
		
		instance.runRoutines(data);
	}
	
	public static void initAll()
	{
		if(!ModDamage.isEnabled) return;
		
		for (World world : Bukkit.getWorlds())
			for (Entity entity : world.getEntities())
				onInit(entity);
	}
}
