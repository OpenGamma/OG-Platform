/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.core.region.RegionUtils;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalScheme;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CurveDefinitionAndSpecifications {
  private static final String[] BBG_MONTH_CODES = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};

  public static YieldCurveDefinition buildUSDSwapOnlyCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.ofYears(i), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId(Country.US), "SWAP_ONLY", interpolatorName, strips);
    return definition;
  }

  public static YieldCurveDefinition buildUSDSwapOnlyNo3YrCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {1, 2, 4, 5, 6, 7, 10, 15, 20, 25, 30};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.ofYears(i), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId(Country.US), "SWAP_ONLY_NO3YR", interpolatorName, strips);
    return definition;
  }

  public static YieldCurveDefinition buildUSDSwapOnly3YrCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    final int[] tenors = new int[] {3};
    for (final int i : tenors) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.ofYears(i), "DEFAULT"));
    }
    final String interpolatorName = Interpolator1DFactory.DOUBLE_QUADRATIC;
    final YieldCurveDefinition definition = new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId(Country.US), "SWAP_ONLY_3YR", interpolatorName, strips);
    return definition;
  }

  public static YieldCurveDefinition buildUSDFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    //    for (final int i : new int[] {1, 2}) {
    //      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    //    }
    for (final int i : new int[] {2}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 9}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 10}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId(Country.US), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildUSDThreeMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    //    for (final int i : new int[] {1, 7, 14}) {
    //      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(i), "DEFAULT"));
    //    }
    for (final int i : new int[] {7, 14}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (int i = 1; i < 7; i++) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.ofYears(0), i, "DEFAULT"));
    }
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.USD, RegionUtils.countryRegionId(Country.US), "FORWARD_3M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildEURFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {7, 14, 21}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.EUR, RegionUtils.financialRegionId("EU"), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildEURSixMonthForwardCurveDefinition() { //TODO use euribor somewhere in the name
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {7, 14, 21}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.EURIBOR, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.EURIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {9, 12, 15, 18}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FRA_6M, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.EUR, RegionUtils.financialRegionId("EU"), "FORWARD_6M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildGBPFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    //    for (final int i : new int[] {1, 2, 7, 14, 21}) {
    //      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    //    }
    for (final int i : new int[] {2, 7, 14, 21}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.GBP, RegionUtils.countryRegionId(Country.GB), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildGBPThreeMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    //    for (final int i : new int[] {1}) {
    //      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(i), "DEFAULT"));
    //    }
    for (final int i : new int[] {1, 2, 3}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.ofMonths(0), i, "DEFAULT"));
    }
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.GBP, RegionUtils.countryRegionId(Country.GB), "FORWARD_3M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildGBPSixMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    //    for (final int i : new int[] {1}) {
    //      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(i), "DEFAULT"));
    //    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {9, 12, 15}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FRA_6M, Tenor.ofMonths(i), "DEFAULT"));
    }
    strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofMonths(18), "DEFAULT"));
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.GBP, RegionUtils.countryRegionId(Country.GB), "FORWARD_6M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildCHFFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {1, 2, 7, 14, 21}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.CHF, RegionUtils.countryRegionId(Country.CH), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildCHFSixMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {1}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {9, 12, 15}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FRA_6M, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.CHF, RegionUtils.countryRegionId(Country.CH), "FORWARD_6M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildJPYFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    //    for (final int i : new int[] {1, 2, 7, 14, 21}) {
    //      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    //    }
    for (final int i : new int[] {2, 7, 14, 21}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.JPY, RegionUtils.countryRegionId(Country.JP), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildJPYSixMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    //    for (final int i : new int[] {1}) {
    //      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(i), "DEFAULT"));
    //    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {9, 12, 15}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.FRA_6M, Tenor.ofMonths(i), "DEFAULT"));
    }
    strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofMonths(18), "DEFAULT"));
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.JPY, RegionUtils.countryRegionId(Country.JP), "FORWARD_6M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildCADFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {1, 2, 7, 14, 21}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {3, 4, 5}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.CAD, RegionUtils.countryRegionId(Country.CA), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildCADSixMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {6}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CDOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofYears(1), "DEFAULT"));
    for (final int i : new int[] {15, 18}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.CAD, RegionUtils.countryRegionId(Country.CA), "FORWARD_6M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildAUDFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {1}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.AUD, RegionUtils.countryRegionId(Country.AU), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildAUDSixMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {7}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 6}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.AUD, RegionUtils.countryRegionId(Country.AU), "FORWARD_6M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildNZDFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {2}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.NZD, RegionUtils.countryRegionId(Country.NZ), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildNZDThreeMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {1, 2, 3, 6}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.NZD, RegionUtils.countryRegionId(Country.NZ), "FORWARD_3M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildDKKFundingCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {1, 2}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.ofDays(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.DKK, RegionUtils.countryRegionId(Country.DK), "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static YieldCurveDefinition buildDKKSixMonthForwardCurveDefinition() {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final int i : new int[] {1, 2, 3, 6}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CIBOR, Tenor.ofMonths(i), "DEFAULT"));
    }
    for (final int i : new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20}) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.ofYears(i), "DEFAULT"));
    }
    return new YieldCurveDefinition(Currency.DKK, RegionUtils.countryRegionId(Country.DK), "FORWARD_6M", Interpolator1DFactory.DOUBLE_QUADRATIC, strips);
  }

  public static Map<String, Map<Currency, YieldCurveDefinition>> buildStandardCurveDefinitions() {
    final Map<Currency, YieldCurveDefinition> forward3MDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    final Map<Currency, YieldCurveDefinition> forward6MDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    final Map<Currency, YieldCurveDefinition> fundingDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    forward3MDefinitions.put(Currency.USD, buildUSDThreeMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.USD, buildUSDFundingCurveDefinition());
    forward6MDefinitions.put(Currency.EUR, buildEURSixMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.EUR, buildEURFundingCurveDefinition());
    forward6MDefinitions.put(Currency.GBP, buildGBPSixMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.GBP, buildGBPFundingCurveDefinition());
    forward6MDefinitions.put(Currency.JPY, buildJPYSixMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.JPY, buildJPYFundingCurveDefinition());
    forward6MDefinitions.put(Currency.CHF, buildCHFSixMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.CHF, buildCHFFundingCurveDefinition());
    forward6MDefinitions.put(Currency.CAD, buildCADSixMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.CAD, buildCADFundingCurveDefinition());
    forward6MDefinitions.put(Currency.AUD, buildAUDSixMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.AUD, buildAUDFundingCurveDefinition());
    forward3MDefinitions.put(Currency.NZD, buildNZDThreeMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.NZD, buildNZDFundingCurveDefinition());
    forward6MDefinitions.put(Currency.DKK, buildDKKSixMonthForwardCurveDefinition());
    fundingDefinitions.put(Currency.DKK, buildDKKFundingCurveDefinition());
    final Map<String, Map<Currency, YieldCurveDefinition>> definitions = new HashMap<String, Map<Currency, YieldCurveDefinition>>();
    definitions.put("FORWARD_3M", forward3MDefinitions);
    definitions.put("FORWARD_6M", forward6MDefinitions);
    definitions.put("FUNDING", fundingDefinitions);
    return definitions;
  }

  public static Map<Currency, CurveSpecificationBuilderConfiguration> buildStandardCurveSpecificationBuilderConfigurations() {
    final Map<Currency, CurveSpecificationBuilderConfiguration> configurations = new HashMap<Currency, CurveSpecificationBuilderConfiguration>();
    final CurveSpecificationBuilderConfiguration usdConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("US"),
        buildStandardBloomberg3MFRAInstrumentProvider("US"), buildStandardBloomberg6MFRAInstrumentProvider("US"), buildStandardBloombergLiborInstrumentProvider("US", "O/N", "T/N"), null, null, null,
        null, buildStandardBloombergFutureInstrumentProvider("ED"), null, buildStandardBloomberg3MSwapInstrumentProvider("US", ""), null, null, buildStandardBloombergOISSwapInstrumentProvider("USSO"));
    configurations.put(Currency.USD, usdConfig);
    final CurveSpecificationBuilderConfiguration eurConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("EU"),
        buildStandardBloomberg3MFRAInstrumentProvider("EU"), buildStandardBloomberg6MFRAInstrumentProvider("EU"), null, buildStandardBloombergEuriborInstrumentProvider(), null, null, null,
        buildStandardBloombergFutureInstrumentProvider("ER"), buildStandardBloomberg6MSwapInstrumentProvider("EUSA"), buildStandardBloomberg3MSwapInstrumentProvider("EU", "V3"), null, null,
        buildStandardBloombergOISSwapInstrumentProvider("EUSWE"));
    configurations.put(Currency.EUR, eurConfig);
    final CurveSpecificationBuilderConfiguration gbpConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("BP"),
        buildStandardBloomberg3MFRAInstrumentProvider("BP"), buildStandardBloomberg6MFRAInstrumentProvider("BP"), buildStandardBloombergLiborInstrumentProvider("BP", "O/N", "T/N"), null, null, null,
        null, buildStandardBloombergFutureInstrumentProvider("L "), buildStandardBloomberg6MSwapInstrumentProvider("BPSW"), null, null, null, buildStandardBloombergOISSwapInstrumentProvider("BPSWS"));
    configurations.put(Currency.GBP, gbpConfig);
    final CurveSpecificationBuilderConfiguration jpyConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("JY"), null,
        buildStandardBloombergJPY6MFRAInstrumentProvider(), buildStandardBloombergLiborInstrumentProvider("JY", "S/N", "T/N"), null, null, null, null,
        buildStandardBloombergFutureInstrumentProvider("EF"), buildStandardBloomberg6MSwapInstrumentProvider("JYSW"), null, null, null, buildStandardBloombergOISSwapInstrumentProvider("JYSO"));
    configurations.put(Currency.JPY, jpyConfig);
    final CurveSpecificationBuilderConfiguration chfConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("SF"),
        buildStandardBloomberg3MFRAInstrumentProvider("SF"), buildStandardBloomberg6MFRAInstrumentProvider("SF"), buildStandardBloombergLiborInstrumentProvider("SF", "S/N", "T/N"), null, null, null,
        null, buildStandardBloombergFutureInstrumentProvider("ES"), buildStandardBloomberg6MSwapInstrumentProvider("SFSW"), null, null, null, buildStandardBloombergOISSwapInstrumentProvider("SFSWT"));
    configurations.put(Currency.CHF, chfConfig);
    final CurveSpecificationBuilderConfiguration cadConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("CD"),
        buildStandardBloomberg3MFRAInstrumentProvider("CD"), buildStandardBloomberg6MFRAInstrumentProvider("CD"), null, null, buildStandardBloombergCDORInstrumentProvider(), null, null,
        buildStandardBloombergFutureInstrumentProvider("BA"), buildStandardBloomberg6MSwapInstrumentProvider("CDSW"), null, null, null, buildStandardBloombergOISSwapInstrumentProvider("CDSO"));
    configurations.put(Currency.CAD, cadConfig);
    final CurveSpecificationBuilderConfiguration audConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("AD"),
        buildStandardBloomberg3MFRAInstrumentProvider("AD"), buildStandardBloomberg6MFRAInstrumentProvider("AD"), buildStandardBloombergLiborInstrumentProvider("AU", "O/N", "T/N"), null, null, null,
        null, null, buildStandardBloomberg6MSwapInstrumentProvider("ADSW"), null, null, null, buildStandardBloombergOISSwapInstrumentProvider("ADSO"));
    configurations.put(Currency.AUD, audConfig);
    final CurveSpecificationBuilderConfiguration nzdConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("ND"),
        buildStandardBloomberg3MFRAInstrumentProvider("ND"), buildStandardBloomberg6MFRAInstrumentProvider("ND"), buildStandardBloombergLiborInstrumentProvider("NZ", "O/N", "T/N"), null, null, null,
        null, null, null, buildStandardBloomberg3MSwapInstrumentProvider("ND", ""), null, null, buildStandardBloombergOISSwapInstrumentProvider("NDSO"));
    configurations.put(Currency.NZD, nzdConfig);
    final CurveSpecificationBuilderConfiguration dkkConfig = new CurveSpecificationBuilderConfiguration(buildStandardBloombergDepositInstrumentProvider("DK"), null, null, null, null, null,
        buildStandardBloombergCiborInstrumentProvider(), null, null, buildStandardBloomberg6MSwapInstrumentProvider("DKSW"), null, null, null,
        buildStandardBloombergOISSwapInstrumentProvider("DKSWTN"));
    configurations.put(Currency.DKK, dkkConfig);
    return configurations;
  }

  public static Map<Currency, CurveSpecificationBuilderConfiguration> buildSyntheticCurveSpecificationBuilderConfigurations() {
    final Map<Currency, CurveSpecificationBuilderConfiguration> configurations = new HashMap<Currency, CurveSpecificationBuilderConfiguration>();
    final ExternalScheme scheme = SecurityUtils.OG_SYNTHETIC_TICKER;
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
    configurations.put(Currency.NZD, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.NZD, scheme));
    return configurations;
  }

  private static CurveSpecificationBuilderConfiguration buildSyntheticCurveSpecificationBuilderConfiguration(final Currency ccy, final ExternalScheme scheme) {
    final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> fra3MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> fra6MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> liborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> euriborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> cdorInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> ciborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> stiborInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> swap6MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> swap3MInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();

    final Tenor[] tenors = new Tenor[] {Tenor.DAY, Tenor.MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, Tenor.SEVEN_MONTHS, Tenor.EIGHT_MONTHS,
        Tenor.NINE_MONTHS, Tenor.TEN_MONTHS, Tenor.ELEVEN_MONTHS, Tenor.TWELVE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.ofYears(6),
        Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10), Tenor.ofYears(11), Tenor.ofYears(12), Tenor.ofYears(15), Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30),
        Tenor.ofYears(40), Tenor.ofYears(50), Tenor.ofYears(80)};

    for (final Tenor tenor : tenors) {
      cashInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.CASH, scheme));
      fra3MInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.FRA_3M, scheme));
      fra6MInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.FRA_6M, scheme));
      liborInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.LIBOR, scheme));
      euriborInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.EURIBOR, scheme));
      cdorInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.CDOR, scheme));
      ciborInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.CIBOR, scheme));
      stiborInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.STIBOR, scheme));
      futureInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.FUTURE, scheme));
      tenorSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.TENOR_SWAP, scheme));
      swap6MInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.SWAP_6M, scheme));
      swap3MInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.SWAP_3M, scheme));
      basisSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.BASIS_SWAP, scheme));
      oisSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.OIS_SWAP, scheme));
    }
    final CurveSpecificationBuilderConfiguration config = new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fra3MInstrumentProviders, fra6MInstrumentProviders,
        liborInstrumentProviders, euriborInstrumentProviders, cdorInstrumentProviders, ciborInstrumentProviders, stiborInstrumentProviders, futureInstrumentProviders, swap6MInstrumentProviders,
        swap3MInstrumentProviders, basisSwapInstrumentProviders, tenorSwapInstrumentProviders, oisSwapInstrumentProviders);
    return config;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergDepositInstrumentProvider(final String prefix) {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    provider.put(Tenor.ofDays(1), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "DR1T Curncy")));
    provider.put(Tenor.ofDays(2), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "DR2T Curncy")));
    provider.put(Tenor.ofDays(3), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "DR3T Curncy")));
    for (int i = 1; i < 4; i++) {
      provider.put(Tenor.ofDays(i * 7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "DR" + i + "Z Curncy")));
    }
    for (int i = 1; i < 12; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "DR" + BBG_MONTH_CODES[i - 1] + " Curncy")));
    }
    for (int i = 1; i < 51; i++) {
      provider.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "DR" + i + " Curncy")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergLiborInstrumentProvider(final String prefix, final String overnightString, final String twoDayString) {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    provider.put(Tenor.ofDays(1), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "00" + overnightString + " Index")));
    provider.put(Tenor.ofDays(2), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "00" + twoDayString + " Index")));
    for (int i = 1; i < 4; i++) {
      provider.put(Tenor.ofDays(i * 7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "000" + i + "W Index")));
    }
    for (int i = 1; i < 10; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "000" + i + "M Index")));
    }
    for (int i = 10; i < 13; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "00" + i + "M Index")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergEuriborInstrumentProvider() {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    for (int i = 1; i < 4; i++) {
      provider.put(Tenor.ofDays(i * 7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("EUR00" + i + "W Index")));
    }
    for (int i = 1; i < 10; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("EUR00" + i + "M Index")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergCDORInstrumentProvider() {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    for (int i = 1; i < 7; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("CDOR0" + i + " RBC Index")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergCiborInstrumentProvider() {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    for (int i = 1; i < 4; i++) {
      provider.put(Tenor.ofDays(i * 7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("CIBO0" + i + "W Index")));
    }
    for (int i = 1; i < 10; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("CIBO0" + i + "M Index")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergStiborInstrumentProvider() {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    provider.put(Tenor.ofDays(1), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("STIB1D Index")));
    for (int i = 1; i < 4; i++) {
      provider.put(Tenor.ofDays(i * 7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("STIB" + i + "W Index")));
    }
    for (int i = 1; i < 10; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("STIB" + i + "M Index")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloomberg3MFRAInstrumentProvider(final String prefix) {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    provider.put(Tenor.ofMonths(3), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "FR00C Curncy")));
    for (int i = 1; i < 22; i++) {
      final int year = i / 12;
      final int month = i % 12;
      String startTenor, endTenor3M;
      if (month == 0) {
        startTenor = "0" + String.valueOf(year);
      } else {
        startTenor = String.valueOf(year) + BBG_MONTH_CODES[month - 1];
      }
      final int endYear3M = (i + 3) / 12;
      final int endMonth3M = (i + 3) % 12;
      if (endMonth3M == 0) {
        endTenor3M = String.valueOf(endYear3M);
      } else {
        endTenor3M = (endYear3M > 0 ? String.valueOf(endYear3M) : "") + BBG_MONTH_CODES[endMonth3M - 1];
      }
      provider.put(Tenor.ofMonths(i + 3), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "FR" + startTenor + endTenor3M + " Curncy")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloomberg6MFRAInstrumentProvider(final String prefix) {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    provider.put(Tenor.ofMonths(6), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "FR00F Curncy")));
    for (int i = 1; i < 22; i++) {
      final int year = i / 12;
      final int month = i % 12;
      String startTenor, endTenor6M;
      if (month == 0) {
        startTenor = "0" + String.valueOf(year);
      } else {
        startTenor = String.valueOf(year) + BBG_MONTH_CODES[month - 1];
      }
      final int endYear6M = (i + 6) / 12;
      final int endMonth6M = (i + 6) % 12;
      if (endMonth6M == 0) {
        endTenor6M = String.valueOf(endYear6M);
      } else {
        endTenor6M = (endYear6M > 0 ? String.valueOf(endYear6M) : "") + BBG_MONTH_CODES[endMonth6M - 1];
      }
      provider.put(Tenor.ofMonths(i + 6), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "FR" + startTenor + endTenor6M + " Curncy")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergJPY6MFRAInstrumentProvider() {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    for (int i = 0; i < 22; i++) {
      provider.put(Tenor.ofMonths(i + 6), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("JYFR" + i + "/" + (i + 6) + " Curncy")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergFutureInstrumentProvider(final String futurePrefix) {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    provider.put(Tenor.ofYears(0), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    // note that these are start points, so 1 yr + (as many quarterly futures as you want)
    provider.put(Tenor.ofYears(1), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    // note that these are start points, so 1 yr + (as many quarterly futures as you want)
    provider.put(Tenor.ofMonths(12), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    provider.put(Tenor.ofMonths(18), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    provider.put(Tenor.ofYears(2), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    provider.put(Tenor.ofMonths(24), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    provider.put(Tenor.ofYears(3), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    provider.put(Tenor.ofMonths(36), new BloombergFutureCurveInstrumentProvider(futurePrefix, "Comdty"));
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloomberg6MSwapInstrumentProvider(final String prefix) {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    for (int i = 1; i < 12; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + BBG_MONTH_CODES[i - 1] + " Curncy")));
    }
    for (int i = 12; i < 60; i += 3) {
      final int year = i / 12;
      final int month = i % 12;
      if (month == 0) {
        provider.put(Tenor.ofYears(year), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + year + " Curncy")));
      } else {
        final String code = year + BBG_MONTH_CODES[month - 1];
        provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + code + " Curncy")));
      }
    }
    for (int i = 5; i < 61; i++) {
      provider.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + i + " Curncy")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloomberg3MSwapInstrumentProvider(final String prefix, final String postfix) {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    for (int i = 1; i < 12; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "SW" + BBG_MONTH_CODES[i - 1] + postfix + " Curncy")));
    }
    for (int i = 12; i < 34; i += 3) {
      final int year = i / 12;
      final int month = i % 12;
      if (month == 0) {
        provider.put(Tenor.ofYears(year), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "SW" + year + postfix + " Curncy")));
      } else {
        final String code = year + BBG_MONTH_CODES[month - 1];
        provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "SW" + code + postfix + " Curncy")));
      }
    }
    for (int i = 3; i < 51; i++) {
      provider.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + "SW" + i + postfix + " Curncy")));
    }
    return provider;
  }

  public static Map<Tenor, CurveInstrumentProvider> buildStandardBloombergOISSwapInstrumentProvider(final String prefix) {
    final Map<Tenor, CurveInstrumentProvider> provider = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    for (int i = 1; i < 4; i++) {
      provider.put(Tenor.ofDays(i * 7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + i + "Z Curncy")));
    }
    for (int i = 1; i < 12; i++) {
      provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + BBG_MONTH_CODES[i - 1] + " Curncy")));
    }
    for (int i = 12; i < 36; i += 3) {
      final int year = i / 12;
      final int month = i % 12;
      if (month == 0) {
        provider.put(Tenor.ofYears(year), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + year + " Curncy")));
      } else {
        final String code = year + BBG_MONTH_CODES[month - 1];
        provider.put(Tenor.ofMonths(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + code + " Curncy")));
      }
    }
    for (int i = 3; i < 51; i++) {
      provider.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(prefix + i + " Curncy")));
    }
    return provider;
  }
}
