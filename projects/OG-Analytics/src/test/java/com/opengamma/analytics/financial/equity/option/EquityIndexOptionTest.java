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
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexOptionTest {
  private static final boolean IS_CALL = false;
  private static final double STRIKE = 100;
  private static final Currency CCY = Currency.AUD;
  private static final ExerciseDecisionType EXERCISE = ExerciseDecisionType.AMERICAN;
  private static final double EXPIRY = 0.25;
  private static final double SETTLEMENT = 0.253;
  private static final double POINT_VALUE = 2500;
  private static final SettlementType SETTLEMENT_TYPE = SettlementType.CASH;
  private static final EquityIndexOption AMERICAN_PUT = new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeExpiry() {
    new EquityIndexOption(-EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTimeToSettlement() {
    new EquityIndexOption(EXPIRY, -SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSettlementBeforeExpiry() {
    new EquityIndexOption(EXPIRY, EXPIRY * 0.99, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStrike() {
    new EquityIndexOption(EXPIRY, SETTLEMENT, -STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, null, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroUnitAmount() {
    new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, 0, EXERCISE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExerciseType() {
    new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, null, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementType() {
    new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, null);
  }

  @Test
  public void testObject() {
    assertEquals(EXPIRY, AMERICAN_PUT.getTimeToExpiry());
    assertEquals(SETTLEMENT, AMERICAN_PUT.getTimeToSettlement());
    assertEquals(STRIKE, AMERICAN_PUT.getStrike());
    assertEquals(IS_CALL, AMERICAN_PUT.isCall());
    assertEquals(CCY, AMERICAN_PUT.getCurrency());
    assertEquals(POINT_VALUE, AMERICAN_PUT.getUnitAmount());
    assertEquals(EXERCISE, AMERICAN_PUT.getExerciseType());
    assertEquals(SETTLEMENT_TYPE, AMERICAN_PUT.getSettlementType());
    assertEquals(AMERICAN_PUT, AMERICAN_PUT);
    assertFalse(AMERICAN_PUT.equals(null));
    assertFalse(AMERICAN_PUT.equals(2.));
    EquityIndexOption other = new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
    assertEquals(AMERICAN_PUT, other);
    assertEquals(AMERICAN_PUT.hashCode(), other.hashCode());
    other = new EquityIndexOption(EXPIRY + 0.0001, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOption(EXPIRY, SETTLEMENT + 1, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE + 1, IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, !IS_CALL, CCY, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, Currency.ITL, POINT_VALUE, EXERCISE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE * 2, EXERCISE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, ExerciseDecisionType.EUROPEAN, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOption(EXPIRY, SETTLEMENT, STRIKE, IS_CALL, CCY, POINT_VALUE, EXERCISE, SettlementType.PHYSICAL);
    assertFalse(AMERICAN_PUT.equals(other));
  }

}
