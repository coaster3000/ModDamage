package com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Base;

import java.util.Random;

import com.KoryuObihiro.bukkit.ModDamage.RoutineObjects.Routine;

abstract class ChanceCalculation extends Routine
{
	int chance;
	final Random random = new Random();
}