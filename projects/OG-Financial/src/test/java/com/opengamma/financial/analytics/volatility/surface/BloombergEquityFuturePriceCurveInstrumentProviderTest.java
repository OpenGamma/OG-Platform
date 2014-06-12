/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergEquityFuturePriceCurveInstrumentProviderTest extends FinancialTestBase {

  private static final String DATA_FIELD_NAME = MarketDataRequirementNames.IMPLIED_VOLATILITY;
  private static final String POSTFIX = "Equity";
  private static final String SCHEME = ExternalSchemes.BLOOMBERG_BUID_WEAK.getName();
  private static final String PREFIX = "AAPL=";
  private static final String EXCHANGE = "OC";
  private static final BloombergEquityFuturePriceCurveInstrumentProvider PROVIDER =
      new BloombergEquityFuturePriceCurveInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME, EXCHANGE);

  private static final LocalDate DATE = LocalDate.of(2013, 2, 1);
  static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("Weekday");

  private static final Short[] EXPIRY_OFFSETS = new Short[] { 1, 2, 8, 13 };
  private static final ExchangeTradedInstrumentExpiryCalculator EXPIRY_CALC = FutureOptionExpiries.EQUITY_FUTURE;

  private static final String[] RESULTS = new String[] {
      "AAPL=G3 OC Equity", "AAPL=H3 OC Equity", "AAPL=U3 OC Equity", "AAPL=G4 OC Equity"
  };

  @Test
  public void testExpiryMonth() {
    assertEquals(Month.FEBRUARY, EXPIRY_CALC.getExpiryMonth(1, DATE).getMonth());    
    assertEquals(Month.MARCH, EXPIRY_CALC.getExpiryMonth(2,DATE).getMonth());
    assertEquals(Month.APRIL, EXPIRY_CALC.getExpiryMonth(3,DATE).getMonth());
    assertEquals(Month.MAY, EXPIRY_CALC.getExpiryMonth(4,DATE).getMonth());
    assertEquals(Month.JUNE, EXPIRY_CALC.getExpiryMonth(5,DATE).getMonth());
    assertEquals(Month.JULY, EXPIRY_CALC.getExpiryMonth(6,DATE).getMonth());
    assertEquals(Month.SEPTEMBER, EXPIRY_CALC.getExpiryMonth(8,DATE).getMonth());
    assertEquals(Month.DECEMBER, EXPIRY_CALC.getExpiryMonth(11,DATE).getMonth());
  }

  @Test
  public void testExpiryDate() {
    assertEquals(LocalDate.of(2013, 2, 15), EXPIRY_CALC.getExpiryDate(1, DATE, WEEKEND_CALENDAR));    
    assertEquals(LocalDate.of(2013, 3, 15), EXPIRY_CALC.getExpiryDate(2, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 9, 20), EXPIRY_CALC.getExpiryDate(8, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2014, 2, 21), EXPIRY_CALC.getExpiryDate(13, DATE, WEEKEND_CALENDAR));
  }

  @Test
  public void testFuturePriceCurveInstrumentProvider() {
    for (int i = 0; i < EXPIRY_OFFSETS.length; i++) {
      final String expected = RESULTS[i];
      final ExternalId actual = PROVIDER.getInstrument(EXPIRY_OFFSETS[i], DATE);
      assertEquals(ExternalSchemes.BLOOMBERG_TICKER_WEAK, actual.getScheme());
      assertEquals(expected, actual.getValue());
    }
  }

  @Test
  public void testCycle() {
    BloombergEquityFuturePriceCurveInstrumentProvider provider = new BloombergEquityFuturePriceCurveInstrumentProvider(PREFIX, POSTFIX,
        DATA_FIELD_NAME, EXCHANGE);
    assertEquals(provider, cycleObject(BloombergEquityFuturePriceCurveInstrumentProvider.class, provider));
    provider = new BloombergEquityFuturePriceCurveInstrumentProvider(PREFIX, POSTFIX,
        DATA_FIELD_NAME, SCHEME, EXCHANGE);
    assertEquals(provider, cycleObject(BloombergEquityFuturePriceCurveInstrumentProvider.class, provider));
  }

}
