/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexFutureOptionTest {
  private static final boolean IS_CALL = false;
  private static final double STRIKE = 100;
  private static final Currency CCY = Currency.AUD;
  private static final ExerciseDecisionType EXERCISE = ExerciseDecisionType.AMERICAN;
  private static final double EXPIRY = 0.25;
  private static final double SETTLEMENT = 0.253;
  private static final double POINT_VALUE = 2500;
  private static final double REFERENCE_PRICE = 42;
  private static final EquityIndexFuture UNDERLYING = new EquityIndexFuture(EXPIRY, SETTLEMENT, STRIKE, CCY, POINT_VALUE);
  private static final EquityIndexFutureOption AMERICAN_PUT = new EquityIndexFutureOption(EXPIRY, UNDERLYING, STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);

  @Test
  public void testObject() {
    assertEquals(EXPIRY, AMERICAN_PUT.getExpiry());
    assertEquals(SETTLEMENT, AMERICAN_PUT.getUnderlying().getTimeToSettlement());
    assertEquals(STRIKE, AMERICAN_PUT.getStrike());
    assertEquals(IS_CALL, AMERICAN_PUT.isCall());
    assertEquals(CCY, AMERICAN_PUT.getUnderlying().getCurrency());
    assertEquals(POINT_VALUE, AMERICAN_PUT.getPointValue());
    assertEquals(EXERCISE, AMERICAN_PUT.getExerciseType());
    assertEquals(AMERICAN_PUT, AMERICAN_PUT);
    assertFalse(AMERICAN_PUT.equals(null));
    assertFalse(AMERICAN_PUT.equals(2.));
    EquityIndexFutureOption other = new EquityIndexFutureOption(EXPIRY, UNDERLYING, STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertEquals(AMERICAN_PUT, other);
    assertEquals(AMERICAN_PUT.hashCode(), other.hashCode());
    other = new EquityIndexFutureOption(EXPIRY + 0.0001, UNDERLYING, STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOption(EXPIRY, new EquityIndexFuture(EXPIRY, SETTLEMENT, STRIKE, CCY, POINT_VALUE + 1), STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOption(EXPIRY, UNDERLYING, STRIKE + 1, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOption(EXPIRY, UNDERLYING, STRIKE, ExerciseDecisionType.EUROPEAN, !IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOption(EXPIRY, UNDERLYING, STRIKE, ExerciseDecisionType.EUROPEAN, IS_CALL, POINT_VALUE * 10, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
  }

}
