/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.ExternalId;

/**
 * 
 */
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProviderTest {
  private static final String PREFIX = "ED";
  private static final String POSTFIX = "Comdty";
  private static final LocalDate DATE = LocalDate.of(2011, 7, 1);
  private static final Integer[] NUMBERS = new Integer[] {1, 7, 10};
  private static final Double[] STRIKES = new Double[] {96., 97.25, 98.5, 99.75};
  private static final String DATA_FIELD_NAME = "OPT_IMPLIED_VOLATILITY_MID";
  private static final String[][] RESULTS = new String[][] {new String[] {"EDU1P 96.000 Comdty", "EDU1P 97.250 Comdty", "EDU1C 98.500 Comdty", "EDU1C 99.750 Comdty"},
                                                            new String[] {"EDH3P 96.000 Comdty", "EDH3P 97.250 Comdty", "EDH3C 98.500 Comdty", "EDH3C 99.750 Comdty"},
                                                            new String[] {"EDZ3P 96.000 Comdty", "EDZ3P 97.250 Comdty", "EDZ3C 98.500 Comdty", "EDZ3C 99.750 Comdty"}};
  private static final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider PROVIDER = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME, 97.625);

  @Test
  public void test() {
    for (int i = 0; i < NUMBERS.length; i++) {
      for (int j = 0; j < STRIKES.length; j++) {
        final ExternalId result = PROVIDER.getInstrument(NUMBERS[i], STRIKES[j], DATE);
        assertEquals(SecurityUtils.BLOOMBERG_TICKER_WEAK, result.getScheme());
        assertEquals(RESULTS[i][j], result.getValue());
      }
    }
  }
}
