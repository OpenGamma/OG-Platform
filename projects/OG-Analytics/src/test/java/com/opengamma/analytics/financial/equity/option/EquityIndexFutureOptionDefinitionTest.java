/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.future.definition.IndexFutureDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexFutureOptionDefinitionTest {
  private static final boolean IS_CALL = false;
  private static final double STRIKE = 100;
  private static final Currency CCY = Currency.AUD;
  private static final ExerciseDecisionType EXERCISE = ExerciseDecisionType.AMERICAN;
  private static final ZonedDateTime EXPIRY = DateUtils.getUTCDate(2013, 2, 1);
  private static final ZonedDateTime SETTLEMENT = DateUtils.getUTCDate(2013, 2, 4);
  private static final double POINT_VALUE = 2500;
  private static final double REFERENCE_PRICE = 42;
  private static final ExternalId EXTERNAL_ID = ExternalId.of(ExternalScheme.of("BLOOMBERG_TICKER"), "TEST");
  private static final IndexFutureDefinition UNDERLYING = new IndexFutureDefinition(EXPIRY, SETTLEMENT, STRIKE, CCY, POINT_VALUE, EXTERNAL_ID);
  private static final EquityIndexFutureOptionDefinition AMERICAN_PUT = new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiry() {
    new EquityIndexFutureOptionDefinition(null, UNDERLYING, STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new EquityIndexFutureOptionDefinition(EXPIRY, null, STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExerciseType() {
    new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, STRIKE, null, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStrike() {
    new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, -STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroStrike() {
    new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, 0, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
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
    assertEquals(EXERCISE, AMERICAN_PUT.getExerciseType());
    assertEquals(EXPIRY, AMERICAN_PUT.getExpiryDate());
    EquityIndexFutureOptionDefinition other = new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertEquals(AMERICAN_PUT, other);
    assertEquals(AMERICAN_PUT.hashCode(), other.hashCode());
    other = new EquityIndexFutureOptionDefinition(EXPIRY.plusDays(1), UNDERLYING, STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOptionDefinition(EXPIRY, new IndexFutureDefinition(EXPIRY, SETTLEMENT.plusDays(2), STRIKE, CCY, POINT_VALUE, EXTERNAL_ID), STRIKE, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, STRIKE + 1, EXERCISE, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, STRIKE, ExerciseDecisionType.EUROPEAN, IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, STRIKE, EXERCISE, !IS_CALL, POINT_VALUE, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
    other = new EquityIndexFutureOptionDefinition(EXPIRY, UNDERLYING, STRIKE, EXERCISE, IS_CALL, POINT_VALUE * 10, REFERENCE_PRICE);
    assertFalse(AMERICAN_PUT.equals(other));
  }

  @Test
  public void testToDerivative() {
    final ZonedDateTime valuationDate = EXPIRY.minusDays(10);
    final EquityIndexFutureOption derivative = AMERICAN_PUT.toDerivative(valuationDate);
    assertEquals(STRIKE, derivative.getStrike());
    assertEquals(10. / 365, derivative.getExpiry());
    assertEquals(13. / 365, derivative.getUnderlying().getTimeToSettlement());
    assertEquals(POINT_VALUE, derivative.getPointValue());
    assertEquals(IS_CALL, derivative.isCall());
    assertEquals(CCY, derivative.getUnderlying().getCurrency());
    assertEquals(EXERCISE, derivative.getExerciseType());
  }
}
