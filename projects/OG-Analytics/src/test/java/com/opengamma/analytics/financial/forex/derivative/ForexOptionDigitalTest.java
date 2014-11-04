/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of Digital Forex options (derivative version).
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionDigitalTest {
  // FX Option: EUR call/USD put; 1m EUR @ 1.4177
  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2012, 6, 8);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2012, 6, 12);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  // Derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 8);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final ForexOptionDigital FX_OPTION = new ForexOptionDigital(FX, EXPIRATION_TIME, IS_CALL, IS_LONG, true);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new ForexOptionDigital(null, EXPIRATION_TIME, IS_CALL, IS_LONG, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongExpiration() {
    new ForexOptionDigital(FX, EXPIRATION_TIME + 0.5, IS_CALL, IS_LONG, true);
  }

  @Test
  public void getter() {
    assertEquals(FX, FX_OPTION.getUnderlyingForex());
    assertEquals(EXPIRATION_TIME, FX_OPTION.getExpirationTime());
    assertEquals(IS_CALL, FX_OPTION.isCall());
    assertEquals(IS_LONG, FX_OPTION.isLong());
  }

  @Test
  public void equalHash() {
    assertTrue(FX_OPTION.equals(FX_OPTION));
    final ForexOptionDigital otherOption = new ForexOptionDigital(FX, EXPIRATION_TIME, IS_CALL, IS_LONG, true);
    assertTrue(otherOption.equals(FX_OPTION));
    assertEquals(FX_OPTION.hashCode(), otherOption.hashCode());
    final ForexOptionDigital otherOptionShort1 = new ForexOptionDigital(FX, EXPIRATION_TIME, IS_CALL, !IS_LONG, true);
    final ForexOptionDigital otherOptionShort2 = new ForexOptionDigital(FX, EXPIRATION_TIME, IS_CALL, !IS_LONG, true);
    assertTrue(otherOptionShort1.equals(otherOptionShort2));
    assertEquals(otherOptionShort1.hashCode(), otherOptionShort2.hashCode());
    ForexOptionDigital modifiedOption;
    modifiedOption = new ForexOptionDigital(FX, EXPIRATION_TIME, !IS_CALL, IS_LONG, true);
    assertFalse(modifiedOption.equals(FX_OPTION));
    modifiedOption = new ForexOptionDigital(FX, EXPIRATION_TIME - 0.01, IS_CALL, IS_LONG, true);
    assertFalse(modifiedOption.equals(FX_OPTION));
    modifiedOption = new ForexOptionDigital(FX, EXPIRATION_TIME, IS_CALL, !IS_LONG, true);
    assertFalse(modifiedOption.equals(FX_OPTION));
    final ForexDefinition modifiedFxDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1 + 1.0, FX_RATE);
    final Forex modifiedFx = modifiedFxDefinition.toDerivative(REFERENCE_DATE);
    modifiedOption = new ForexOptionDigital(modifiedFx, EXPIRATION_TIME, IS_CALL, IS_LONG, true);
    assertFalse(modifiedOption.equals(FX_OPTION));
    assertFalse(modifiedOption.equals(null));
  }

}
