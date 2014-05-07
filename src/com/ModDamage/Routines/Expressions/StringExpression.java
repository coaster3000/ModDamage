package com.ModDamage.Routines.Expressions;

import java.util.ArrayList;
import java.util.List;

import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Variables.String.EntityAsString;
import com.ModDamage.Backend.Minecraft.Variables.String.EntityString;
import com.ModDamage.Backend.Minecraft.Variables.String.PlayerString;
import com.ModDamage.Backend.Minecraft.Variables.String.WorldString;
import com.ModDamage.Routines.Expressions.Function.FormatFunction;
import com.ModDamage.Routines.Expressions.Function.IndexOfFunction;
import com.ModDamage.Routines.Expressions.Function.LoreFunction;
import com.ModDamage.Routines.Expressions.Function.RegexReplaceFunction;
import com.ModDamage.Routines.Expressions.Function.ReplaceFunction;
import com.ModDamage.Routines.Expressions.Function.SubstringFunction;
import com.ModDamage.Routines.Expressions.Function.ToIntFunction;

public abstract class StringExpression<From> extends DataProvider<String, From>
{
	protected StringExpression(Class<From> wantStart, IDataProvider<From> startDP)
	{
		super(wantStart, startDP);
	}

	@Override
	public Class<String> provides() { return String.class; }

	public static void register()
	{
		LiteralString.register();
		EntityString.register();
		EntityAsString.register();
		PlayerString.register();
		WorldString.register();
		
		SubstringFunction.register();
		IndexOfFunction.register();
		ToIntFunction.register();
		LoreFunction.register();
		ReplaceFunction.register();
		RegexReplaceFunction.register();
		FormatFunction.register();
	}


	@SuppressWarnings("unchecked")
	public static List<IDataProvider<String>> getStrings(Object nestedContent, EventInfo info)
	{
		List<String> strings = new ArrayList<String>();
		if (nestedContent instanceof String)
			strings.add((String)nestedContent);
		else if(nestedContent instanceof List)
			strings.addAll((List<String>) nestedContent);
		else
			return null;

		List<IDataProvider<String>> istrings = new ArrayList<IDataProvider<String>>();
		for(String string : strings)
		{
			istrings.add(new InterpolatedString(string, info, true));
		}

		return istrings;
	}
}