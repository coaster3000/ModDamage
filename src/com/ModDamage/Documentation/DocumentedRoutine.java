package com.ModDamage.Documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * Routine documentation annotation used for source-located documentation and automagic generation of wiki material.
 * This annotation is solely for the purpose of the organic aspects of the wiki. Automatic properties such as regex syntax
 *   are automagically deduced and included in the doc-making script.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DocumentedRoutine {
	String Name();
	String Description();
	String Example();
}
