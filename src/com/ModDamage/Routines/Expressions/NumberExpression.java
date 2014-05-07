package com.ModDamage.Routines.Expressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ModDamage.StringMatcher;
import com.ModDamage.Backend.BailException;
import com.ModDamage.Backend.Configuration.Parsing.DataProvider;
import com.ModDamage.Backend.Configuration.Parsing.IDataProvider;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventData;
import com.ModDamage.Backend.Minecraft.Events.EventInfo.EventInfo;
import com.ModDamage.Backend.Minecraft.Variables.Number.EnchantmentInt;
import com.ModDamage.Backend.Minecraft.Variables.Number.ItemEnchantmentInt;
import com.ModDamage.Backend.Minecraft.Variables.Number.LocalNum;
import com.ModDamage.Backend.Minecraft.Variables.Number.NegativeNum;
import com.ModDamage.Backend.Minecraft.Variables.Number.NumberOp;
import com.ModDamage.Backend.Minecraft.Variables.Number.PotionEffectInt;
import com.ModDamage.Backend.Minecraft.Variables.Number.RoutinesNum;
import com.ModDamage.Routines.RoutineList;
import com.ModDamage.Routines.Expressions.Function.BlockFunction;
import com.ModDamage.Routines.Expressions.Function.DistanceFunction;
import com.ModDamage.Routines.Expressions.Function.IntFunction;
import com.ModDamage.Routines.Expressions.Function.LocFunction;

public abstract class NumberExpression<From> extends DataProvider<Number, From>
{
	protected NumberExpression(Class<From> wantStart, IDataProvider<From> startDP)
	{
		super(wantStart, startDP);
		defaultValue = 0;
	}
	
	public final Number get(From from, EventData data) throws BailException
	{
		try
		{
			return myGet(from, data);
		}
		catch (Throwable t)
		{
			throw new BailException(this, t);
		}
	}
	protected abstract Number myGet(From from, EventData data) throws BailException;

    public static final Pattern literalNumber = Pattern.compile("[0-9]+(\\.[0-9]+)?");

    /**
     * This parses either a literal number (123) or %{number}
     * @param sm sm.spawn()
     * @param info The current EventInfo
     * @return The new Number IDataProvider or null if parsing failed
     */
    public static IDataProvider<Number> parse(StringMatcher sm, EventInfo info) {
        Matcher m = sm.matchFront(InterpolatedString.interpolationStartPattern);
        if (m != null) {
            IDataProvider<Number> numberDP = DataProvider.parse(info, Number.class, sm.spawn(), false, true, InterpolatedString.interpolationEndPattern);
            if (numberDP == null) return null;
            if (!sm.matchesFront(InterpolatedString.interpolationEndPattern)) return null;
            return numberDP;
        }

        m = sm.matchFront(literalNumber);
        if (m != null) {
        	if (m.group(1) != null)
                return new LiteralNumber(Double.parseDouble(m.group()));
        	else
        		return new LiteralNumber(Integer.parseInt(m.group()));
        }

        return null;
    }
	
	@Override
	public Class<? extends Number> provides() { return Number.class; }
	
	public static IDataProvider<Number> getNew(RoutineList routines, EventInfo info) 
	{
		if(routines != null && !routines.isEmpty())
			return new RoutinesNum(routines, info);
		return null;
	}
	
	public static void registerAllNumbers()
	{
		IntFunction.register();
		LocFunction.register();
		BlockFunction.register();
		DistanceFunction.register();
		
		LiteralNumber.register();
		LocalNum.register();
		EnchantmentInt.register();
		NumberOp.register();
		ItemEnchantmentInt.register();
		NegativeNum.register();
		PotionEffectInt.register();

		com.ModDamage.External.mcMMO.PlayerInt.register();
		com.ModDamage.External.mcMMO.PlayerSkillInt.register();
	}
}