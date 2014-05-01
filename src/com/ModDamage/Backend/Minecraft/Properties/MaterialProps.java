package com.ModDamage.Backend.Minecraft.Properties;

import com.ModDamage.Backend.Configuration.Parsing.Property.Properties;

import org.bukkit.Material;

public class MaterialProps
{
    public static void register()
    {
        Properties.register("isSolid", Material.class, "isSolid");
        Properties.register("isRecord", Material.class, "isRecord");
    }
}
