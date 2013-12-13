/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AgricultureFutureOptionDefinitionTest {
  private final static ExternalId AN_ID = ExternalId.of("Scheme", "value");
  private final static ZonedDateTime FIRST_DELIVERY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private final static ZonedDateTime LAST_DELIVERY_DATE = DateUtils.getUTCDate(2012, 9, 21);
  private final static ZonedDateTime SETTLEMENT_DATE = LAST_DELIVERY_DATE;
  private final static ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private final static ZonedDateTime A_DATE = DateUtils.getUTCDate(2011, 9, 20);
  private final static AgricultureFutureDefinition AN_UNDERLYING = new AgricultureFutureDefinition(EXPIRY_DATE, AN_ID, 100, FIRST_DELIVERY_DATE, LAST_DELIVERY_DATE, 1000, "tonnes",
      SettlementType.PHYSICAL, 0, Currency.GBP, SETTLEMENT_DATE);

  /**
   * Test hashCode and equals methods.
   */
  @Test()
  public void testHashEquals() {
    AgricultureFutureOptionDefinition first = new AgricultureFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.EUROPEAN, true);
    AgricultureFutureOptionDefinition second = new AgricultureFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.EUROPEAN, true);
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
    AgricultureFutureOptionDefinition third = new AgricultureFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.AMERICAN, false);
    assertFalse(first.equals(third));
    assertFalse(second.hashCode() == third.hashCode());
  }

  /**
   * Test getters
   */
  @Test
  public void testGetters() {
    AgricultureFutureOptionDefinition first = new AgricultureFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.EUROPEAN, true);
    AgricultureFutureOptionDefinition second = new AgricultureFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.AMERICAN, false);
    assertEquals(first.getStrike(), 100.);
    assertEquals(first.getExerciseType(), ExerciseDecisionType.EUROPEAN);
    assertEquals(first.getUnderlying(), AN_UNDERLYING);
    assertTrue(first.isCall());
    assertEquals(second.getExerciseType(), ExerciseDecisionType.AMERICAN);
    assertFalse(second.isCall());
  }

  /**
   * Test method for {@link com.opengamma.analytics.financial.commodity.definition.AgricultureFutureOptionDefinition#toDerivative(javax.time.calendar.ZonedDateTime)}.
   */
  @Test
  public void testToDerivative() {
    AgricultureFutureOptionDefinition first = new AgricultureFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.EUROPEAN, true);
    AgricultureFutureOptionDefinition second = new AgricultureFutureOptionDefinition(EXPIRY_DATE, AN_UNDERLYING, 100, ExerciseDecisionType.AMERICAN, false);

    AgricultureFutureOption firstDerivative = first.toDerivative(A_DATE);
    AgricultureFutureOption secondDerivative = second.toDerivative(A_DATE);
    assertEquals(firstDerivative.getStrike(), 100.);
    assertEquals(firstDerivative.getExerciseType(), ExerciseDecisionType.EUROPEAN);
    assertEquals(firstDerivative.getUnderlying(), AN_UNDERLYING.toDerivative(A_DATE));
    assertTrue(firstDerivative.isCall());
    assertEquals(secondDerivative.getExerciseType(), ExerciseDecisionType.AMERICAN);
    assertFalse(secondDerivative.isCall());

    AgricultureFutureOption firstDerivative2 = new AgricultureFutureOption(0.0027397260273972603, AN_UNDERLYING.toDerivative(A_DATE), 100, ExerciseDecisionType.EUROPEAN, true);
    assertEquals(firstDerivative.hashCode(), firstDerivative2.hashCode());
    assertEquals(firstDerivative, firstDerivative2);
  }

}
