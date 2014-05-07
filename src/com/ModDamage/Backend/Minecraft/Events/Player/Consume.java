package com.ModDamage.Backend.Minecraft.Events.Player;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import com.ModDamage.ModDamage;
import com.ModDamage.Backend.Minecraft.ItemHolder;
import com.ModDamage.Backend.Minecraft.Events.MDEvent;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.SimpleEventInfo;

public class Consume extends MDEvent implements Listener
{
	public Consume() { super(myInfo); }
	
	static final EventInfo myInfo = new SimpleEventInfo(
			Player.class,		"player",
			World.class,		"world",
			ItemHolder.class, 	"item",
			Boolean.class,		"cancelled");
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onConsume(PlayerItemConsumeEvent event)
	{
		if(!ModDamage.isEnabled) return;
		
		Player player = event.getPlayer();
		ItemHolder itemHolder = new ItemHolder(event.getItem());
		
		EventData data = myInfo.makeData(
				player,
				player.getWorld(),
				itemHolder,
				event.isCancelled()
				);
		
		runRoutines(data);
		
		event.setCancelled(data.get(Boolean.class, data.start + data.objects.length - 1));
		event.setItem(itemHolder.getItem());
	}
}
