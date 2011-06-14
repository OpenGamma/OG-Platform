/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests related to the construction of vanilla Forex options (derivative version).
 */
public class ForexOptionVanillaTest {
  // FX Option: EUR call/USD put; 1m EUR @ 1.4177
  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtil.getUTCDate(2012, 6, 8);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2012, 6, 12);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final boolean IS_CALL = true;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  // Derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2011, 6, 8);
  private static final String DISCOUNTING_CURVE_NAME_CUR_1 = "Discounting EUR";
  private static final String DISCOUNTING_CURVE_NAME_CUR_2 = "Discounting USD";
  private static final String[] CURVES_NAME = new String[] {DISCOUNTING_CURVE_NAME_CUR_1, DISCOUNTING_CURVE_NAME_CUR_2};
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final ForexOptionVanilla FX_OPTION = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWrongExpiration() {
    new ForexOptionVanilla(FX, EXPIRATION_TIME + 0.5, IS_CALL);
  }

  @Test
  public void getter() {
    assertEquals(FX, FX_OPTION.getUnderlyingForex());
    assertEquals(EXPIRATION_TIME, FX_OPTION.getTimeToExpiry());
    assertEquals(IS_CALL, FX_OPTION.isCall());
  }

  @Test
  /**
   * Tests the call/put description.
   */
  public void callPut() {
    ForexOptionVanilla optPositiveCall = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL);
    assertTrue("Forex vanilla option call/put: Positive amount / call", optPositiveCall.isCall());
    ForexOptionVanilla optPositivePut = new ForexOptionVanilla(FX, EXPIRATION_TIME, !IS_CALL);
    assertTrue("Forex vanilla option call/put: Positive amount / put", !optPositivePut.isCall());
    ForexDefinition fxNegativeDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, -NOMINAL_1, FX_RATE);
    Forex fxNegative = fxNegativeDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    ForexOptionVanilla optNegativePut = new ForexOptionVanilla(fxNegative, EXPIRATION_TIME, !IS_CALL);
    assertTrue("Forex vanilla option call/put: Negative amount / put", optNegativePut.isCall());
    ForexOptionVanilla optNegativeCall = new ForexOptionVanilla(fxNegative, EXPIRATION_TIME, IS_CALL);
    assertTrue("Forex vanilla option call/put: Negative amount / call", !optNegativeCall.isCall());
  }

  @Test
  public void equalHash() {
    ForexOptionVanilla otherOption = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL);
    assertTrue(otherOption.equals(FX_OPTION));
    assertEquals(FX_OPTION.hashCode(), otherOption.hashCode());
    ForexOptionVanilla modifiedOption;
    modifiedOption = new ForexOptionVanilla(FX, EXPIRATION_TIME, !IS_CALL);
    assertFalse(modifiedOption.equals(FX_OPTION));
    modifiedOption = new ForexOptionVanilla(FX, EXPIRATION_TIME - 0.01, IS_CALL);
    assertFalse(modifiedOption.equals(FX_OPTION));
    ForexDefinition modifiedFxDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1 + 1.0, FX_RATE);
    Forex modifiedFx = modifiedFxDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    modifiedOption = new ForexOptionVanilla(modifiedFx, EXPIRATION_TIME, IS_CALL);
    assertFalse(modifiedOption.equals(FX_OPTION));
  }

}
