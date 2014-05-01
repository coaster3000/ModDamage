package com.ModDamage.Backend.Configuration.Parsing;

import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Nullable;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;

public interface IDataProvider<T>
{
	@Nullable
    public T get(EventData data) throws BailException;
	public abstract Class<? extends T> provides();
}
