package com.ModDamage.Backend.Configuration.Parsing;

import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Nullable;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;

public interface ISettableDataProvider<T> extends IDataProvider<T>
{
	public void set(EventData data, @Nullable T value) throws BailException;
	public boolean isSettable();

}