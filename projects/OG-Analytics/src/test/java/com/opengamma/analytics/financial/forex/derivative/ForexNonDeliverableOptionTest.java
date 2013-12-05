package com.opengamma.analytics.financial.forex.derivative;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ForexNonDeliverableOptionTest {

  private static final Currency KRW = Currency.of("KRW");
  private static final Currency USD = Currency.EUR;
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2012, 5, 2);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 5, 4);
  private static final double NOMINAL_USD = 1000000; // 1m
  private static final double STRIKE_USD_KRW = 1123.45;

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 11, 10);
  private static final double FIXING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATE);
  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);

  private static final ForexNonDeliverableForward NDF = new ForexNonDeliverableForward(KRW, USD, NOMINAL_USD, STRIKE_USD_KRW, FIXING_TIME, PAYMENT_TIME);

  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final ForexNonDeliverableOption NDO = new ForexNonDeliverableOption(NDF, IS_CALL, IS_LONG);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFX() {
    new ForexNonDeliverableOption(null, IS_CALL, IS_LONG);
  }

  @Test
  public void getter() {
    assertEquals("Forex non-deliverable option - getter", NDF, NDO.getUnderlyingNDF());
    assertEquals("Forex non-deliverable option - getter", IS_CALL, NDO.isCall());
    assertEquals("Forex non-deliverable option - getter", IS_LONG, NDO.isLong());
  }
}
