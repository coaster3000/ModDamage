package com.ModDamage.Backend.Minecraft.Events.Entity;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.ModDamage.ModDamage;
import com.ModDamage.Backend.Minecraft.Events.MDEvent;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.SimpleEventInfo;

public class ProjectileLaunch extends MDEvent implements Listener
{
	public ProjectileLaunch() { super(myInfo); }
	
	static final EventInfo myInfo = new SimpleEventInfo(
			Entity.class, 		"shooter", "entity",
			Projectile.class,	"projectile",
			World.class,		"world",
			Boolean.class, 		"cancelled");
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onProjectileLaunch(ProjectileLaunchEvent event)
	{
		if(!ModDamage.isEnabled) return;
		
		Projectile projectile = (Projectile)event.getEntity();
		LivingEntity shooter = projectile.getShooter();
		
		EventData data = myInfo.makeData(
				shooter,
				projectile,
				projectile.getWorld(),
				event.isCancelled());
		
		runRoutines(data);
		
		event.setCancelled(data.get(Boolean.class, data.start + data.objects.length - 1));
	}
}
