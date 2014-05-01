package com.ModDamage.Backend.Minecraft.Events.Player;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ModDamage.ModDamage;
import com.ModDamage.Backend.Minecraft.Events.Init;
import com.ModDamage.Backend.Minecraft.Events.MDEvent;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.SimpleEventInfo;

public class Join extends MDEvent implements Listener
{
	public Join() { super(myInfo); }
	
	static final EventInfo myInfo = new SimpleEventInfo(
			Player.class,	"player",
			World.class,	"world");
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event)
	{
		if(!ModDamage.isEnabled) return;
		
		if (disableJoinMessages)
			event.setJoinMessage(null);
		
		Player player = event.getPlayer();
		EventData data = myInfo.makeData(
				player,
				player.getWorld());
		
		runRoutines(data);
		
		Init.onInit(player);
	}
}
