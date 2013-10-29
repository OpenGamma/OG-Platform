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
import com.opengamma.financial.convention.expirycalc.SoybeanFutureExpiryCalculator;
import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergCommodityFuturePriceCurveInstrumentProviderTest extends FinancialTestBase {

  private static final String DATA_FIELD_NAME = MarketDataRequirementNames.IMPLIED_VOLATILITY;
  private static final String POSTFIX = "Comdty";
  private static final String SCHEME = ExternalSchemes.BLOOMBERG_BUID_WEAK.getName();
  private static final String PREFIX = "S ";
  private static final BloombergCommodityFuturePriceCurveInstrumentProvider PROVIDER =
      new BloombergCommodityFuturePriceCurveInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME);
  private static final SoybeanFutureExpiryCalculator EXPIRY_CALC = SoybeanFutureExpiryCalculator.getInstance();

  private static final LocalDate DATE_AFTER_FIFTEENTH = LocalDate.of(2012, 11, 21);
  private static final LocalDate DATE_BEFORE_FIFTEENTH = LocalDate.of(2012, 11, 10);
  private static final LocalDate DATE_IN_DECEMBER = LocalDate.of(2012, 12, 10);
  static final Calendar WEEKEND_CALENDAR = new MondayToFridayCalendar("a");

  private static final Short[] EXPIRY_OFFSETS = new Short[] { 1, 2, 8, 10 };

  private static final String[] RESULTS = new String[] {
      "S H3 Comdty", "S K3 Comdty", "S H4 Comdty", "S N4 Comdty"
  };

  @Test
  public void testSoybeanFutureExpiryCalculator_getExpiryMonth() {
    assertEquals(DATE_AFTER_FIFTEENTH.plusMonths(4).getMonth(), EXPIRY_CALC.getExpiryMonth(1, DATE_AFTER_FIFTEENTH).getMonth());
    assertEquals(DATE_AFTER_FIFTEENTH.plusMonths(8).getMonth(), EXPIRY_CALC.getExpiryMonth(3, DATE_AFTER_FIFTEENTH).getMonth());
    assertEquals(DATE_AFTER_FIFTEENTH.plusMonths(10).getMonth(), EXPIRY_CALC.getExpiryMonth(5, DATE_AFTER_FIFTEENTH).getMonth());
    assertEquals(DATE_BEFORE_FIFTEENTH.plusMonths(2).getMonth(), EXPIRY_CALC.getExpiryMonth(1, DATE_BEFORE_FIFTEENTH).getMonth());
    assertEquals(DATE_BEFORE_FIFTEENTH.plusMonths(6).getMonth(), EXPIRY_CALC.getExpiryMonth(3, DATE_BEFORE_FIFTEENTH).getMonth());
    assertEquals(DATE_BEFORE_FIFTEENTH.plusMonths(9).getMonth(), EXPIRY_CALC.getExpiryMonth(5, DATE_BEFORE_FIFTEENTH).getMonth());
    assertEquals(DATE_BEFORE_FIFTEENTH.plusMonths(16).getMonth(), EXPIRY_CALC.getExpiryMonth(9, DATE_BEFORE_FIFTEENTH).getMonth());
    assertEquals(DATE_IN_DECEMBER.plusMonths(1).getMonth(), EXPIRY_CALC.getExpiryMonth(1, DATE_IN_DECEMBER).getMonth());
  }

  @Test
  public void testSoybeanFutureExpiryCalculator_getExpiryDate() {
    assertEquals(LocalDate.of(2013, 3, 14), EXPIRY_CALC.getExpiryDate(1, DATE_AFTER_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 5, 14), EXPIRY_CALC.getExpiryDate(2, DATE_AFTER_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 7, 12), EXPIRY_CALC.getExpiryDate(3, DATE_AFTER_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 8, 14), EXPIRY_CALC.getExpiryDate(4, DATE_AFTER_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 9, 13), EXPIRY_CALC.getExpiryDate(5, DATE_AFTER_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 11, 14), EXPIRY_CALC.getExpiryDate(6, DATE_AFTER_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 1, 14), EXPIRY_CALC.getExpiryDate(1, DATE_BEFORE_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 5, 14), EXPIRY_CALC.getExpiryDate(3, DATE_BEFORE_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 9, 13), EXPIRY_CALC.getExpiryDate(6, DATE_BEFORE_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2013, 11, 14), EXPIRY_CALC.getExpiryDate(7, DATE_BEFORE_FIFTEENTH, WEEKEND_CALENDAR));
    assertEquals(LocalDate.of(2014, 1, 14), EXPIRY_CALC.getExpiryDate(8, DATE_BEFORE_FIFTEENTH, WEEKEND_CALENDAR));
  }

  @Test
  public void testFuturePriceCurveInstrumentProvider() {
    for (int i = 0; i < EXPIRY_OFFSETS.length; i++) {
      final String expected = RESULTS[i];
      final ExternalId actual = PROVIDER.getInstrument(EXPIRY_OFFSETS[i], DATE_AFTER_FIFTEENTH);
      assertEquals(ExternalSchemes.BLOOMBERG_TICKER_WEAK, actual.getScheme());
      assertEquals(expected, actual.getValue());
    }
  }

  @Test
  public void testCycle() {
    BloombergCommodityFuturePriceCurveInstrumentProvider provider = new BloombergCommodityFuturePriceCurveInstrumentProvider(PREFIX, POSTFIX,
        DATA_FIELD_NAME);
    assertEquals(provider, cycleObject(BloombergCommodityFuturePriceCurveInstrumentProvider.class, provider));
    provider = new BloombergCommodityFuturePriceCurveInstrumentProvider(PREFIX, POSTFIX,
        DATA_FIELD_NAME, SCHEME);
    assertEquals(provider, cycleObject(BloombergCommodityFuturePriceCurveInstrumentProvider.class, provider));
  }

}
