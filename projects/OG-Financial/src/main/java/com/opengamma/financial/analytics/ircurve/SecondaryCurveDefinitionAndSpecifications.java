/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.EURIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.LIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.STIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_3M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_6M;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * A secondary set of curve definitions using synthetic instrument identifiers, primarily useful for curves for 'fake' market data used during evaluation.
 */
public class SecondaryCurveDefinitionAndSpecifications {

  private static final String SPEC_NAME = "SECONDARY";

  public static YieldCurveDefinition buildFundingCurve(final Currency ccy, final ExternalId region, final Tenor[] depositStrips, final Tenor[] tenorSwaps) {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final Tenor depositTenor : depositStrips) {
      if (depositTenor.getPeriod().equals(Period.ofDays(30))) {
        throw new OpenGammaRuntimeException("This shouldn't happen!");
      }
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, depositTenor, SPEC_NAME));
    }
    for (final Tenor tenorSwapTenor : tenorSwaps) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.TENOR_SWAP, tenorSwapTenor, SPEC_NAME));
    }
    final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    final String rightExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    final boolean interpolateYields = true;    final YieldCurveDefinition definition = new YieldCurveDefinition(ccy, region, "FUNDING", Interpolator1DFactory.DOUBLE_QUADRATIC,
        leftExtrapolatorName, rightExtrapolatorName, interpolateYields, strips);
    return definition;
  }

  public static YieldCurveDefinition buildForwardCurve(final Currency ccy, final ExternalId region, final Tenor[] liborStrips, final Tenor futureStartTenor, final int numQuarterlyFutures,
      final Tenor[] swaps, final StripInstrumentType iborType, final StripInstrumentType swapType) {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final Tenor liborTenor : liborStrips) {
      strips.add(new FixedIncomeStrip(iborType, liborTenor, SPEC_NAME));
    }
    if (futureStartTenor != null) {
      for (int i = 1; i <= numQuarterlyFutures; i++) {
        strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, futureStartTenor, i, SPEC_NAME));
      }
    }
    for (final Tenor swapTenor : swaps) {
      strips.add(new FixedIncomeStrip(swapType, swapTenor, SPEC_NAME));
    }
    final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    final String rightExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    final boolean interpolateYields = true;    final YieldCurveDefinition definition = new YieldCurveDefinition(ccy, region, "FORWARD", Interpolator1DFactory.DOUBLE_QUADRATIC,
        leftExtrapolatorName, rightExtrapolatorName, interpolateYields, strips);
    return definition;
  }

  public static YieldCurveDefinition buildSecondaryCurve(final Currency ccy, final ExternalId region, final Tenor[] liborStrips, final Tenor futureStartTenor, final int numQuarterlyFutures,
      final Tenor[] swaps, final StripInstrumentType iborType, final StripInstrumentType swapType) {
    final Collection<FixedIncomeStrip> strips = new ArrayList<FixedIncomeStrip>();
    for (final Tenor liborTenor : liborStrips) {
      strips.add(new FixedIncomeStrip(StripInstrumentType.CASH, liborTenor, SPEC_NAME));
    }
    if (futureStartTenor != null) {
      for (int i = 1; i <= numQuarterlyFutures; i++) {
        strips.add(new FixedIncomeStrip(StripInstrumentType.FUTURE, futureStartTenor, i, SPEC_NAME));
      }
    }
    for (final Tenor tenorSwapTenor : swaps) {
      strips.add(new FixedIncomeStrip(swapType, tenorSwapTenor, SPEC_NAME));
    }
    final String leftExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    final String rightExtrapolatorName = Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
    final boolean interpolateYields = true;    final YieldCurveDefinition definition = new YieldCurveDefinition(ccy, region, "SECONDARY", Interpolator1DFactory.DOUBLE_QUADRATIC,
        leftExtrapolatorName, rightExtrapolatorName, interpolateYields, strips);
    return definition;
  }

  private static Tenor[] makeShortEnd(final boolean includeOvernight, final boolean includeSpotNext, final boolean include2W) {
    final List<Tenor> results = new ArrayList<Tenor>();
    if (includeOvernight) {
      results.add(Tenor.ofDays(1));
    }
    if (includeSpotNext) {
      results.add(Tenor.ofDays(3));
    }
    if (include2W) {
      results.add(Tenor.ofDays(14));
    }
    for (int i = 1; i <= 12; i++) {
      results.add(Tenor.ofMonths(i));
    }
    return results.toArray(new Tenor[] {});
  }

  private static Tenor[] makeLongEnd(final int firstContiguousYear, final int lastContiguousYear, final int[] nonContiguousYears) {
    Assert.isTrue(firstContiguousYear <= lastContiguousYear);
    final List<Tenor> results = new ArrayList<Tenor>();
    for (int i = firstContiguousYear; i <= lastContiguousYear; i++) {
      results.add(Tenor.ofYears(i));
    }
    for (final int i : nonContiguousYears) {
      results.add(Tenor.ofYears(i));
    }
    return results.toArray(new Tenor[] {});
  }

  public static Map<String, Map<Currency, YieldCurveDefinition>> buildSecondaryCurveDefinitions() {
    final Map<Currency, YieldCurveDefinition> singleDefinitions = new HashMap<Currency, YieldCurveDefinition>();
    final Currency usd = Currency.USD;
    final ExternalId usdRegion = ExternalSchemes.countryRegionId(Country.US);
    singleDefinitions.put(usd, buildSecondaryCurve(usd, usdRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), LIBOR, SWAP_3M));

    final Currency eur = Currency.EUR;
    final ExternalId eurRegion = ExternalSchemes.countryRegionId(Country.EU);
    singleDefinitions.put(eur, buildSecondaryCurve(eur, eurRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), EURIBOR, SWAP_6M));

    final Currency gbp = Currency.GBP;
    final ExternalId gbpRegion = ExternalSchemes.countryRegionId(Country.GB);
    singleDefinitions.put(gbp, buildSecondaryCurve(gbp, gbpRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), LIBOR, SWAP_6M));

    final Currency chf = Currency.CHF;
    final ExternalId chfRegion = ExternalSchemes.countryRegionId(Country.CH);
    singleDefinitions.put(chf, buildSecondaryCurve(chf, chfRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), LIBOR, SWAP_6M));

    final Currency aud = Currency.AUD;
    final ExternalId audRegion = ExternalSchemes.countryRegionId(Country.AU);
    singleDefinitions.put(aud, buildSecondaryCurve(aud, audRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), LIBOR, SWAP_6M));

    final Currency sek = Currency.SEK;
    final ExternalId sekRegion = ExternalSchemes.countryRegionId(Country.SE);
    singleDefinitions.put(sek, buildSecondaryCurve(sek, sekRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), STIBOR, SWAP_6M));

    final Currency nzd = Currency.NZD;
    final ExternalId nzdRegion = ExternalSchemes.countryRegionId(Country.NZ);
    singleDefinitions.put(nzd, buildSecondaryCurve(nzd, nzdRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 30}), LIBOR, SWAP_6M));

    final Currency cad = Currency.CAD;
    final ExternalId cadRegion = ExternalSchemes.countryRegionId(Country.CA);
    singleDefinitions.put(cad, buildSecondaryCurve(cad, cadRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), LIBOR, SWAP_6M));

    final Currency dkk = Currency.DKK;
    final ExternalId dkkRegion = ExternalSchemes.countryRegionId(Country.DK);
    singleDefinitions.put(dkk, buildSecondaryCurve(dkk, dkkRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), CIBOR, SWAP_6M));

    final Currency jpy = Currency.JPY;
    final ExternalId jpyRegion = ExternalSchemes.countryRegionId(Country.JP);
    singleDefinitions.put(jpy, buildSecondaryCurve(jpy, jpyRegion, makeShortEnd(true, false, false), null, 0, makeLongEnd(2, 10, new int[] {12, 15, 20, 25, 30, 40}), LIBOR, SWAP_6M));
    final Map<String, Map<Currency, YieldCurveDefinition>> results = new HashMap<String, Map<Currency, YieldCurveDefinition>>();
    results.put("SECONDARY", singleDefinitions);
    return results;
  }

  public static Map<Currency, CurveSpecificationBuilderConfiguration> buildSyntheticCurveSpecificationBuilderConfigurations() {
    final Map<Currency, CurveSpecificationBuilderConfiguration> configurations = new HashMap<Currency, CurveSpecificationBuilderConfiguration>();
    final ExternalScheme scheme = ExternalSchemes.OG_SYNTHETIC_TICKER;
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
    configurations.put(Currency.CHF, buildSyntheticCurveSpecificationBuilderConfiguration(Currency.CHF, scheme));
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

    final Tenor[] tenors = new Tenor[] {Tenor.ONE_DAY, Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, Tenor.SEVEN_MONTHS,
      Tenor.EIGHT_MONTHS, Tenor.NINE_MONTHS, Tenor.TEN_MONTHS, Tenor.ELEVEN_MONTHS, Tenor.TWELVE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FOUR_YEARS,
      Tenor.FIVE_YEARS, Tenor.ofYears(6), Tenor.ofYears(7), Tenor.ofYears(8), Tenor.ofYears(9), Tenor.ofYears(10), Tenor.ofYears(11), Tenor.ofYears(12), Tenor.ofYears(15),
      Tenor.ofYears(20), Tenor.ofYears(25), Tenor.ofYears(30), Tenor.ofYears(40), Tenor.ofYears(50), Tenor.ofYears(80)};

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
      swap3MInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.SWAP_3M, scheme));
      swap6MInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.SWAP_6M, scheme));
      basisSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.BASIS_SWAP, scheme));
      oisSwapInstrumentProviders.put(tenor, new SyntheticIdentifierCurveInstrumentProvider(ccy, StripInstrumentType.OIS_SWAP, scheme));
    }
    final CurveSpecificationBuilderConfiguration config = new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fra3MInstrumentProviders, fra6MInstrumentProviders,
        liborInstrumentProviders, euriborInstrumentProviders, cdorInstrumentProviders, ciborInstrumentProviders, stiborInstrumentProviders, futureInstrumentProviders, swap6MInstrumentProviders,
        swap3MInstrumentProviders, basisSwapInstrumentProviders, tenorSwapInstrumentProviders, oisSwapInstrumentProviders, null, null, null, null, null);
    return config;
  }

}
