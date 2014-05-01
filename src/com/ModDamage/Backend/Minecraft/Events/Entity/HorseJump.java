package com.ModDamage.Backend.Minecraft.Events.Entity;

import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.HorseJumpEvent;

import com.ModDamage.ModDamage;
import com.ModDamage.Backend.Minecraft.Events.MDEvent;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.SimpleEventInfo;

public class HorseJump extends MDEvent {
	
	public HorseJump() { super(myInfo); }
	
	static EventInfo myInfo = new SimpleEventInfo(
			Horse.class, "horse",
			Double.class, "power", "-default",
			Boolean.class, "cancelled");

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onHorseJump(HorseJumpEvent event)
	{
		if(!ModDamage.isEnabled || event.isCancelled()) return;
		
		EventData data = myInfo.makeData(event.getEntity(), event.getPower(), event.isCancelled());
		
		runRoutines(data);
		
		event.setCancelled(data.get(Boolean.class, data.start + data.objects.length - 1));
		event.setPower(data.get(Double.class, data.start + 1).floatValue());
	}
}
