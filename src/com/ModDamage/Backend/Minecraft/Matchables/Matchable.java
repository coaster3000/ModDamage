package com.ModDamage.Backend.Minecraft.Matchables;

public interface Matchable<T extends Enum<T> & Matchable<T>>
{
	public boolean matches(Matchable<?> other);
	
	public String name();
}
