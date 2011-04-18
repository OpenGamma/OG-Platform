/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.Period;

import org.springframework.util.Assert;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CurveDefinitionAndSpecifications {

  public static YieldCurveDefinition buildUSDSwapOnlyCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    //final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    //final String rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    //TODO need to be able to add interpolator + extrapolator + extrapolator (or even more than one interpolator for use in different regions of the curve) [FIN-149]
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId("US"), "SWAP_ONLY", interpolatorName, strips);
    return definition;
  }
  
  public static YieldCurveDefinition buildUSDSwapOnlyNo3YrCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {1, 2, 4, 5, 6, 7, 10, 15, 20, 25, 30};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    //final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    //final String rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    //TODO need to be able to add interpolator + extrapolator + extrapolator (or even more than one interpolator for use in different regions of the curve) [FIN-149]
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId("US"), "SWAP_ONLY_NO3YR", interpolatorName, strips);
    return definition;
  }
  
  public static YieldCurveDefinition buildUSDSwapOnly3YrCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {3};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    //final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    //final String rightExtrapolatorName = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
    //TODO need to be able to add interpolator + extrapolator + extrapolator (or even more than one interpolator for use in different regions of the curve) [FIN-149]
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId("US"), "SWAP_ONLY_3YR", interpolatorName, strips);
    return definition;
  }
  
  public static YieldCurveDefinition buildUSDSingleCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(1), "DEFAULT"));
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(3), "DEFAULT"));
    for (final int i : new int[] {6, 9, 12}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FRA, Tenor.ofMonths(i), "DEFAULT"));
    }
    strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.ofYears(1), 1, "DEFAULT"));
    strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.ofYears(1), 2, "DEFAULT"));
  
    final int[] tenors = new int[] {2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId("US"), "SINGLE", interpolatorName, strips);
    return definition;
  }

  public static YieldCurveDefinition buildUSDFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
//    strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(7)), "DEFAULT"));
//    strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(14)), "DEFAULT"));
//    for (final int i : new int[] {1, 3, 6, 7, 9}) {
//      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofMonths(i)), "DEFAULT"));
    
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(1), "DEFAULT"));  
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(3), "DEFAULT"));
//    }
    
    for (final int i : new int[] {1, 2, 3, 5, 7, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.TENOR_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId("US"), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
    return definition;
  }

  public static YieldCurveDefinition buildUSDForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(1), "DEFAULT"));  
    strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(3), "DEFAULT"));
    
//    for (final int i : new int[] {6, 9, 12, 15, 18, 21}) {
//      strips.add(new FixedIncomeStrip(StripInstrumentType.FRA, Tenor.ofMonths(i)), "DEFAULT"));
//    }
    
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.ofYears(0), i, "DEFAULT"));
    }
    
    for (final int i : new int[] {2, 3, 4, 5, 7, 10, 15, 20, 25, 30 }) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId("US"), "FORWARD", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
    return definition;
  }
  

  public static YieldCurveDefinition buildFundingCurve(Currency ccy, Identifier region, Tenor[] liborStrips, Tenor[] tenorSwaps) {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (Tenor liborTenor : liborStrips) {
      if (liborTenor.getPeriod().equals(Period.ofDays(30))) {
        throw new OpenGammaRuntimeException("This shouldn't happen!");
      }
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, liborTenor, "DEFAULT"));
    }
    for (Tenor tenorSwapTenor : tenorSwaps) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.TENOR_SWAP, tenorSwapTenor, "DEFAULT"));
    }
    final YieldCurveDefinition definition = new YieldCurveDefinition(ccy, region, "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
    return definition;
  }
  
  public static YieldCurveDefinition buildForwardCurve(Currency ccy, Identifier region, Tenor[] liborStrips, Tenor futureStartTenor, int numQuarterlyFutures, Tenor[] swaps) {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (Tenor liborTenor : liborStrips) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, liborTenor, "DEFAULT"));
    }
    if (futureStartTenor != null) {
      for (int i = 1; i <= numQuarterlyFutures; i++) {
        strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, futureStartTenor, i, "DEFAULT"));
      }
    }
    for (Tenor tenorSwapTenor : swaps) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, tenorSwapTenor, "DEFAULT"));
    }
    final YieldCurveDefinition definition = new YieldCurveDefinition(ccy, region, "FORWARD", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
    return definition;
  }
  
  public static YieldCurveDefinition buildSingleCurve(Currency ccy, Identifier region, Tenor[] liborStrips, Tenor futureStartTenor, int numQuarterlyFutures, Tenor[] swaps) {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (Tenor liborTenor : liborStrips) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, liborTenor, "DEFAULT"));
    }
    if (futureStartTenor != null) {
      for (int i = 1; i <= numQuarterlyFutures; i++) {
        strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, futureStartTenor, i, "DEFAULT"));
      }
    }
    for (Tenor tenorSwapTenor : swaps) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, tenorSwapTenor, "DEFAULT"));
    }
    final YieldCurveDefinition definition = new YieldCurveDefinition(ccy, region, "SINGLE", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
    return definition;
  }
  
  private static Tenor[] makeShortEnd(boolean includeOvernight, boolean includeSpotNext, boolean include2W) {
    List<Tenor> results = new ArrayList<Tenor>();
    if (includeOvernight) {
      results.add(Tenor.ofDays(1));
    }
    if (includeSpotNext) {
      results.add(Tenor.ofDays(3));
    }
    results.add(Tenor.ofDays(7));
    if (include2W) {
      results.add(Tenor.ofDays(14));
    }
    for (int i = 1; i <= 12; i++) {
      results.add(Tenor.ofMonths(i));
    }
    return results.toArray(new Tenor[] {});
  }
  
  private static Tenor[] makeLongEnd(int firstContiguousYear, int lastContiguousYear, int[] nonContiguousYears) {
    Assert.isTrue(firstContiguousYear <= lastContiguousYear);
    List<Tenor> results = new ArrayList<Tenor>();
    for (int i = firstContiguousYear; i <= lastContiguousYear; i++) {
      results.add(Tenor.ofYears(i));
    }
    for (int i : nonContiguousYears) {
      results.add(Tenor.ofYears(i));
    }
    return results.toArray(new Tenor[] {});
  }
  
  public static Map<String, Map<Currency, YieldCurveDefinition>> buildStandardCurveDefintions() {
    Map<Currency, YieldCurveDefinition> forwardDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    Map<Currency, YieldCurveDefinition> fundingDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    Map<Currency, YieldCurveDefinition> singleDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    Currency usd = Currency.USD;
    Identifier usdRegion = RegionUtils.countryRegionId("US");
    forwardDefinitions.put(usd, buildForwardCurve(usd, usdRegion, makeShortEnd(true, false, true), 
        Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50, 60 })));
    fundingDefinitions.put(usd, buildFundingCurve(usd, usdRegion, makeShortEnd(true, false, true), makeLongEnd(1, 10, new int[] {12, 15, 20, 25, 30 })));
    singleDefinitions.put(usd, buildSingleCurve(usd, usdRegion, makeShortEnd(true, false, true), Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50, 60 })));
    
    Currency eur = Currency.EUR;
    Identifier eurRegion = RegionUtils.countryRegionId("EU");
    forwardDefinitions.put(eur, buildForwardCurve(eur, eurRegion, makeShortEnd(false, false, false), Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50, 60 })));
    fundingDefinitions.put(eur, buildFundingCurve(eur, eurRegion, makeShortEnd(false, false, false), makeLongEnd(1, 15, new int[] {20, 25, 30, 40 })));
    singleDefinitions.put(eur, buildSingleCurve(eur, eurRegion, makeShortEnd(false, false, false), Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50, 60 })));
    
    Currency gbp = Currency.GBP;
    Identifier gbpRegion = RegionUtils.countryRegionId("GB");
    forwardDefinitions.put(gbp, buildForwardCurve(gbp, gbpRegion, makeShortEnd(true, false, true), Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50, 60 })));
    fundingDefinitions.put(gbp, buildFundingCurve(gbp, gbpRegion, makeShortEnd(true, false, true), makeLongEnd(1, 15, new int[] {20, 25, 30, 40, 50 })));
    singleDefinitions.put(gbp, buildSingleCurve(gbp, gbpRegion, makeShortEnd(true, false, true), Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50, 60 })));
    
    Currency chf = Currency.CHF;
    Identifier chfRegion = RegionUtils.countryRegionId("CH");
    forwardDefinitions.put(chf, buildForwardCurve(chf, chfRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50 })));
    fundingDefinitions.put(chf, buildFundingCurve(chf, chfRegion, makeShortEnd(false, true, true), makeLongEnd(1, 15, new int[] {20, 25, 30 })));
    singleDefinitions.put(chf, buildSingleCurve(chf, chfRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50 })));
    
    Currency aud = Currency.AUD;
    Identifier audRegion = RegionUtils.countryRegionId("AU");
    forwardDefinitions.put(aud, buildForwardCurve(aud, audRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 12, new int[] {15, 20, 25, 30, 40 })));
    fundingDefinitions.put(aud, buildFundingCurve(aud, audRegion, makeShortEnd(false, true, true), makeLongEnd(1, 15, new int[] {20, 25, 30 })));
    singleDefinitions.put(aud, buildSingleCurve(aud, audRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 12, new int[] {15, 20, 25, 30, 40 })));
    
    Currency sek = Currency.of("SEK");
    Identifier sekRegion = RegionUtils.countryRegionId("SE");
    forwardDefinitions.put(sek, buildForwardCurve(sek, sekRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30 })));
    fundingDefinitions.put(sek, buildFundingCurve(sek, sekRegion, makeShortEnd(false, true, true), makeLongEnd(1, 15, new int[] {20, 25, 30 })));
    singleDefinitions.put(sek, buildSingleCurve(sek, sekRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30 })));
    
    Currency nzd = Currency.of("NZD");
    Identifier nzdRegion = RegionUtils.countryRegionId("NZ");
    forwardDefinitions.put(nzd, buildForwardCurve(nzd, nzdRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 30 })));
    fundingDefinitions.put(nzd, buildFundingCurve(nzd, nzdRegion, makeShortEnd(false, true, true), makeLongEnd(1, 15, new int[] {20 })));
    singleDefinitions.put(nzd, buildSingleCurve(nzd, nzdRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 30 })));
    
    Currency cad = Currency.CAD;
    Identifier cadRegion = RegionUtils.countryRegionId("CA");
    forwardDefinitions.put(cad, buildForwardCurve(cad, cadRegion, makeShortEnd(true, false, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50 })));
    fundingDefinitions.put(cad, buildFundingCurve(cad, cadRegion, makeShortEnd(true, false, true), makeLongEnd(1, 15, new int[] {20, 25, 30 })));
    singleDefinitions.put(cad, buildSingleCurve(cad, cadRegion, makeShortEnd(true, false, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50 })));
    
    Currency dkk = Currency.of("DKK");
    Identifier dkkRegion = RegionUtils.countryRegionId("DK");
    forwardDefinitions.put(dkk, buildForwardCurve(dkk, dkkRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50 })));
    fundingDefinitions.put(dkk, buildFundingCurve(dkk, dkkRegion, makeShortEnd(false, true, true), makeLongEnd(1, 15, new int[] {20, 25, 30 })));
    singleDefinitions.put(dkk, buildSingleCurve(dkk, dkkRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50 })));
    
    Currency jpy = Currency.JPY;
    Identifier jpyRegion = RegionUtils.countryRegionId("JP");
    forwardDefinitions.put(jpy, buildForwardCurve(jpy, jpyRegion, makeShortEnd(false, true, true), Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 35, 40, 50, 60 })));
    fundingDefinitions.put(jpy, buildFundingCurve(jpy, jpyRegion, makeShortEnd(false, true, true), makeLongEnd(1, 15, new int[] {20, 25, 30, 35, 40, 50 })));
    singleDefinitions.put(jpy, buildSingleCurve(jpy, jpyRegion, makeShortEnd(false, true, true), Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 35, 40, 50, 60 })));
    
    Map<String, Map<Currency, YieldCurveDefinition>> results = new HashMap<String, Map<Currency, YieldCurveDefinition>>();
    results.put("FORWARD", forwardDefinitions);
    results.put("FUNDING", fundingDefinitions);
    results.put("SINGLE", singleDefinitions);
    return results;
  }
  
  public static Map<Currency, CurveSpecificationBuilderConfiguration> buildStandardCurveSpecificationBuilderConfigurations() {
    Map<Currency, CurveSpecificationBuilderConfiguration> configurations = new HashMap<Currency, CurveSpecificationBuilderConfiguration>();
    configurations.put(Currency.USD, buildStandardCurveSpecificationBuilderConfiguration("US", "ED", "", "BG", true, false, true, 5, 30, 
                                                                                                        new int [] {35, 40, 45, 50, 60 }, new int[] {1, 3, 6, 9 }, 
                                                                                                        10, new int[] {12, 15, 20, 25, 30}));
    configurations.put(Currency.EUR, buildStandardCurveSpecificationBuilderConfiguration("EU", "ER", "V3", "BS", false, false, false, 4, 50,  
                                                                                                         new int[] {60 }, new int[] {1, 3, 6, 9},
                                                                                                         20, new int[] {25, 30, 25, 40}));
    configurations.put(Currency.GBP, buildStandardCurveSpecificationBuilderConfiguration("BP", "L ", "", "BS", true, false, true, 5, 30, 
                                                                                                        new int[] {35, 40, 50, 60}, new int[] {3, 6, 9}, 
                                                                                                        15, new int[] {20, 25, 30, 40, 50}));
    configurations.put(Currency.CHF, buildStandardCurveSpecificationBuilderConfiguration("SF", "ES", "", "BS", false, true, true, 1, 30,
                                                                                                        new int[] {40, 50 }, new int[0], 15, new int[] {20, 25, 30}));
    configurations.put(Currency.AUD, buildStandardCurveSpecificationBuilderConfiguration("AD", null, "", "BS", false, true, true, 1, 12, 
                                                                                                        new int[] {15, 20, 25, 30, 40 }, new int[0], 15, new int[] {20, 25, 30}));
    configurations.put(Currency.of("SEK"), buildStandardCurveSpecificationBuilderConfiguration("SK", null, "", "BS", false, true, true, 1, 30,  
                                                                                                        new int[0], new int[0], 15, new int[] {20, 25, 30}));
    configurations.put(Currency.of("NZD"), buildStandardCurveSpecificationBuilderConfiguration("ND", null, "", "BS", false, true, true, 1, 15,  
                                                                                                        new int[] {20, 30 }, new int[0], 15, new int[] {20 }));
    configurations.put(Currency.CAD, buildStandardCurveSpecificationBuilderConfiguration("CD", null, "", "BS", true, false, true, 1, 15,  
                                                                                                        new int[] {20, 25, 30, 40, 50 }, new int[0], 15, new int[] {20, 25, 30 }));
    configurations.put(Currency.of("DKK"), buildStandardCurveSpecificationBuilderConfiguration("DK", null, "", "BS", false, true, true, 1, 15,  
                                                                                                        new int[] {20, 25, 30, 40, 50 }, new int[0], 15, new int[] {20, 25, 30 }));
    configurations.put(Currency.JPY, buildStandardCurveSpecificationBuilderConfiguration("JY", "EF", "", "BS", false, true, true, 1, 30,  
                                                                                                        new int[] {35, 40, 50, 60 }, new int[0], 15, new int[] {20, 25, 30, 35, 40, 50 }));
    return configurations;
  }
  
  public static Map<Currency, CurveSpecificationBuilderConfiguration> buildSyntheticCurveSpecificationBuilderConfigurations() {
    Map<Currency, CurveSpecificationBuilderConfiguration> configurations = new HashMap<Currency, CurveSpecificationBuilderConfiguration>();
    IdentificationScheme scheme = SecurityUtils.OG_SYNTHETIC_TICKER;
    configurations.put(Currency.EUR, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.EUR, scheme));
    configurations.put(Currency.DKK, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.DKK, scheme));
    configurations.put(Currency.DEM, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.DEM, scheme));
    configurations.put(Currency.CZK, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.CZK, scheme));
    configurations.put(Currency.CAD, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.CAD, scheme));
    configurations.put(Currency.AUD, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.AUD, scheme));
    configurations.put(Currency.USD, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.USD, scheme));
    configurations.put(Currency.SKK, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.SKK, scheme));
    configurations.put(Currency.SEK, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.SEK, scheme));
    configurations.put(Currency.NOK, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.NOK, scheme));
    configurations.put(Currency.JPY, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.JPY, scheme));
    configurations.put(Currency.ITL, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.ITL, scheme));
    configurations.put(Currency.HUF, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.HUF, scheme));
    configurations.put(Currency.HKD, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.HKD, scheme));
    configurations.put(Currency.GBP, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.GBP, scheme));
    configurations.put(Currency.FRF, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.FRF, scheme));
    return configurations;
  }
  
  private static CurveSpecificationBuilderConfiguration buildSyntheticCurveSpecificationBuilderConfiguration(Currency ccy, IdentificationScheme scheme) {
    Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    
    Tenor[] tenors = new Tenor[] {Tenor.DAY, Tenor.MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, 
                                  Tenor.SEVEN_MONTHS, Tenor.EIGHT_MONTHS, Tenor.NINE_MONTHS, Tenor.TEN_MONTHS, Tenor.ELEVEN_MONTHS, Tenor.TWELVE_MONTHS,
                                  Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.ofYears(6), Tenor.ofYears(7),
                                  Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10), Tenor.ofYears(11), Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20),
                                  Tenor.ofYears(25), Tenor.ofYears(30), Tenor.ofYears(40), Tenor.ofYears(50), Tenor.ofYears(80)};
    
    for (Tenor tenor : tenors) {
      cashInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.CASH, scheme));
      fraInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.FRA, scheme));
      rateInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.LIBOR, scheme));
      futureInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.FUTURE, scheme));
      tenorSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.TENOR_SWAP, scheme));
      swapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.SWAP, scheme));
      basisSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.BASIS_SWAP, scheme));
    }
    CurveSpecificationBuilderConfiguration config = new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, 
                                                                                               fraInstrumentProviders, 
                                                                                               rateInstrumentProviders, 
                                                                                               futureInstrumentProviders, 
                                                                                               swapInstrumentProviders, 
                                                                                               basisSwapInstrumentProviders, 
                                                                                               tenorSwapInstrumentProviders);
    return config;
  }
  
  /**
   * Parameterized specification construction to remove code duplication, parameters are a bit arbitrary
   * @param generalPrefix generalized prefix for currency, e.g. US for US or BP for GB, applies to most codes
   * @param futurePrefix prefix for futures is often different, e.g. 'L ' for GB or 'ED' for US.
   * @param swapPostfix code postfix for swaps, only used on EUR at the moment (V3/V1 for vs 1M, or vs 3M) gives e.g. EUSW6V3 Curncy, empty string for 'don't use'.
   * @param basisSwapPostfix postfix for basis swaps, note this postfix goes BEFORE the year, e.g. USBG6 Curncy the postifx is BG, for SFBS7 Curncy the postfix is BS.
   * @param includeOvernight whether to include the overnight rate O/N in the spec, sometimes it's not available but the Spot/Next (S/N) is
   * @param includeSpotNext whether to include the spot/next rate S/N in the spec, sometimes it's not available
   * @param include2W whether to include the 2 week rate, sometimes it's not available.
   * @param maxDepositYears maximum number of years that deposit rates are available for (we assume all months are available, but only some currencies go out multiple years)
   * @param swapsEndContiguousYears maximum number of years that swap rates are available for every year (e.g. 20 means 1..20)
   * @param swapsNonContiguousYears array of the non-contiguous swap rate years (e.g. new int[] { 15, 18, 20, 30, 40 }) 
   * @param basisSwapsMonths array of 1-based months for which basis swaps are available. e.g. new int[] { 3, 6, 9 } means 3, 6, 9 months.
   * @param basisSwapsEndContiguousYears maximum number of years that contigious basis swap rates are available, so 20 => 1..20
   * @param basisSwapsNonContiguousYears array of the non-contiguoys basis swap rate years (e.g. new int[] { 15, 20, 30, 40 })
   * @return the specification builder configuration
   */
  public static CurveSpecificationBuilderConfiguration buildStandardCurveSpecificationBuilderConfiguration(String generalPrefix, String futurePrefix, String swapPostfix, String basisSwapPostfix,
                                                                                                           boolean includeOvernight, boolean includeSpotNext, boolean include2W,
                                                                                                           int maxDepositYears, 
                                                                                                           int swapsEndContiguousYears, int[] swapsNonContiguousYears,
                                                                                                           int[] basisSwapsMonths, int basisSwapsEndContiguousYears, 
                                                                                                           int[] basisSwapsNonContiguousYears) {
    
    // LIBOR
    final Map<Tenor, CurveInstrumentProvider> liborRateInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    if (includeOvernight) {
      liborRateInstrumentProviders.put(Tenor.ofDays(1), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + "00O/N Index")));
    }
    if (includeSpotNext) {
      liborRateInstrumentProviders.put(Tenor.ofDays(3), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + "00S/N Index")));
    }
    liborRateInstrumentProviders.put(Tenor.ofDays(7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + "0001W Index")));
    if (include2W) {
      liborRateInstrumentProviders.put(Tenor.ofDays(14), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + "0002W Index")));
    }
    for (int i = 1; i <= 12; i++) {
      liborRateInstrumentProviders.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + "00" + (i < 10 ? "0" : "") + i + "M Index")));
    }

    // FRAs
    final Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickersFRAs = new Object[][] {
      {THREE_MONTHS, generalPrefix + "FR00C Curncy"}, {SIX_MONTHS, generalPrefix + "FR0CF Curncy"}, {NINE_MONTHS, generalPrefix + "FR0FI Curncy"},
      {TWELVE_MONTHS, generalPrefix + "FR0I1 Curncy"}, {Tenor.ofMonths(15), generalPrefix + "FR011C Curncy" }, {Tenor.ofMonths(18), generalPrefix + "FR1C1F Curncy"},
      {Tenor.ofMonths(21), generalPrefix + "FR1F1I Curncy"}, {Tenor.ofMonths(24), generalPrefix + "FR1I2 Curncy"}
    };
    for (final Object[] tenorsTickersFRA : tenorsTickersFRAs) {
      final Tenor tenor = (Tenor) tenorsTickersFRA[0];
      final String ticker = (String) tenorsTickersFRA[1];
      fraInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }
    
    // DEPOSIT RATES
    final Map<Tenor, CurveInstrumentProvider> depositCashInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] depositTenorTickers = new Object[][]  {
      {ONE_DAY, generalPrefix + "DR1T Curncy" }, {TWO_DAYS, generalPrefix + "DR2T Curncy"}, {THREE_DAYS, generalPrefix + "DR3T Curncy"},
      {ONE_WEEK, generalPrefix + "DR7D Curncy"}, {TWO_WEEKS, generalPrefix + "DR2Z Curncy"}, {THREE_WEEKS, generalPrefix + "DR3Z Curncy"},
      {ONE_MONTH, generalPrefix + "DRA Curncy"}, {TWO_MONTHS, generalPrefix + "DRB Curncy"}, {THREE_MONTHS, generalPrefix + "DRC Curncy"},
      {FOUR_MONTHS, generalPrefix + "DRD Curncy"}, {FIVE_MONTHS, generalPrefix + "DRE Curncy"}, {SIX_MONTHS, generalPrefix + "DRF Curncy"},
      {SEVEN_MONTHS, generalPrefix + "DRG Curncy"}, {EIGHT_MONTHS, generalPrefix + "DRH Curncy"}, {NINE_MONTHS, generalPrefix + "DRI Curncy"},
      {TEN_MONTHS, generalPrefix + "DRJ Curncy"}, {ELEVEN_MONTHS, generalPrefix + "DRK Curncy"},  {TWELVE_MONTHS, generalPrefix + "DRL Curncy"},
      
    };

    for (final Object[] tenorsTicker : depositTenorTickers) {
      final Tenor tenor = (Tenor) tenorsTicker[0];
      final String ticker = (String) tenorsTicker[1];
      depositCashInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }
    
    final Object[][] depositYearTickers = new Object[][] {
      {ONE_YEAR, generalPrefix + "DR1 Curncy"}, {TWO_YEARS, generalPrefix + "DR2 Curncy"}, {THREE_YEARS, generalPrefix + "DR3 Curncy"},
      {FOUR_YEARS, generalPrefix + "DR4 Curncy"}, {FIVE_YEARS, generalPrefix + "DR5 Curncy"}
    };
    
    for (int i = 0; i < maxDepositYears; i++) {
      final Object[] tenorsTicker = depositYearTickers[i];
      final Tenor tenor = (Tenor) tenorsTicker[0];
      final String ticker = (String) tenorsTicker[1];
      depositCashInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    // IR FUTURES
    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    if (futurePrefix != null) {
      futureInstrumentProviders.put(Tenor.ofYears(0), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
      // note that these are start points, so 1 yr + (as many quarterly futures as you want)
      futureInstrumentProviders.put(Tenor.ofYears(1), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
      // note that these are start points, so 1 yr + (as many quarterly futures as you want)
      futureInstrumentProviders.put(Tenor.ofMonths(12), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
      // note that these are start points, so 1.5 yr + (as many quarterly futures as you want)
      futureInstrumentProviders.put(Tenor.ofMonths(18), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
      // note that these are start points, so 2 yr + (as many quarterly futures as you want)
      futureInstrumentProviders.put(Tenor.ofYears(2), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
      // note that these are start points, so 2 yr + (as many quarterly futures as you want)
      futureInstrumentProviders.put(Tenor.ofMonths(24), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
      // note that these are start points, so 2 yr + (as many quarterly futures as you want)
      futureInstrumentProviders.put(Tenor.ofYears(3), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
      // note that these are start points, so 2 yr + (as many quarterly futures as you want)
      futureInstrumentProviders.put(Tenor.ofMonths(36), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    }
    // SWAPS
    final Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    
    final String[] swapMonthCodes = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K" };
    for (int i = 1; i <= swapMonthCodes.length; i++) { // i here is 1-based so Jan=1, Dec=12.
      swapInstrumentProviders.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(
          SecurityUtils.bloombergTickerSecurityId(generalPrefix + "SW" + swapMonthCodes[i - 1] + " Curncy"))); // no postfix here.
    }
    for (int i = 1; i <= swapsEndContiguousYears; i++) {
      swapInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + "SW" + i + swapPostfix + " Curncy")));
    }
    for (final int i : swapsNonContiguousYears) {
      swapInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + "SW" + i + swapPostfix + " Curncy")));
    }
    
    // BASIS SWAPS (not used)
    final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    
    // TENOR SWAPS
    final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final String[] monthCodes = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K" };
    for (int i : basisSwapsMonths) { // i here is 1-based so Jan=1, Dec=12.
      tenorSwapInstrumentProviders.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + basisSwapPostfix + monthCodes[i - 1] + " Curncy")));
    }
    for (int i = 1; i <= basisSwapsEndContiguousYears; i++) {
      tenorSwapInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + basisSwapPostfix + i + " Curncy")));
    }
    for (final int i : basisSwapsNonContiguousYears) {
      tenorSwapInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(generalPrefix + basisSwapPostfix + i + " Curncy")));
    }

    final CurveSpecificationBuilderConfiguration config = new CurveSpecificationBuilderConfiguration(depositCashInstrumentProviders, fraInstrumentProviders,
                                                                                                     liborRateInstrumentProviders, futureInstrumentProviders,
                                                                                                     swapInstrumentProviders, basisSwapInstrumentProviders, 
                                                                                                     tenorSwapInstrumentProviders);
    return config;
  }  
  
  public static CurveSpecificationBuilderConfiguration buildUSDCurveSpecificationBuilderConfiguration() {
    final Map<Tenor, CurveInstrumentProvider> liborRateInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    liborRateInstrumentProviders.put(Tenor.ofDays(1), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US00O/N Index")));
    liborRateInstrumentProviders.put(Tenor.ofDays(7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US0001W Index")));
    liborRateInstrumentProviders.put(Tenor.ofDays(14), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US0002W Index")));
    for (int i = 1; i <= 12; i++) {
      liborRateInstrumentProviders.put(Tenor.ofMonths(i), 
          new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US00" + (i < 10 ? "0" : "") + i + "M Index")));
    }

    final Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickersFRAs = new Object[][] {
      {THREE_MONTHS, "USFR00C Curncy"}, {SIX_MONTHS, "USFR0CF Curncy"}, {NINE_MONTHS, "USFR0FI Curncy"},
      {TWELVE_MONTHS, "USFR0I1 Curncy"}, {Tenor.ofMonths(15), "USFR011C Curncy" }, {Tenor.ofMonths(18), "USFR1C1F Curncy"},
      {Tenor.ofMonths(21), "USFR1F1I Curncy"}, {Tenor.ofMonths(24), "USFR1I2 Curncy"}
    };
    for (final Object[] tenorsTickersFRA : tenorsTickersFRAs) {
      final Tenor tenor = (Tenor) tenorsTickersFRA[0];
      final String ticker = (String) tenorsTickersFRA[1];
      fraInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
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
      depositCashInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    futureInstrumentProviders.put(Tenor.ofYears(0), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 1 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(Tenor.ofYears(1), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 1 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(Tenor.ofMonths(12), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 1.5 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(Tenor.ofMonths(18), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 2 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(Tenor.ofYears(2), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 2 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(Tenor.ofMonths(24), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 2 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(Tenor.ofYears(3), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));
    // note that these are start points, so 2 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(Tenor.ofMonths(36), new BloombergFutureCurveInstrumentProvider("ED", "Comdty"));

    final Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] availableYears = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 25, 30, 35, 40, 45, 50, 60 };
    for (final int i : availableYears) {
      swapInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("USSW" + i + " Curncy")));
    }
    
    final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] tenorAvailableYears = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30};
    for (final int i : tenorAvailableYears) {
      tenorSwapInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("USBG" + i + " Curncy")));
    }

    final CurveSpecificationBuilderConfiguration config = new CurveSpecificationBuilderConfiguration(depositCashInstrumentProviders, fraInstrumentProviders,
                                                                                                     liborRateInstrumentProviders, futureInstrumentProviders,
                                                                                                     swapInstrumentProviders, basisSwapInstrumentProviders, 
                                                                                                     tenorSwapInstrumentProviders);
    return config;
  }
  

  public static CurveSpecificationBuilderConfiguration buildTestConfiguration() {
    final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    cashInstrumentProviders.put(Tenor.ofDays(1), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US00O/N Curncy")));
    cashInstrumentProviders.put(Tenor.ofDays(7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US0001W Curncy")));

    final Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickersFRAs = new Object[][] {{THREE_MONTHS, "USFR00C Curncy"}, {SIX_MONTHS, "USFR0CF Curncy"}};
    for (final Object[] tenorsTickersFRA : tenorsTickersFRAs) {
      final Tenor tenor = (Tenor) tenorsTickersFRA[0];
      final String ticker = (String) tenorsTickersFRA[1];
      fraInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers = new Object[][]  {{ONE_DAY, "USDR1T Curncy" }, {TWO_DAYS, "USDR2T Curncy"}};

    for (final Object[] tenorsTicker : tenorsTickers) {
      final Tenor tenor = (Tenor) tenorsTicker[0];
      final String ticker = (String) tenorsTicker[1];
      rateInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    // note that these are start points, so 1 yr + (as many quarterly futures as you want)
    futureInstrumentProviders.put(Tenor.ofMonths(12), new BloombergFutureCurveInstrumentProvider("ED", "Curncy"));
    // note that these are start points, so 1.5 yr + (as many quarterly futures as you want)

    final Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] availableYears = {1, 2};
    for (final int i : availableYears) {
      swapInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("USSW" + i + " Curncy")));
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
