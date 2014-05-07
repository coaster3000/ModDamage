package com.ModDamage.Backend.Minecraft.Magic.Handle;

import java.lang.reflect.Method;

import org.bukkit.entity.Entity;

import com.ModDamage.Backend.Minecraft.Magic.MagicStuff;

public class CBHandleClass implements IMagicHandleClass
{
	final Method CraftEntity_getHandle;
	
	public CBHandleClass()
	{
		Class<?> CraftEntity = MagicStuff.safeClassForName(MagicStuff.obc + ".entity.CraftEntity");
		CraftEntity_getHandle = MagicStuff.safeGetMethod(CraftEntity, "getHandle");
	}

	@Override
	public Class<?> getHandleClass(Entity entity)
	{
		try
		{
			return CraftEntity_getHandle.invoke(entity).getClass();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
