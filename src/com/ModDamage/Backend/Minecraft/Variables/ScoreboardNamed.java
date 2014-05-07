package com.ModDamage.Backend.Minecraft.Variables;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.scoreboard.Scoreboard;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.BaseDataParser;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Scoreboards;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Routines.Expressions.InterpolatedString;

public class ScoreboardNamed implements IDataProvider<Scoreboard>
{
    public static final Pattern word = Pattern.compile("[\\w\\[\\]]+");

	public static void register()
	{
		DataProvider.register(Scoreboard.class,
				Pattern.compile("scoreboard(?:named)?_", Pattern.CASE_INSENSITIVE),
				new BaseDataParser<Scoreboard>()
				{
					@Override
					public IDataProvider<Scoreboard> parse(EventInfo info, Matcher m, StringMatcher sm)
					{
                        IDataProvider<String> nameDP = InterpolatedString.parseWord(word, sm.spawn(), info);
                        if (nameDP == null) return null;

                        sm.accept();
						return new ScoreboardNamed(nameDP);
					}
				});
	}

	protected final IDataProvider<String> nameDP;

	ScoreboardNamed(IDataProvider<String> nameDP)
	{
		this.nameDP = nameDP;
	}
	
	@Override
	public Scoreboard get(EventData data) throws BailException
    {
        String nameString = nameDP.get(data);
        if (nameString == null) return null;
        
        Scoreboard sb = Scoreboards.getNamed(nameString);
        
        return sb;
    }
	
	@Override
	public Class<Scoreboard> provides() { return Scoreboard.class; }
	
	@Override
	public String toString(){ return "scoreboard_" + nameDP; }
}
