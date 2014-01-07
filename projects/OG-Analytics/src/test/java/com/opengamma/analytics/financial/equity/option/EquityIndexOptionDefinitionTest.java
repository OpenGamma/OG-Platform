/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexOptionDefinitionTest {
  private static final boolean IS_CALL = false;
  private static final double STRIKE = 100;
  private static final Currency CCY = Currency.AUD;
  private static final ExerciseDecisionType EXERCISE = ExerciseDecisionType.AMERICAN;
  private static final ZonedDateTime EXPIRY = DateUtils.getUTCDate(2013, 2, 1);
  private static final LocalDate SETTLEMENT = LocalDate.of(2013, 2, 4);
  private static final double POINT_VALUE = 2500;
  private static final SettlementType SETTLEMENT_TYPE = SettlementType.CASH;
  private static final EquityIndexOptionDefinition AMERICAN_PUT = new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE,
      SETTLEMENT_TYPE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStrike() {
    new EquityIndexOptionDefinition(IS_CALL, -STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroStrike() {
    new EquityIndexOptionDefinition(IS_CALL, 0, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new EquityIndexOptionDefinition(IS_CALL, STRIKE, null, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExerciseType() {
    new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, null, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiry() {
    new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, null, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementDate() {
    new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, null, POINT_VALUE, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroPointValue() {
    new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT, 0, SETTLEMENT_TYPE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettlementType() {
    new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValuationDate() {
    AMERICAN_PUT.toDerivative(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValuationAfterExpiry() {
    AMERICAN_PUT.toDerivative(EXPIRY.plusDays(1));
  }

  @Test
  public void testObject() {
    assertEquals(AMERICAN_PUT, AMERICAN_PUT);
    assertFalse(AMERICAN_PUT.equals(null));
    assertFalse(AMERICAN_PUT.equals(2.));
    assertEquals(IS_CALL, AMERICAN_PUT.isCall());
    assertEquals(STRIKE, AMERICAN_PUT.getStrike());
    assertEquals(CCY, AMERICAN_PUT.getCurrency());
    assertEquals(EXERCISE, AMERICAN_PUT.getExerciseType());
    assertEquals(EXPIRY, AMERICAN_PUT.getExpiryDate());
    assertEquals(SETTLEMENT, AMERICAN_PUT.getSettlementDate());
    assertEquals(POINT_VALUE, AMERICAN_PUT.getPointValue());
    assertEquals(SETTLEMENT_TYPE, AMERICAN_PUT.getSettlementType());
    EquityIndexOptionDefinition other = new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
    assertEquals(AMERICAN_PUT, other);
    assertEquals(AMERICAN_PUT.hashCode(), other.hashCode());
    other = new EquityIndexOptionDefinition(!IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOptionDefinition(IS_CALL, STRIKE + 1, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOptionDefinition(IS_CALL, STRIKE, Currency.USD, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, ExerciseDecisionType.EUROPEAN, EXPIRY, SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY.plusYears(1), SETTLEMENT, POINT_VALUE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT.plusYears(1), POINT_VALUE, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE * 2, SETTLEMENT_TYPE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexOptionDefinition(IS_CALL, STRIKE, CCY, EXERCISE, EXPIRY, SETTLEMENT, POINT_VALUE, SettlementType.PHYSICAL);
    assertFalse(AMERICAN_PUT.equals(other));
  }

  @Test
  public void testToDerivative() {
    final ZonedDateTime valuationDate = EXPIRY.minusDays(10);
    final EquityIndexOption derivative = AMERICAN_PUT.toDerivative(valuationDate);
    assertEquals(STRIKE, derivative.getStrike());
    assertEquals(10. / 365, derivative.getTimeToExpiry());
    assertEquals(13. / 365, derivative.getTimeToSettlement());
    assertEquals(POINT_VALUE, derivative.getUnitAmount());
    assertEquals(IS_CALL, derivative.isCall());
    assertEquals(CCY, derivative.getCurrency());
    assertEquals(SETTLEMENT_TYPE, derivative.getSettlementType());
    assertEquals(EXERCISE, derivative.getExerciseType());
  }
}
