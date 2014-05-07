package com.ModDamage.Routines.Expressions.List;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.ModDamage.ModDamage;
import com.ModDamage.StringMatcher;
import com.ModDamage.TagManager;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Expressions.InterpolatedString;
import com.ModDamage.Routines.Expressions.ListExpression;
import com.ModDamage.Tags.ITagDictionary;
import com.ModDamage.Tags.TagsHolder;

public class TaggedObjectSet<TaggedClass> extends ListExpression<TaggedClass> {
	public final boolean isString;
	public final Class<TaggedClass> tagClass;
	public final IDataProvider<String> tagName;

    public TaggedObjectSet(boolean isString, Class<TaggedClass> tagClass, IDataProvider<String> tagName)
	{
    	this.isString = isString;
    	this.tagClass = tagClass;
    	this.tagName = tagName;
	}

	public List<TaggedClass> get(EventData data) throws BailException {
        TagManager manager = TagManager.getInstance();
        
        TagsHolder<?> holder;
        if (isString)
        	holder = manager.stringTags;
        else
        	holder = manager.numTags;
    	
		ITagDictionary<?, ?> tags;
		if (tagClass == OfflinePlayer.class)
			tags = (ITagDictionary<?, ?>) holder.onPlayer;
		else if (tagClass == Entity.class)
			tags = (ITagDictionary<?, ?>) holder.onEntity;
		else if (tagClass == World.class)
			tags = (ITagDictionary<?, ?>) holder.onWorld;
		else if (tagClass == Chunk.class)
			tags = (ITagDictionary<?, ?>) holder.onChunk;
		else if (tagClass == Location.class)
			tags = (ITagDictionary<?, ?>) holder.onLocation;
		else return null;
        
		String tag = tagName.get(data);
		if (tag == null) return null;
		
        @SuppressWarnings("unchecked")
		Map<TaggedClass, ?> things = (Map<TaggedClass, ?>) tags.getAllTagged(tag.toLowerCase());
        if (things == null) return null;

        return new ArrayList<TaggedClass>(things.keySet());
    }

    public Class<TaggedClass> providesElement() {
        return tagClass;
    }

    public String toString() {
        return tagClass.getSimpleName().toLowerCase() + " " + (isString?"s":"") + "tagged " + tagName;
    }

    public static void register()
    {
        DataProvider.register(List.class, Pattern.compile("(players?|entit(?:y|ies)|worlds?|chunks?|loc(?:ation)?s?|blocks?) (s)?tagged ", Pattern.CASE_INSENSITIVE), new BaseDataParser() {
            public TaggedObjectSet parse(EventInfo info, Matcher m, StringMatcher sm) {
                String taggableType = m.group(1).toLowerCase();
                String tagType = m.group(2);
                IDataProvider<String> tagName = InterpolatedString.parseWord(InterpolatedString.word, sm.spawn(), info);
                
                
                boolean isString;
                if (tagType != null)
                	isString = true;
                else
                	isString = false;
                
                Class<? extends Object> tagClass;
                if (taggableType.startsWith("player"))
                	tagClass = OfflinePlayer.class;
                else if (taggableType.startsWith("entity"))
                	tagClass = Entity.class;
                else if (taggableType.startsWith("world"))
                	tagClass = World.class;
                else if (taggableType.startsWith("chunk"))
                	tagClass = Chunk.class;
                else if (taggableType.startsWith("loc") || taggableType.startsWith("block"))
                	tagClass = Location.class;
                else return null;

                sm.accept();
                return new TaggedObjectSet(isString, tagClass, tagName);
            }
        });
    }
}
