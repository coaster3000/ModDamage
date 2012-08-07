package com.ModDamage.Events;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.ModDamage.MDEvent;
import com.ModDamage.ModDamage;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.EventInfo;
import com.ModDamage.EventInfo.SimpleEventInfo;

public class ProjectileHit extends MDEvent implements Listener
{
	public ProjectileHit() { super(myInfo); }
	
	static final EventInfo myInfo = new SimpleEventInfo(
			Entity.class, 		"shooter",
			Projectile.class,	"projectile",
			World.class,		"world");
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onProjectileHit(ProjectileHitEvent event)
	{
		if(!ModDamage.isEnabled) return;
		
		Projectile projectile = (Projectile)event.getEntity();
		LivingEntity shooter = projectile.getShooter();
		
		EventData data = myInfo.makeData(
				shooter,
				projectile,
				projectile.getWorld());
		
		runRoutines(data);
	}
}