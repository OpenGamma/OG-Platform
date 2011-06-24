/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.Identifier;

/**
 * 
 */
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProviderTest {
  private static final String PREFIX = "ED";
  private static final String POSTFIX = "Comdty";
  private static final LocalDate DATE = LocalDate.of(1, 7, 2011);
  private static final Integer[] NUMBERS = new Integer[] {1, 7, 10};
  private static final Double[] STRIKES = new Double[] {96., 97.25, 98.5, 99.75};
  private static final String[][] RESULTS = new String[][] {new String[] {"EDU1 96C Comdty", "EDU1 97.25C Comdty", "EDU1 98.5C Comdty", "EDU1 99.75C Comdty"},
                                                            new String[] {"EDH3 96C Comdty", "EDH3 97.25C Comdty", "EDH3 98.5C Comdty", "EDH3 99.75C Comdty"},
                                                            new String[] {"EDZ3 96C Comdty", "EDZ3 97.25C Comdty", "EDZ3 98.5C Comdty", "EDZ3 99.75C Comdty"}};
  private static final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider PROVIDER = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX);

  //@Test
  public void test() {
    for (int i = 0; i < NUMBERS.length; i++) {
      for (int j = 0; j < STRIKES.length; j++) {
        final Identifier result = PROVIDER.getInstrument(NUMBERS[i], STRIKES[j], DATE);
        assertEquals(result.getScheme(), SecurityUtils.BLOOMBERG_TICKER);
        assertEquals(RESULTS[i][j], result.getValue());
      }
    }
  }
}
