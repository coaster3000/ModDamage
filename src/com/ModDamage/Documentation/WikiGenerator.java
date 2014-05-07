package com.ModDamage.Documentation;

import com.ModDamage.Routines.Routine;

public class WikiGenerator {

	//TODO Markdown production!
	
	public String generateMarkdownFromRoutine(Class<? extends Routine> routineClass)
	{
		//foreach()
		if(routineClass.isAnnotationPresent(DocumentedRoutine.class))
		{
			
		}
		else
		{
			
		}
		return null;
	}
}
