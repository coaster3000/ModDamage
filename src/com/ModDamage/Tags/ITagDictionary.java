package com.ModDamage.Tags;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Entity;

public interface ITagDictionary<TagValueType, TaggedType> {
    
	/**
     * Tag the thing. A new tag is made if it doesn't already exist.
     */
	void addTag(TaggedType obj, String tag, TagValueType tagValue);
    
    /**
    * Checks if thing has been tagged with the specified tag.
    *
    * @return Boolean indicating whether or not the thing was tagged.
    */
    boolean isTagged(TaggedType obj, String tag);
    
    
    List<String> getTags(TaggedType obj);
    
    
    Map<TaggedType, TagValueType> getAllTagged(String tag);
    
    
    TagValueType getTagValue(TaggedType obj, String tag);
    
    
    void removeTag(TaggedType obj, String tag);
    
    void clear();

    
    void load(Map tagMap, Map<UUID, Entity> entities);//<String, Map<String, TaggedType>>
    
    
    Map save(Set<Entity> entities);
}
