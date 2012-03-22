/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
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
  private static final NextExpiryAdjuster NEXT_EXPIRY_ADJUSTER = new NextExpiryAdjuster();
  private static final String[][] RESULTS = new String[][] {new String[] {"EDU1C 96.000 Comdty", "EDU1C 97.250 Comdty", "EDU1P 98.500 Comdty", "EDU1P 99.750 Comdty"},
    new String[] {"EDH3C 96.000 Comdty", "EDH3C 97.250 Comdty", "EDH3P 98.500 Comdty", "EDH3P 99.750 Comdty"},
    new String[] {"EDZ3C 96.000 Comdty", "EDZ3C 97.250 Comdty", "EDZ3P 98.500 Comdty", "EDZ3P 99.750 Comdty"}};

  private static final String[][] EXPIRED_RESULTS = new String[][] {new String[] {"EDU11C 96.000 Comdty", "EDU11C 97.250 Comdty", "EDU11P 98.500 Comdty", "EDU11P 99.750 Comdty"},
    new String[] {"EDH13C 96.000 Comdty", "EDH13C 97.250 Comdty", "EDH13P 98.500 Comdty", "EDH13P 99.750 Comdty"},
    new String[] {"EDZ13C 96.000 Comdty", "EDZ13C 97.250 Comdty", "EDZ13P 98.500 Comdty", "EDZ13P 99.750 Comdty"}};

  private static final LocalDate[] EXPIRY_DATES = new LocalDate[] {NEXT_EXPIRY_ADJUSTER.adjustDate(LocalDate.of(2011, MonthOfYear.SEPTEMBER, 1)),
    NEXT_EXPIRY_ADJUSTER.adjustDate(LocalDate.of(2013, MonthOfYear.SEPTEMBER, 1)),
    NEXT_EXPIRY_ADJUSTER.adjustDate(LocalDate.of(2013, MonthOfYear.DECEMBER, 1)) };

  private static final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider PROVIDER = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME, 97.625);

  @Test
  public void test() {
    final LocalDate today = LocalDate.now();
    for (int i = 0; i < NUMBERS.length; i++) {
      for (int j = 0; j < STRIKES.length; j++) {
        String expected;
        if (today.isAfter(EXPIRY_DATES[i])) {
          expected = EXPIRED_RESULTS[i][j];
        } else {
          expected = RESULTS[i][j];
        }
        final ExternalId result = PROVIDER.getInstrument(NUMBERS[i], STRIKES[j], DATE);
        assertEquals(SecurityUtils.BLOOMBERG_TICKER_WEAK, result.getScheme());
        assertEquals(expected, result.getValue());
      }
    }
  }
}
