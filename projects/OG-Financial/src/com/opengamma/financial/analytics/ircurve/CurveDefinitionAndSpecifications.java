/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static com.opengamma.util.time.Tenor.EIGHT_MONTHS;
import static com.opengamma.util.time.Tenor.ELEVEN_MONTHS;
import static com.opengamma.util.time.Tenor.FIVE_MONTHS;
import static com.opengamma.util.time.Tenor.FIVE_YEARS;
import static com.opengamma.util.time.Tenor.FOUR_MONTHS;
import static com.opengamma.util.time.Tenor.FOUR_YEARS;
import static com.opengamma.util.time.Tenor.NINE_MONTHS;
import static com.opengamma.util.time.Tenor.ONE_DAY;
import static com.opengamma.util.time.Tenor.ONE_MONTH;
import static com.opengamma.util.time.Tenor.ONE_WEEK;
import static com.opengamma.util.time.Tenor.ONE_YEAR;
import static com.opengamma.util.time.Tenor.SEVEN_MONTHS;
import static com.opengamma.util.time.Tenor.SIX_MONTHS;
import static com.opengamma.util.time.Tenor.TEN_MONTHS;
import static com.opengamma.util.time.Tenor.THREE_DAYS;
import static com.opengamma.util.time.Tenor.THREE_MONTHS;
import static com.opengamma.util.time.Tenor.THREE_WEEKS;
import static com.opengamma.util.time.Tenor.THREE_YEARS;
import static com.opengamma.util.time.Tenor.TWELVE_MONTHS;
import static com.opengamma.util.time.Tenor.TWO_DAYS;
import static com.opengamma.util.time.Tenor.TWO_MONTHS;
import static com.opengamma.util.time.Tenor.TWO_WEEKS;
import static com.opengamma.util.time.Tenor.TWO_YEARS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.time.calendar.Period;

import com.opengamma.core.common.Currency;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CurveDefinitionAndSpecifications {

  public static YieldCurveDefinition buildUSDSwapOnlyCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, new Tenor(Period.ofYears(i)), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    //final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    //final String rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    //TODO need to be able to add interpolator + extrapolator + extrapolator (or even more than one interpolator for use in different regions of the curve) [FIN-149]
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.getInstance("USD"), "SWAP_ONLY", interpolatorName, strips);
    return definition;
  }
  
  public static YieldCurveDefinition buildUSDSwapOnlyNo3YrCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {1, 2, 4, 5, 6, 7, 10, 15, 20, 25, 30};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, new Tenor(Period.ofYears(i)), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    //final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    //final String rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    //TODO need to be able to add interpolator + extrapolator + extrapolator (or even more than one interpolator for use in different regions of the curve) [FIN-149]
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.getInstance("USD"), "SWAP_ONLY_NO3YR", interpolatorName, strips);
    return definition;
  }
  
  public static YieldCurveDefinition buildUSDSwapOnly3YrCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {3};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, new Tenor(Period.ofYears(i)), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    //final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    //final String rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    //TODO need to be able to add interpolator + extrapolator + extrapolator (or even more than one interpolator for use in different regions of the curve) [FIN-149]
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.getInstance("USD"), "SWAP_ONLY_3YR", interpolatorName, strips);
    return definition;
  }
  
  public static YieldCurveDefinition buildUSDSingleCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, new Tenor(Period.ofDays(1)), "DEFAULT"));
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, new Tenor(Period.ofMonths(3)), "DEFAULT"));
    for (final int i : new int[] {6, 9, 12}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FRA, new Tenor(Period.ofMonths(i)), "DEFAULT"));
    }
    strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, new Tenor(Period.ofYears(1)), 1, "DEFAULT"));
    strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, new Tenor(Period.ofYears(1)), 2, "DEFAULT"));
  
    final int[] tenors = new int[] {2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, new Tenor(Period.ofYears(i)), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.getInstance("USD"), "SINGLE", interpolatorName, strips);
    return definition;
  }

  public static YieldCurveDefinition buildUSDFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
//    strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, new Tenor(Period.ofDays(7)), "DEFAULT"));
//    strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, new Tenor(Period.ofDays(14)), "DEFAULT"));
//    for (final int i : new int[] {1, 3, 6, 7, 9}) {
//      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, new Tenor(Period.ofMonths(i)), "DEFAULT"));
    
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, new Tenor(Period.ofDays(1)), "DEFAULT"));  
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, new Tenor(Period.ofMonths(3)), "DEFAULT"));
//    }
    
    for (final int i : new int[] {1, 2, 3, 5, 7, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.TENOR_SWAP, new Tenor(Period.ofYears(i)), "DEFAULT"));
    }
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.getInstance("USD"), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
    return definition;
  }

  public static YieldCurveDefinition buildUSDForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, new Tenor(Period.ofDays(1)), "DEFAULT"));  
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, new Tenor(Period.ofMonths(3)), "DEFAULT"));
    
//    for (final int i : new int[] {6, 9, 12, 15, 18, 21}) {
//      strips.add(new FixedIncomeStrip(StripInstrumentType.FRA, new Tenor(Period.ofMonths(i)), "DEFAULT"));
//    }
    
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, new Tenor(Period.ofYears(0)), i, "DEFAULT"));
    }
    
    for (final int i : new int[] {2, 3, 4, 5, 7, 10, 15, 20, 25, 30 }) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, new Tenor(Period.ofYears(i)), "DEFAULT"));
    }
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.getInstance("USD"), "FORWARD", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
    return definition;
  }



  public static CurveSpecificationBuilderConfiguration buildUSDCurveSpecificationBuilderConfiguration() {
    final Map<Tenor, CurveInstrumentProvider> liborRateInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    liborRateInstrumentProviders.put(new Tenor(Period.ofDays(1)), new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "US00O/N Index")));
    liborRateInstrumentProviders.put(new Tenor(Period.ofDays(7)), new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "US0001W Index")));
    liborRateInstrumentProviders.put(new Tenor(Period.ofDays(14)), new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "US0002W Index")));
    for (int i = 1; i <= 12; i++) {
      liborRateInstrumentProviders.put(new Tenor(Period.ofMonths(i)), 
          new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "US00" + (i < 10 ? "0" : "") + i + "M Index")));
    }

    final Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickersFRAs = new Object[][] {
      {THREE_MONTHS, "USFR00C Curncy"}, {SIX_MONTHS, "USFR0CF Curncy"}, {NINE_MONTHS, "USFR0FI Curncy"},
      {TWELVE_MONTHS, "USFR0I1 Curncy"}, {new Tenor(Period.ofMonths(15)), "USFR011C Curncy" }, {new Tenor(Period.ofMonths(18)), "USFR1C1F Curncy"},
      {new Tenor(Period.ofMonths(21)), "USFR1F1I Curncy"}, {new Tenor(Period.ofMonths(24)), "USFR1I2 Curncy"}
    };
    for (final Object[] tenorsTickersFRA : tenorsTickersFRAs) {
      final Tenor tenor = (Tenor) tenorsTickersFRA[0];
      final String ticker = (String) tenorsTickersFRA[1];
      fraInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> depositCashInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers = new Object[][]  {
      {ONE_DAY, "USDR1T Curncy" }, {TWO_DAYS, "USDR2T Curncy"}, {THREE_DAYS, "USDR3T Curncy"},
      {ONE_WEEK, "USDR7D Curncy"}, {TWO_WEEKS, "USDR2Z Curncy"}, {THREE_WEEKS, "USDR3Z Curncy"},
      {ONE_MONTH, "USDRA Curncy"}, {TWO_MONTHS, "USDRB Curncy"}, {THREE_MONTHS, "USDRC Curncy"},
      {FOUR_MONTHS, "USDRD Curncy"}, {FIVE_MONTHS, "USDRE Curncy"}, {SIX_MONTHS, "USDRF Curncy"},
      {SEVEN_MONTHS, "USDRG Curncy"}, {EIGHT_MONTHS, "USDRH Curncy"}, {NINE_MONTHS, "USDRI Curncy"},
      {TEN_MONTHS, "USDRJ Curncy"}, {ELEVEN_MONTHS, "USDRK Curncy"},  {TWELVE_MONTHS, "USDRL Curncy"},
      {ONE_YEAR, "USDR1 Curncy"}, {TWO_YEARS, "USDR2 Curncy"}, {THREE_YEARS, "USDR3 Curncy"},
      {FOUR_YEARS, "USDR4 Curncy"}, {FIVE_YEARS, "USDR5 Curncy"}
    };


    for (final Object[] tenorsTicker : tenorsTickers) {
      final Tenor tenor = (Tenor) tenorsTicker[0];
      final String ticker = (String) tenorsTicker[1];
      depositCashInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    futureInstrumentProviders.put(new Tenor(Period.ofYears(0)), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 1 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(new Tenor(Period.ofYears(1)), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 1 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(new Tenor(Period.ofMonths(12)), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 1.5 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(new Tenor(Period.ofMonths(18)), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 2 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(new Tenor(Period.ofYears(2)), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 2 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(new Tenor(Period.ofMonths(24)), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 2 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(new Tenor(Period.ofYears(3)), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 2 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(new Tenor(Period.ofMonths(36)), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));

    final Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] availableYears = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 25, 30, 35, 40, 45, 50, 60 };
    for (final int i : availableYears) {
      swapInstrumentProviders.put(new Tenor(Period.ofYears(i)), new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "USSW" + i + " Curncy")));
    }
    
    final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] tenorAvailableYears = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30};
    for (final int i : tenorAvailableYears) {
      tenorSwapInstrumentProviders.put(new Tenor(Period.ofYears(i)), new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "USBG" + i + " Curncy")));
    }

    final CurveSpecificationBuilderConfiguration config = new CurveSpecificationBuilderConfiguration(depositCashInstrumentProviders, fraInstrumentProviders,
                                                                                                     liborRateInstrumentProviders, futureInstrumentProviders,
                                                                                                     swapInstrumentProviders, basisSwapInstrumentProviders, 
                                                                                                     tenorSwapInstrumentProviders);
    return config;
  }

  public static CurveSpecificationBuilderConfiguration buildTestConfiguration() {
    final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    cashInstrumentProviders.put(new Tenor(Period.ofDays(1)), new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "US00O/N Curncy")));
    cashInstrumentProviders.put(new Tenor(Period.ofDays(7)), new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "US0001W Curncy")));

    final Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickersFRAs = new Object[][] {{THREE_MONTHS, "USFR00C Curncy"}, {SIX_MONTHS, "USFR0CF Curncy"}};
    for (final Object[] tenorsTickersFRA : tenorsTickersFRAs) {
      final Tenor tenor = (Tenor) tenorsTickersFRA[0];
      final String ticker = (String) tenorsTickersFRA[1];
      fraInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers = new Object[][]  {{ONE_DAY, "USDR1T Curncy" }, {TWO_DAYS, "USDR2T Curncy"}};

    for (final Object[] tenorsTicker : tenorsTickers) {
      final Tenor tenor = (Tenor) tenorsTicker[0];
      final String ticker = (String) tenorsTicker[1];
      rateInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    // note that these are start points, so 1 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(new Tenor(Period.ofMonths(12)), new BloombergFutureCurveInstrumentProvider("ED", "Curncy"));
    // note that these are start points, so 1.5 yr + (as many quarterly futures as you want)

    final Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] availableYears = {1, 2};
    for (final int i : availableYears) {
      swapInstrumentProviders.put(new Tenor(Period.ofYears(i)), new StaticCurveInstrumentProvider(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "USSW" + i + " Curncy")));
    }
    
    final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();

    final CurveSpecificationBuilderConfiguration config = new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fraInstrumentProviders,
                                                                                                     rateInstrumentProviders, futureInstrumentProviders,
                                                                                                     swapInstrumentProviders, basisSwapInstrumentProviders,
                                                                                                     tenorSwapInstrumentProviders);
    return config;
  }
}
