package com.ModDamage.Backend.Minecraft.Events.Player;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.ModDamage.ModDamage;
import com.ModDamage.Backend.Minecraft.Events.MDEvent;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.SimpleEventInfo;

public class AsyncChat extends MDEvent implements Listener
{
	public AsyncChat() { super(myInfo); }
	
	static final EventInfo myInfo = new SimpleEventInfo(
			Player.class,	"player",
			World.class,	"world",
			String.class,	"message",
			String.class,	"format",
			Boolean.class,	"cancelled");
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent event)
	{
		if(!ModDamage.isEnabled) return;
		
		EventData data = myInfo.makeData(
				event.getPlayer(),
				event.getPlayer().getWorld(),
				event.getMessage(),
				event.getFormat(),
				event.isCancelled());
		
		runRoutines(data);

		event.setMessage(data.get(String.class, data.start + 2));
		event.setFormat(data.get(String.class, data.start + 3));
		
		event.setCancelled(data.get(Boolean.class, data.start + data.objects.length - 1));
	}
}
