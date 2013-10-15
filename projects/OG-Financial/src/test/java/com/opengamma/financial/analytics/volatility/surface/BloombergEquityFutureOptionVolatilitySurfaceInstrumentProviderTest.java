/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergEquityFutureOptionVolatilitySurfaceInstrumentProviderTest extends FinancialTestBase {

  private static final String PREFIX = "SP";
  private static final String POSTFIX = "Index";
  private static final LocalDate DATE = LocalDate.of(2013, 2, 28);
  private static final List<Pair<Integer, Tenor>> N_OPTION = new ArrayList<>();
  private static final double[] STRIKES = new double[] {110, 120, 130, 140, 150};
  private static final double SPOT = 131;
  private static final String DATA_FIELD_NAME = MarketDataRequirementNames.MID_IMPLIED_VOLATILITY;
  private static final String EXCHANGE = "NYSE";
  private static final String SCHEME = ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName();
  private static final String[] RESULTS = new String[] {
    "SPH3P 110 Index", "SPH3P 120 Index", "SPH3P 130 Index", "SPH3C 140 Index", "SPH3C 150 Index",
    "SPJ3P 110 Index", "SPJ3P 120 Index", "SPJ3P 130 Index", "SPJ3C 140 Index", "SPJ3C 150 Index",
    "SPK3P 110 Index", "SPK3P 120 Index", "SPK3P 130 Index", "SPK3C 140 Index", "SPK3C 150 Index",
    "SPM3P 110 Index", "SPM3P 120 Index", "SPM3P 130 Index", "SPM3C 140 Index", "SPM3C 150 Index",
    "SPH3P 110 Index", "SPH3P 120 Index", "SPH3P 130 Index", "SPH3C 140 Index", "SPH3C 150 Index",
    "SPM3P 110 Index", "SPM3P 120 Index", "SPM3P 130 Index", "SPM3C 140 Index", "SPM3C 150 Index",
    "SPZ3P 110 Index", "SPZ3P 120 Index", "SPZ3P 130 Index", "SPZ3C 140 Index", "SPZ3C 150 Index",
  };

  static {
    final int[] n = new int[] {1, 2, 3, 4, 1, 2, 4};
    final Tenor monthly = Tenor.ONE_MONTH;
    final Tenor quarterly = Tenor.THREE_MONTHS;
    final Tenor[] periods = new Tenor[] {monthly, monthly, monthly, monthly, quarterly, quarterly, quarterly};
    for (int i = 0; i < n.length; i++) {
      N_OPTION.add(Pairs.of(n[i], periods[i]));
    }
  }

  @Test
  public void testTickers() {
    final BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider provider =
        new BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME, SPOT, EXCHANGE, SCHEME);
    int i = 0;
    for (final Pair<Integer, Tenor> p : N_OPTION) {
      for (final double strike : STRIKES) {
        assertEquals(ExternalId.of(SCHEME, RESULTS[i++]), provider.getInstrument(p, strike, DATE));
      }
    }
  }

  @Test
  public void cycleObject() {
    final BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider provider =
        new BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME, SPOT, EXCHANGE, SCHEME);
    assertEquals(provider, cycleObject(BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
  }
}
