package com.ModDamage.Backend.Configuration.Parsing;

import com.ModDamage.Backend.Configuration.Parsing.DataProvider.IDataTransformer;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;

public class CastTransformer<T, S> implements IDataTransformer<T, S>
{
	final Class<T> provides;
	
	public CastTransformer(Class<T> provides)
	{
		this.provides = provides;
	}
	
	@Override
	public IDataProvider<T> transform(EventInfo info, IDataProvider<S> dp)
	{
		return new CastDataProvider<T>(dp, provides);
	}

}
