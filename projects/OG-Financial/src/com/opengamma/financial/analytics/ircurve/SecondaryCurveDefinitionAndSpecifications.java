/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
 * A secondary set of curve definitions using synthetic instrument identifiers, primarily useful for curves for 'fake' market data used during evaluation.
 */
public class SecondaryCurveDefinitionAndSpecifications {
 

  public static YieldCurveDefinition buildFundingCurve(Currency ccy, Identifier region, Tenor[] depositStrips, Tenor[] tenorSwaps) {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (Tenor depositTenor : depositStrips) {
      if (depositTenor.getPeriod().equals(Period.ofDays(30))) {
        throw new OpenGammaRuntimeException("This shouldn't happen!");
      }
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, depositTenor, "DEFAULT"));
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
    for (Tenor swapTenor : swaps) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP, swapTenor, "DEFAULT"));
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
  
  public static Map<String, Map<Currency, YieldCurveDefinition>> buildSecondaryCurveDefintions() {
    //Map<Currency, YieldCurveDefinition> forwardDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    //Map<Currency, YieldCurveDefinition> fundingDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    Map<Currency, YieldCurveDefinition> singleDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    Currency usd = Currency.USD;
    Identifier usdRegion = RegionUtils.countryRegionId("US");
    singleDefinitions.put(usd, buildSingleCurve(usd, usdRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 12, new int[] {15, 20, 25, 30, 40 })));
    
    Currency eur = Currency.EUR;
    Identifier eurRegion = RegionUtils.countryRegionId("EU");
    singleDefinitions.put(eur, buildSingleCurve(eur, eurRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 12, new int[] {15, 20, 25, 30, 40 })));
    
    Currency gbp = Currency.GBP;
    Identifier gbpRegion = RegionUtils.countryRegionId("GB");
    singleDefinitions.put(gbp, buildSingleCurve(gbp, gbpRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 12, new int[] {15, 20, 25, 30, 40 })));
    
    Currency chf = Currency.CHF;
    Identifier chfRegion = RegionUtils.countryRegionId("CH");
    singleDefinitions.put(chf, buildSingleCurve(chf, chfRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 12, new int[] {15, 20, 25, 30, 40 })));
    
    Currency aud = Currency.AUD;
    Identifier audRegion = RegionUtils.countryRegionId("AU");
    singleDefinitions.put(aud, buildSingleCurve(aud, audRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 12, new int[] {15, 20, 25, 30, 40 })));
    
    Currency sek = Currency.of("SEK");
    Identifier sekRegion = RegionUtils.countryRegionId("SE");
    singleDefinitions.put(sek, buildSingleCurve(sek, sekRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 12, new int[] {15, 20, 25, 30, 40 })));
    
    Currency nzd = Currency.of("NZD");
    Identifier nzdRegion = RegionUtils.countryRegionId("NZ");
    singleDefinitions.put(nzd, buildSingleCurve(nzd, nzdRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 30 })));
    
    Currency cad = Currency.CAD;
    Identifier cadRegion = RegionUtils.countryRegionId("CA");
    singleDefinitions.put(cad, buildSingleCurve(cad, cadRegion, makeShortEnd(true, false, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50 })));
    
    Currency dkk = Currency.of("DKK");
    Identifier dkkRegion = RegionUtils.countryRegionId("DK");
    singleDefinitions.put(dkk, buildSingleCurve(dkk, dkkRegion, makeShortEnd(false, true, true), null, 0, makeLongEnd(2, 15, new int[] {20, 25, 30, 40, 50 })));
    
    Currency jpy = Currency.JPY;
    Identifier jpyRegion = RegionUtils.countryRegionId("JP");
    singleDefinitions.put(jpy, buildSingleCurve(jpy, jpyRegion, makeShortEnd(false, true, true), Tenor.ofYears(1), 3, makeLongEnd(2, 15, new int[] {20, 25, 30, 35, 40, 50 })));
    
    Map<String, Map<Currency, YieldCurveDefinition>> results = new HashMap<String, Map<Currency, YieldCurveDefinition>>();
    //results.put("FORWARD", forwardDefinitions);
    //results.put("FUNDING", fundingDefinitions);
    results.put("SECONDARY", singleDefinitions);
    return results;
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
    Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    
    Tenor[] tenors = new Tenor[] {Tenor.DAY, Tenor.MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, 
                                  Tenor.SEVEN_MONTHS, Tenor.EIGHT_MONTHS, Tenor.NINE_MONTHS, Tenor.TEN_MONTHS, Tenor.ELEVEN_MONTHS, Tenor.TWELVE_MONTHS,
                                  Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.ofYears(6), Tenor.ofYears(7),
                                  Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10), Tenor.ofYears(11), Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20),
                                  Tenor.ofYears(25), Tenor.ofYears(30), Tenor.ofYears(40)}; //, Tenor.ofYears(50), Tenor.ofYears(80)};
    
    for (Tenor tenor : tenors) {
      cashInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.CASH, scheme));
      fraInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.FRA, scheme));
      rateInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.LIBOR, scheme));
      futureInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.FUTURE, scheme));
      tenorSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.TENOR_SWAP, scheme));
      swapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.SWAP, scheme));
      basisSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.BASIS_SWAP, scheme));
      oisSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.OIS_SWAP, scheme));
    }
    CurveSpecificationBuilderConfiguration config = new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, 
                                                                                               fraInstrumentProviders, 
                                                                                               rateInstrumentProviders, 
                                                                                               futureInstrumentProviders, 
                                                                                               swapInstrumentProviders, 
                                                                                               basisSwapInstrumentProviders, 
                                                                                               tenorSwapInstrumentProviders,
                                                                                               oisSwapInstrumentProviders);
    return config;
  }

}
