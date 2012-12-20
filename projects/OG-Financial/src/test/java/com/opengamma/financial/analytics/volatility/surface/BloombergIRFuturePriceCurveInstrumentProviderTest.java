/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.ExternalId;
import com.opengamma.util.OpenGammaClock;

/**
 * This test was set up to check Bloomberg's logic of ticker construction for IR Futures.
 * It will begin to fail by design, to clean this up to nail down the logic of the switch over.
 */
public class BloombergIRFuturePriceCurveInstrumentProviderTest {
  private static final String PREFIX_EUR = "ER";
  private static final String POSTFIX = "Comdty";
  private static final LocalDate SNAPSHOT_DATE = LocalDate.of(2010, 6, 14); // Last Trading Date of ERM10 Comdty
  private static final String FIELD_NAME = MarketDataRequirementNames.MARKET_VALUE;

  private static final BloombergIRFuturePriceCurveInstrumentProvider PROVIDER_EUR = new BloombergIRFuturePriceCurveInstrumentProvider(PREFIX_EUR, POSTFIX, FIELD_NAME);

  private static final int nFutures = 9;
  private static final String[] RESULTS_EUR = new String[] {"ERM10 Comdty", "ERU10 Comdty", "ERZ10 Comdty", "ERH11 Comdty", "ERM11 Comdty", "ERU11 Comdty", "ERZ1 Comdty", "ERH2 Comdty", "ERM2 Comdty" };

  /**
   *  The first test will begin to fail in August, on the 17th or 20th. Good, but not quite good enough.
   *  At that point, it might be too late to confirm exactly when the Sep 2011 Euribor 3M future ticker goes from ERU1 to ERU11.
   *  So, we've added a test that begins failing a week earlier.   [edit : I've pushed the date back to
   */
  @Test(enabled = false)
  // Disabled so that the v1.2.x branch builds cleanly
  public void testEur() {
    String expected;
    for (int ithFuture = 1; ithFuture <= nFutures; ithFuture++) {
      expected = RESULTS_EUR[ithFuture - 1];
      final ExternalId result = PROVIDER_EUR.getInstrument(ithFuture, SNAPSHOT_DATE);
      assertEquals(ExternalSchemes.BLOOMBERG_TICKER_WEAK, result.getScheme());
      assertEquals(expected, result.getValue());
    }
  }

  /**
   * Want to nail down when ERZ1 Comdty is no longer available, and then infer what fields of the Security this is based on.
   */
  @Test(enabled = false)
  // Disabled so that the v1.2.x branch builds cleanly
  public void whenThisBeginsToFailCheckIfERZ1IsValid() {
    final LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    assertTrue(today.isBefore(LocalDate.of(2012, 11, 16)));
  }

  @Test
  public void testERM() {

    final LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    final LocalDate lastTradeDateOfERM1 = LocalDate.of(2011, 6, 13);

    final ExternalId actual = PROVIDER_EUR.getInstrument(5, SNAPSHOT_DATE);
    final String expected;
    if (today.isBefore(lastTradeDateOfERM1.plusMonths(11))) {
      expected = "ERM1 Comdty";
    } else {
      expected = "ERM11 Comdty";
    }
    assertEquals(expected, actual.getValue());

  }

}
