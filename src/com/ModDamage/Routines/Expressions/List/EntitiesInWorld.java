package com.ModDamage.Routines.Expressions.List;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.ModDamage.StringMatcher;
import com.ModDamage.Utils;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Matchables.EntityType;
import com.ModDamage.Routines.Expressions.ListExpression;

@SuppressWarnings("rawtypes")
public class EntitiesInWorld extends ListExpression {
    public final EntityType entityType;
    public final IDataProvider<World> worldDP;

    public EntitiesInWorld(EntityType entityType, IDataProvider<World> worldDP) {
        this.entityType = entityType;
        this.worldDP = worldDP;
    }

    @SuppressWarnings("unchecked")
    public List get(EventData data) throws BailException {
        Class cls = entityType.myClass;
        List entities;

        if (worldDP == null) {
            entities = new ArrayList();
            for (World world : Bukkit.getServer().getWorlds())
                entities.addAll(world.getEntitiesByClass(cls));
        }
        else {
            World world = worldDP.get(data);
            if (world == null) return null;

            entities = Utils.asList(world.getEntitiesByClass(cls));
        }

        return entities;
    }

    public Class<?> providesElement() {
        return entityType.myClass;
    }

    public String toString() {
        return entityType + " in " + (worldDP == null? "server" : worldDP);
    }

    public static final Pattern serverPattern = Pattern.compile("server", Pattern.CASE_INSENSITIVE);

    public static void register()
    {
        DataProvider.register(List.class, Pattern.compile("(?:all\\s+)?(\\w+) in ", Pattern.CASE_INSENSITIVE), new BaseDataParser<List>() {
            public EntitiesInWorld parse(EventInfo info, Matcher m, StringMatcher sm) {
                EntityType entityType = EntityType.getElementNamed(m.group(1));
                if (entityType == null) return null;

                IDataProvider<World> worldDP;
                if (sm.matchesFront(serverPattern))
                    worldDP = null;
                else {
                	worldDP = DataProvider.parse(info, World.class, sm.spawn());
                	if (worldDP == null) return null;
                }

                sm.accept();
                return new EntitiesInWorld(entityType, worldDP);
            }
        });
    }
}
