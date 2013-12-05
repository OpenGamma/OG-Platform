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
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of vanilla Forex options (derivative version).
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionVanillaTest {
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
  private static final String DISCOUNTING_CURVE_NAME_CUR_1 = "Discounting EUR";
  private static final String DISCOUNTING_CURVE_NAME_CUR_2 = "Discounting USD";
  private static final String[] CURVES_NAME = new String[] {DISCOUNTING_CURVE_NAME_CUR_1, DISCOUNTING_CURVE_NAME_CUR_2};
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final ForexOptionVanilla FX_OPTION = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL, IS_LONG);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongExpiration() {
    new ForexOptionVanilla(FX, EXPIRATION_TIME + 0.5, IS_CALL, IS_LONG);
  }

  @Test
  public void getter() {
    assertEquals(FX, FX_OPTION.getUnderlyingForex());
    assertEquals(EXPIRATION_TIME, FX_OPTION.getTimeToExpiry());
    assertEquals(IS_CALL, FX_OPTION.isCall());
    assertEquals(IS_LONG, FX_OPTION.isLong());
  }

  @SuppressWarnings("deprecation")
  @Test
  /**
   * Tests the call/put description.
   */
  public void callPutDeprecated() {
    final Forex fx = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionVanilla optPositiveCall = new ForexOptionVanilla(fx, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertTrue("Forex vanilla option call/put: Positive amount / call", optPositiveCall.isCall());
    final ForexOptionVanilla optPositivePut = new ForexOptionVanilla(fx, EXPIRATION_TIME, !IS_CALL, IS_LONG);
    assertTrue("Forex vanilla option call/put: Positive amount / put", !optPositivePut.isCall());
    final ForexDefinition fxNegativeDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, -NOMINAL_1, FX_RATE);
    final Forex fxNegative = fxNegativeDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionVanilla optNegativePut = new ForexOptionVanilla(fxNegative, EXPIRATION_TIME, !IS_CALL, IS_LONG);
    assertTrue("Forex vanilla option call/put: Negative amount / put", optNegativePut.isCall());
    final ForexOptionVanilla optNegativeCall = new ForexOptionVanilla(fxNegative, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertTrue("Forex vanilla option call/put: Negative amount / call", !optNegativeCall.isCall());
  }

  @SuppressWarnings("deprecation")
  @Test
  public void equalHashDeprecated() {
    final Forex fx = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final ForexOptionVanilla fxOption = new ForexOptionVanilla(fx, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertTrue(fxOption.equals(fxOption));
    final ForexOptionVanilla otherOption = new ForexOptionVanilla(fx, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertTrue(otherOption.equals(fxOption));
    assertEquals(fxOption.hashCode(), otherOption.hashCode());
    final ForexOptionVanilla otherOptionShort1 = new ForexOptionVanilla(fx, EXPIRATION_TIME, IS_CALL, !IS_LONG);
    final ForexOptionVanilla otherOptionShort2 = new ForexOptionVanilla(fx, EXPIRATION_TIME, IS_CALL, !IS_LONG);
    assertTrue(otherOptionShort1.equals(otherOptionShort2));
    assertEquals(otherOptionShort1.hashCode(), otherOptionShort2.hashCode());
    ForexOptionVanilla modifiedOption;
    modifiedOption = new ForexOptionVanilla(fx, EXPIRATION_TIME, !IS_CALL, IS_LONG);
    assertFalse(modifiedOption.equals(fxOption));
    modifiedOption = new ForexOptionVanilla(fx, EXPIRATION_TIME - 0.01, IS_CALL, IS_LONG);
    assertFalse(modifiedOption.equals(fxOption));
    modifiedOption = new ForexOptionVanilla(fx, EXPIRATION_TIME, IS_CALL, !IS_LONG);
    assertFalse(modifiedOption.equals(fxOption));
    final ForexDefinition modifiedFxDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1 + 1.0, FX_RATE);
    final Forex modifiedFx = modifiedFxDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    modifiedOption = new ForexOptionVanilla(modifiedFx, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertFalse(modifiedOption.equals(fxOption));
    final EuropeanVanillaOption option = new EuropeanVanillaOption(fxOption.getStrike(), fxOption.getTimeToExpiry(), fxOption.isCall());
    assertFalse(fxOption.equals(option));
    assertFalse(modifiedOption.equals(null));
  }

  @Test
  /**
   * Tests the call/put description.
   */
  public void callPut() {
    final ForexOptionVanilla optPositiveCall = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertTrue("Forex vanilla option call/put: Positive amount / call", optPositiveCall.isCall());
    final ForexOptionVanilla optPositivePut = new ForexOptionVanilla(FX, EXPIRATION_TIME, !IS_CALL, IS_LONG);
    assertTrue("Forex vanilla option call/put: Positive amount / put", !optPositivePut.isCall());
    final ForexDefinition fxNegativeDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, -NOMINAL_1, FX_RATE);
    final Forex fxNegative = fxNegativeDefinition.toDerivative(REFERENCE_DATE);
    final ForexOptionVanilla optNegativePut = new ForexOptionVanilla(fxNegative, EXPIRATION_TIME, !IS_CALL, IS_LONG);
    assertTrue("Forex vanilla option call/put: Negative amount / put", optNegativePut.isCall());
    final ForexOptionVanilla optNegativeCall = new ForexOptionVanilla(fxNegative, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertTrue("Forex vanilla option call/put: Negative amount / call", !optNegativeCall.isCall());
  }

  @Test
  public void equalHash() {
    assertTrue(FX_OPTION.equals(FX_OPTION));
    final ForexOptionVanilla otherOption = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertTrue(otherOption.equals(FX_OPTION));
    assertEquals(FX_OPTION.hashCode(), otherOption.hashCode());
    final ForexOptionVanilla otherOptionShort1 = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL, !IS_LONG);
    final ForexOptionVanilla otherOptionShort2 = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL, !IS_LONG);
    assertTrue(otherOptionShort1.equals(otherOptionShort2));
    assertEquals(otherOptionShort1.hashCode(), otherOptionShort2.hashCode());
    ForexOptionVanilla modifiedOption;
    modifiedOption = new ForexOptionVanilla(FX, EXPIRATION_TIME, !IS_CALL, IS_LONG);
    assertFalse(modifiedOption.equals(FX_OPTION));
    modifiedOption = new ForexOptionVanilla(FX, EXPIRATION_TIME - 0.01, IS_CALL, IS_LONG);
    assertFalse(modifiedOption.equals(FX_OPTION));
    modifiedOption = new ForexOptionVanilla(FX, EXPIRATION_TIME, IS_CALL, !IS_LONG);
    assertFalse(modifiedOption.equals(FX_OPTION));
    final ForexDefinition modifiedFxDefinition = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1 + 1.0, FX_RATE);
    final Forex modifiedFx = modifiedFxDefinition.toDerivative(REFERENCE_DATE);
    modifiedOption = new ForexOptionVanilla(modifiedFx, EXPIRATION_TIME, IS_CALL, IS_LONG);
    assertFalse(modifiedOption.equals(FX_OPTION));
    final EuropeanVanillaOption option = new EuropeanVanillaOption(FX_OPTION.getStrike(), FX_OPTION.getTimeToExpiry(), FX_OPTION.isCall());
    assertFalse(FX_OPTION.equals(option));
    assertFalse(modifiedOption.equals(null));
  }

}
