/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.DateAdjusters;
import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.id.ExternalId;

/**
 * This test will begin late in 2012 as historical data on options on the Sep2011 Eurodollar future will cease to be provided  
 */
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProviderTest {
  private static final String PREFIX = "ED";
  private static final String POSTFIX = "Comdty";
  private static final LocalDate DATE = LocalDate.of(2011, 7, 1);
  private static final Short[] NUMBERS = new Short[] {1, 7, 10};
  private static final Double[] STRIKES = new Double[] {96., 97.25, 98.5, 99.75};
  private static final String DATA_FIELD_NAME = "OPT_IMPLIED_VOLATILITY_MID";
  private static final String[][] RESULTS = new String[][] {new String[] {"EDN1P 96.000 Comdty", "EDN1P 97.250 Comdty", "EDN1C 98.500 Comdty", "EDN1C 99.750 Comdty"},
    new String[] {"EDH2P 96.000 Comdty", "EDH2P 97.250 Comdty", "EDH2C 98.500 Comdty", "EDH2C 99.750 Comdty"},
    new String[] {"EDZ2P 96.000 Comdty", "EDZ2P 97.250 Comdty", "EDZ2C 98.500 Comdty", "EDZ2C 99.750 Comdty"}};

  private static final FutureOptionExpiries UTILS =  FutureOptionExpiries.of(DateAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY), new NextExpiryAdjuster());
  private static final LocalDate[] EXPIRY_DATES = new LocalDate[] {UTILS.getFutureOptionExpiry(1,LocalDate.of(2011, MonthOfYear.SEPTEMBER, 1)),
    UTILS.getFutureOptionExpiry(1,LocalDate.of(2013, MonthOfYear.MARCH, 1)),
    UTILS.getFutureOptionExpiry(1,LocalDate.of(2013, MonthOfYear.DECEMBER, 1)) }; 

  private static final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider PROVIDER = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME, 97.625);

  @Test
  public void test() {
    for (int i = 0; i < NUMBERS.length; i++) {
      assertEquals(EXPIRY_DATES[i], UTILS.getQuarterlyExpiry(NUMBERS[i], DATE));
      for (int j = 0; j < STRIKES.length; j++) {
        final String expected = RESULTS[i][j];
        final ExternalId actual = PROVIDER.getInstrument(NUMBERS[i], STRIKES[j], DATE);
        assertEquals(ExternalSchemes.BLOOMBERG_TICKER_WEAK, actual.getScheme());
        assertEquals(expected, actual.getValue());
      }
    }
  }
}
