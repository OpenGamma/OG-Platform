/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.expirycalc.SoybeanFutureOptionExpiryCalculator;
import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProviderTest extends FinancialTestBase {

  private static final Double CALL_ABOVE_STRIKE = 150.0;
  private static final String DATA_FIELD_NAME = MarketDataRequirementNames.IMPLIED_VOLATILITY;
  private static final String FUTURE_OPTION_PREFIX = "S ";
  private static final String POSTFIX = "Comdty";
  private static final String EXCHANGE = "CBT";
  private static final String SCHEME = ExternalSchemes.BLOOMBERG_BUID_WEAK.getName();
  private static final SoybeanFutureOptionExpiryCalculator EXPIRY_CALC = SoybeanFutureOptionExpiryCalculator.getInstance();
  private static final String PREFIX = "S ";
  private static final double CENTRE_STRIKE = 1400.0;
  private static final String EXCHANGE_ID = "CBT";
  private static final BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider PROVIDER =
    new BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME, CENTRE_STRIKE, EXCHANGE_ID);

  private static final LocalDate DATE = LocalDate.of(2012, 11, 21);
  static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("a");

  private static final Short[] EXPIRY_OFFSETS = new Short[] {1, 2, 8, 10};
  private static final Double[] STRIKES = new Double[] {1350.0, 1400.0, 1450.0};

  private static final String[][] RESULTS = new String[][] {
    new String[] {"S Z2P 1350.0 Comdty", "S Z2P 1400.0 Comdty", "S Z2C 1450.0 Comdty"},
    new String[] {"S F3P 1350.0 Comdty", "S F3P 1400.0 Comdty", "S F3C 1450.0 Comdty"},
    new String[] {"S U3P 1350.0 Comdty", "S U3P 1400.0 Comdty", "S U3C 1450.0 Comdty"},
    new String[] {"S F4P 1350.0 Comdty", "S F4P 1400.0 Comdty", "S F4C 1450.0 Comdty"}};

  @Test
  public void testSoybeanFutureOptionExpiryCalculator_getExpiryMonth() {
    assertEquals(DATE.plusMonths(1), EXPIRY_CALC.getExpiryMonth(1, DATE));
    assertEquals(DATE.plusMonths(2), EXPIRY_CALC.getExpiryMonth(2, DATE));
    assertEquals(DATE.plusMonths(6), EXPIRY_CALC.getExpiryMonth(5, DATE));
    assertEquals(DATE.plusMonths(10), EXPIRY_CALC.getExpiryMonth(8, DATE));
    assertEquals(DATE.plusMonths(12), EXPIRY_CALC.getExpiryMonth(9, DATE));
    assertEquals(DATE.plusMonths(14), EXPIRY_CALC.getExpiryMonth(10, DATE));
    assertEquals(DATE.plusMonths(16), EXPIRY_CALC.getExpiryMonth(11, DATE));
  }

  @Test
  public void testSoybeanFutureOptionExpiryCalculator_getExpiryDate() {
    assertEquals(LocalDate.of(2012,11,23), EXPIRY_CALC.getExpiryDate(1, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2012,12,21), EXPIRY_CALC.getExpiryDate(2, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013,1,25), EXPIRY_CALC.getExpiryDate(3, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013,2,22), EXPIRY_CALC.getExpiryDate(4, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013,4,26), EXPIRY_CALC.getExpiryDate(5, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013,6,21), EXPIRY_CALC.getExpiryDate(6, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013,7,26), EXPIRY_CALC.getExpiryDate(7, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013,8,23), EXPIRY_CALC.getExpiryDate(8, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013,10,25), EXPIRY_CALC.getExpiryDate(9, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013,12,27), EXPIRY_CALC.getExpiryDate(10, DATE, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2014,2,21), EXPIRY_CALC.getExpiryDate(11, DATE, WEEKEND_CALENDAR));
  }

  @Test
  public void testSurfaceInstrumentProvider() {
    for (int i = 0; i < EXPIRY_OFFSETS.length; i++) {
      for (int j = 0; j < STRIKES.length; j++) {
        final String expected = RESULTS[i][j];
        final ExternalId actual = PROVIDER.getInstrument(EXPIRY_OFFSETS[i], STRIKES[j], DATE);
        assertEquals(ExternalSchemes.BLOOMBERG_TICKER_WEAK, actual.getScheme());
        assertEquals(expected, actual.getValue());
      }
    }
  }

  @Test
  public void testCycle() {
    BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider provider = new BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME, CALL_ABOVE_STRIKE, EXCHANGE);
    assertEquals(provider, cycleObject(BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
    provider = new BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME, CALL_ABOVE_STRIKE, EXCHANGE, SCHEME);
    assertEquals(provider, cycleObject(BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
  }

}
