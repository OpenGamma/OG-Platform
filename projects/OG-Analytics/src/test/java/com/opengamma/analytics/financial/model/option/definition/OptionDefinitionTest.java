/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class OptionDefinitionTest {
  private static final double STRIKE = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final double YEARS = 1;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, YEARS));
  private static final Boolean IS_CALL = true;
  private static final double PAYOFF_VALUE = 34;
  private static final OptionDefinition DEFINITION = new MyOptionDefinition(STRIKE, EXPIRY, IS_CALL);
  protected static final OptionExerciseFunction<StandardOptionDataBundle> EXERCISE = new OptionExerciseFunction<StandardOptionDataBundle>() {

    @Override
    public boolean shouldExercise(final StandardOptionDataBundle data, final Double optionPrice) {
      return false;
    }
  };
  protected static final OptionPayoffFunction<StandardOptionDataBundle> PAYOFF = new OptionPayoffFunction<StandardOptionDataBundle>() {

    @Override
    public double getPayoff(final StandardOptionDataBundle data, final Double optionPrice) {
      return PAYOFF_VALUE;
    }
  };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeStrike() {
    new MyOptionDefinition(-STRIKE, EXPIRY, IS_CALL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiry() {
    new MyOptionDefinition(STRIKE, null, IS_CALL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDateAfterExpiry() {
    DEFINITION.getTimeToExpiry(DateUtils.getDateOffsetWithYearFraction(DATE, YEARS * 2));
  }

  @Test
  public void testGetters() {
    assertEquals(DEFINITION.getExerciseFunction(), EXERCISE);
    assertEquals(DEFINITION.getExpiry(), EXPIRY);
    assertEquals(DEFINITION.getPayoffFunction(), PAYOFF);
    assertEquals(DEFINITION.getStrike(), STRIKE, 0);
    assertEquals(DEFINITION.isCall(), IS_CALL);
    assertEquals(DEFINITION.getTimeToExpiry(DATE), YEARS, 0);
  }

  @Test
  public void testEqualsAndHashCode() {
    final OptionDefinition definition1 = new MyOptionDefinition(STRIKE, EXPIRY, IS_CALL);
    final OptionDefinition definition2 = new MyOptionDefinition(2 * STRIKE, EXPIRY, IS_CALL);
    final OptionDefinition definition3 = new MyOptionDefinition(STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, YEARS * 2)), IS_CALL);
    final OptionDefinition definition4 = new MyOptionDefinition(STRIKE, EXPIRY, !IS_CALL);
    assertEquals(DEFINITION, definition1);
    assertEquals(DEFINITION.hashCode(), definition1.hashCode());
    assertFalse(DEFINITION.equals(definition2));
    assertFalse(DEFINITION.equals(definition3));
    assertFalse(DEFINITION.equals(definition4));
  }

  private static class MyOptionDefinition extends OptionDefinition {

    public MyOptionDefinition(final Double strike, final Expiry expiry, final Boolean isCall) {
      super(strike, expiry, isCall);
    }

    @Override
    public OptionExerciseFunction<StandardOptionDataBundle> getExerciseFunction() {
      return EXERCISE;
    }

    @Override
    public OptionPayoffFunction<StandardOptionDataBundle> getPayoffFunction() {
      return PAYOFF;
    }

  }
}
