/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.id.ExternalId;
import com.opengamma.util.OpenGammaClock;

/**
 * 
 */
public class BloombergIRFuturePriceCurveInstrumentProviderTest {
  private static final String PREFIX_EUR = "ER";
  private static final String PREFIX_USD = "ED";
  private static final String POSTFIX = "Comdty";
  private static final LocalDate SNAPSHOT_DATE = LocalDate.of(2010, 6, 14); // Last Trading Date of ERM10 Comdty
  private static final LocalDate CURRENT_DATE = LocalDate.of(2012, 4, 16);
  private static final String FIELD_NAME = MarketDataRequirementNames.MARKET_VALUE;
  
  private static final BloombergIRFuturePriceCurveInstrumentProvider PROVIDER_EUR = new BloombergIRFuturePriceCurveInstrumentProvider(PREFIX_EUR, POSTFIX, FIELD_NAME);
  private static final BloombergIRFuturePriceCurveInstrumentProvider PROVIDER_USD = new BloombergIRFuturePriceCurveInstrumentProvider(PREFIX_USD, POSTFIX, FIELD_NAME);

  private static final int nFutures = 9;
  private static final String[] RESULTS_EUR = new String[] {"ERM10 Comdty","ERU10 Comdty","ERZ10 Comdty","ERH11 Comdty", "ERM1 Comdty", "ERU1 Comdty","ERZ1 Comdty","ERH2 Comdty", "ERM2 Comdty"};
  
  /**
   * This will begin to fail in mid June. Good. At that point, clean this up to nail down the logic of the switch over.
   */
  @Test
  public void testEur() {
    String expected;
    for (int ithFuture = 1; ithFuture <= nFutures; ithFuture++) {
      expected = RESULTS_EUR[ithFuture-1];       
      final ExternalId result = PROVIDER_EUR.getInstrument(ithFuture, SNAPSHOT_DATE);
      assertEquals(ExternalSchemes.BLOOMBERG_TICKER_WEAK, result.getScheme());
      assertEquals(expected, result.getValue());
    }
  }
  
  @Test
  public void testERM() {
    
    final LocalDate today = LocalDate.now(OpenGammaClock.getInstance());
    final LocalDate lastTradeDateOfERM1 = LocalDate.of(2011,6,13);
    final LocalDate lastTradeDateOfERM2 = LocalDate.of(2012,6,18);
    
    final ExternalId actual = PROVIDER_EUR.getInstrument(5, SNAPSHOT_DATE);
    final String expected;
    if(today.isBefore(lastTradeDateOfERM1.plusYears(1))) {
      expected = "ERM1 Comdty"; 
    } else {
      expected = "ERM11 Comdty";
    }
    assertEquals(expected, actual.getValue());
    
  }
  
  
  
}