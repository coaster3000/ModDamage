package com.ModDamage.Routines.Expressions;


import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Routines.Expressions.List.EntitiesInWorld;
import com.ModDamage.Routines.Expressions.List.TaggedObjectSet;

import java.util.List;

@SuppressWarnings("rawtypes")
public abstract class ListExpression<T> implements IDataProvider<List> {
    public List<T> get(EventData data) throws BailException {
        return null;
    }

	public final Class<List> provides() {
        return List.class;
    }

    public abstract Class<T> providesElement();


    public static void register() {
        EntitiesInWorld.register();
        TaggedObjectSet.register();
    }
}
