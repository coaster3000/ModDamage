package com.ModDamage.Backend.Configuration.Parsing.Property;

import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider.IDataTransformer;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class PropertyTransformer<T, S> implements IDataTransformer<T, S>
{
	final Property<T, S> property;
	
	public PropertyTransformer(Property<T, S> property) {
		this.property = property;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IDataProvider<T> transform(EventInfo info, final IDataProvider<S> dp)
	{
		return new DataProvider<T, S>((Class<S>) dp.provides(), dp)
			{
				@Override
				public T get(S start, EventData data) throws BailException
				{
					return property.get(start, data);
				}

				@Override
				public Class<? extends T> provides()
				{
					return property.provides;
				}
				
				@Override
				public String toString()
				{
					return startDP.toString();
				}
			};
	}
}
