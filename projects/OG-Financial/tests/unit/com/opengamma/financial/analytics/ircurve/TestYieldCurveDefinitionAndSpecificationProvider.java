/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class TestYieldCurveDefinitionAndSpecificationProvider {

  public static CurveSpecificationBuilderConfiguration buildOldTestCurveConfiguration() {
    final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    cashInstrumentProviders.put(Tenor.ofDays(1), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US00O/N Curncy")));
    cashInstrumentProviders.put(Tenor.ofDays(7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US0001W Curncy")));

    final Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickersFRAs = new Object[][] { {Tenor.THREE_MONTHS, "USFR00C Curncy"}, {Tenor.SIX_MONTHS, "USFR0CF Curncy"}};
    for (final Object[] tenorsTickersFRA : tenorsTickersFRAs) {
      final Tenor tenor = (Tenor) tenorsTickersFRA[0];
      final String ticker = (String) tenorsTickersFRA[1];
      fraInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers = new Object[][] { {Tenor.ONE_MONTH, "US0001M Index"}, {Tenor.THREE_MONTHS, "US0003M Index"}};

    for (final Object[] tenorsTicker : tenorsTickers) {
      final Tenor tenor = (Tenor) tenorsTicker[0];
      final String ticker = (String) tenorsTicker[1];
      rateInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    futureInstrumentProviders.put(Tenor.ofMonths(12), new BloombergFutureCurveInstrumentProvider("ED", "Curncy"));

    final Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] availableYears = {1, 2};
    for (final int i : availableYears) {
      swapInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("USSW" + i + " Curncy")));
    }

    final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    return new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fraInstrumentProviders, null, rateInstrumentProviders, null, null, futureInstrumentProviders, null, swapInstrumentProviders,
        basisSwapInstrumentProviders, tenorSwapInstrumentProviders, oisSwapInstrumentProviders);
  }

  public static CurveSpecificationBuilderConfiguration buildTestUSDCurveConfiguration() {
    final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    cashInstrumentProviders.put(Tenor.ofDays(1), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US00O/N Curncy")));
    cashInstrumentProviders.put(Tenor.ofDays(7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("US0001W Curncy")));

    final Map<Tenor, CurveInstrumentProvider> fra3MInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers3MFRAs = new Object[][] { {Tenor.THREE_MONTHS, "USFR00C Curncy"}, {Tenor.SIX_MONTHS, "USFR0CF Curncy"}};
    for (final Object[] tenorsTickers3MFRA : tenorsTickers3MFRAs) {
      final Tenor tenor = (Tenor) tenorsTickers3MFRA[0];
      final String ticker = (String) tenorsTickers3MFRA[1];
      fra3MInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> fra6MInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers6MFRAs = new Object[][] { {Tenor.SIX_MONTHS, "USFR00F Curncy"}, {Tenor.NINE_MONTHS, "USFR0CI Curncy"}};
    for (final Object[] tenorsTickers6MFRA : tenorsTickers6MFRAs) {
      final Tenor tenor = (Tenor) tenorsTickers6MFRA[0];
      final String ticker = (String) tenorsTickers6MFRA[1];
      fra3MInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> liborInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers = new Object[][] { {Tenor.ONE_MONTH, "US0001M Index"}, {Tenor.THREE_MONTHS, "US0003M Index"}};

    for (final Object[] tenorsTicker : tenorsTickers) {
      final Tenor tenor = (Tenor) tenorsTicker[0];
      final String ticker = (String) tenorsTicker[1];
      liborInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    futureInstrumentProviders.put(Tenor.ofMonths(12), new BloombergFutureCurveInstrumentProvider("ED", "Curncy"));

    final Map<Tenor, CurveInstrumentProvider> swap3MInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] availableYears = {1, 2};
    for (final int i : availableYears) {
      swap3MInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("USSW" + i + " Curncy")));
    }

    final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    return new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fra3MInstrumentProviders, fra6MInstrumentProviders, liborInstrumentProviders, null, futureInstrumentProviders, null,
        null, swap3MInstrumentProviders, basisSwapInstrumentProviders, tenorSwapInstrumentProviders, oisSwapInstrumentProviders);
  }

  public static CurveSpecificationBuilderConfiguration buildTestEURCurveConfiguration() {
    final Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    cashInstrumentProviders.put(Tenor.ofDays(7), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("EUR001W Curncy")));

    final Map<Tenor, CurveInstrumentProvider> fra3MInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers3MFRAs = new Object[][] { {Tenor.THREE_MONTHS, "EUFR00C Curncy"}, {Tenor.SIX_MONTHS, "EUFR0CF Curncy"}};
    for (final Object[] tenorsTickers3MFRA : tenorsTickers3MFRAs) {
      final Tenor tenor = (Tenor) tenorsTickers3MFRA[0];
      final String ticker = (String) tenorsTickers3MFRA[1];
      fra3MInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> fra6MInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsTickers6MFRAs = new Object[][] { {Tenor.SIX_MONTHS, "EUFR00F Curncy"}, {Tenor.NINE_MONTHS, "EUFR0CI Curncy"}};
    for (final Object[] tenorsTickers6MFRA : tenorsTickers6MFRAs) {
      final Tenor tenor = (Tenor) tenorsTickers6MFRA[0];
      final String ticker = (String) tenorsTickers6MFRA[1];
      fra3MInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }
    final Map<Tenor, CurveInstrumentProvider> liborInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsLiborTickers = new Object[][] { {Tenor.ONE_WEEK, "EU0003W Index"}, {Tenor.ONE_MONTH, "EU0001M Index"}};

    for (final Object[] tenorsLiborTicker : tenorsLiborTickers) {
      final Tenor tenor = (Tenor) tenorsLiborTicker[0];
      final String ticker = (String) tenorsLiborTicker[1];
      liborInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> euriborInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Object[][] tenorsEuriborTickers = new Object[][] { {Tenor.ONE_WEEK, "EUR003W Index"}, {Tenor.ONE_MONTH, "EUR001M Index"}};

    for (final Object[] tenorsEuriborTicker : tenorsEuriborTickers) {
      final Tenor tenor = (Tenor) tenorsEuriborTicker[0];
      final String ticker = (String) tenorsEuriborTicker[1];
      euriborInstrumentProviders.put(tenor, new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId(ticker)));
    }

    final Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    futureInstrumentProviders.put(Tenor.ofMonths(12), new BloombergFutureCurveInstrumentProvider("ER", "Curncy"));

    final Map<Tenor, CurveInstrumentProvider> swap3MInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] available3MYears = {1, 2};
    for (final int i : available3MYears) {
      swap3MInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("EUSW" + i + "V3 Curncy")));
    }

    final Map<Tenor, CurveInstrumentProvider> swap6MInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final int[] available6MYears = {1, 2};
    for (final int i : available6MYears) {
      swap3MInstrumentProviders.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(SecurityUtils.bloombergTickerSecurityId("EUSA" + i + " Curncy")));
    }

    final Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    final Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = new LinkedHashMap<Tenor, CurveInstrumentProvider>();
    return new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fra3MInstrumentProviders, fra6MInstrumentProviders, liborInstrumentProviders, euriborInstrumentProviders,
        null, futureInstrumentProviders, swap6MInstrumentProviders, swap3MInstrumentProviders, basisSwapInstrumentProviders, tenorSwapInstrumentProviders, oisSwapInstrumentProviders);
  }
}
