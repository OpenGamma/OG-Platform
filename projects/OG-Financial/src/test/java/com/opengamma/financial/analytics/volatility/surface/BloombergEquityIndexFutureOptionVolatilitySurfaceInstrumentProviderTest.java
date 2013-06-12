/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergEquityIndexFutureOptionVolatilitySurfaceInstrumentProviderTest {

  private static final String PREFIX = "DJX";
  private static final String POSTFIX = "Index";
  private static final LocalDate DATE = LocalDate.of(2012, 5, 23);
  private static final Short[] EXPIRY_OFFSETS = new Short[] {1, 2, 8};
  private static final Double[] STRIKES = new Double[] {90.0, 145.0, 205.0};

  private static final String DATA_FIELD_NAME = "OPT_IMPLIED_VOLATILITY_MID";

  private static final String[][] RESULTS = new String[][]
    {new String[] {"DJX 06/16/12 P90.0 Index", "DJX 06/16/12 P145.0 Index", "DJX 06/16/12 C205.0 Index"},
    new String[] {"DJX 07/21/12 P90.0 Index", "DJX 07/21/12 P145.0 Index", "DJX 07/21/12 C205.0 Index"},
      new String[] { "DJX 01/19/13 P90.0 Index", "DJX 01/19/13 P145.0 Index", "DJX 01/19/13 C205.0 Index" } }; // TODO Fix date on this last one

  private static final FutureOptionExpiries UTILS = FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1));
  private static LocalDate[] EXPIRY_DATES = new LocalDate[3];
  private static final String EXCHANGE = "OSE";
  static {
    for (int i = 0; i < EXPIRY_OFFSETS.length; i++) {
      EXPIRY_DATES[i] = FutureOptionExpiries.EQUITY.getFutureOptionExpiry(EXPIRY_OFFSETS[i], DATE);
    }
  }

  private static final BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider PROVIDER =
      new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME, 150.25, EXCHANGE);


  @Test
  public void test() {
    for (int i = 0; i < EXPIRY_OFFSETS.length; i++) {
      for (int j = 0; j < STRIKES.length; j++) {
        final String expected = RESULTS[i][j];
        final ExternalId actual = PROVIDER.getInstrument(EXPIRY_OFFSETS[i], STRIKES[j], DATE);
        assertEquals(ExternalSchemes.BLOOMBERG_TICKER_WEAK, actual.getScheme());
        if (!(expected.equals(actual.getValue()))) {
          assertEquals(expected, actual.getValue());  
        }
        
      }
    }
  }

  @Test
  public void testUtils() {
    assertEquals(EXPIRY_DATES[0], FutureOptionExpiries.EQUITY.getMonthlyExpiry(1, DATE));
    assertEquals(EXPIRY_DATES[1], FutureOptionExpiries.EQUITY.getMonthlyExpiry(2, DATE));
    assertEquals(EXPIRY_DATES[2], FutureOptionExpiries.EQUITY.getQuarterlyExpiry(EXPIRY_OFFSETS[2]-6, UTILS.getMonthlyExpiry(6, DATE)));
  }


}
