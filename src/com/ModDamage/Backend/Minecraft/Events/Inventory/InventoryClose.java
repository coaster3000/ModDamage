package com.ModDamage.Backend.Minecraft.Events.Inventory;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import com.ModDamage.ModDamage;
import com.ModDamage.Backend.Minecraft.Events.MDEvent;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.SimpleEventInfo;

public class InventoryClose extends MDEvent implements Listener
{
	public InventoryClose() { super(myInfo); }
	
	static final EventInfo myInfo = new SimpleEventInfo(
			Inventory.class,	"inv", "inventory",
			InventoryView.class, "view",
			Player.class,		"player",
			World.class,		"world");
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onInteract(InventoryCloseEvent event)
	{
		if(!ModDamage.isEnabled) return;
		
		Player player = (Player) event.getPlayer();
		
		EventData data = myInfo.makeData(
				event.getInventory(),
				event.getView(),
				player,
				player.getWorld()
				);
		
		runRoutines(data);
	}
}
