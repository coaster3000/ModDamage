package com.ModDamage.Expressions;


import com.ModDamage.Backend.BailException;
import com.ModDamage.EventInfo.EventData;
import com.ModDamage.EventInfo.IDataProvider;
import com.ModDamage.Expressions.List.EntitiesInWorld;

import java.util.List;

public abstract class ListExp<T> implements IDataProvider<List> {
    public List<T> get(EventData data) throws BailException {
        return null;
    }

    public final Class<List> provides() {
        return List.class;
    }

    public abstract Class<T> providesElement();


    public static void register() {
        EntitiesInWorld.register();
    }
}