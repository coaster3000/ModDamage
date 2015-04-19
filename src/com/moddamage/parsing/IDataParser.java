package com.moddamage.parsing;

import java.util.regex.Matcher;

import com.moddamage.StringMatcher;
import com.moddamage.backend.ScriptLine;
import com.moddamage.eventinfo.EventInfo;

public interface IDataParser<T, S>
{
	IDataProvider<T> parse(ScriptLine line, EventInfo info, IDataProvider<S> startDP, Matcher m, StringMatcher sm);
}