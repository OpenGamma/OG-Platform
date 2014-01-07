/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CashOrNothingOptionDefinitionTest {
  private static final double SPOT = 100;
  private static final double STRIKE = 100;
  private static final double PAYMENT = 5;
  private static final boolean IS_CALL = true;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2010, 7, 1));
  private static final CashOrNothingOptionDefinition CALL = new CashOrNothingOptionDefinition(STRIKE, EXPIRY, true, PAYMENT);
  private static final CashOrNothingOptionDefinition PUT = new CashOrNothingOptionDefinition(STRIKE, EXPIRY, false, PAYMENT);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.05)), 0, new VolatilitySurface(ConstantDoublesSurface.from(0.2)), SPOT,
      DateUtils.getUTCDate(2010, 1, 1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePayment() {
    new CashOrNothingOptionDefinition(STRIKE, EXPIRY, IS_CALL, -PAYMENT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALL.getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void test() {
    assertEquals(CALL.getPayment(), PAYMENT, 0);
    CashOrNothingOptionDefinition other = new CashOrNothingOptionDefinition(STRIKE, EXPIRY, IS_CALL, PAYMENT);
    assertEquals(CALL, other);
    assertEquals(CALL.hashCode(), other.hashCode());
    other = new CashOrNothingOptionDefinition(STRIKE + 1, EXPIRY, IS_CALL, PAYMENT);
    assertFalse(CALL.equals(other));
    other = new CashOrNothingOptionDefinition(STRIKE, new Expiry(DateUtils.getUTCDate(2010, 8, 1)), IS_CALL, PAYMENT);
    assertFalse(CALL.equals(other));
    other = new CashOrNothingOptionDefinition(STRIKE, EXPIRY, !IS_CALL, PAYMENT);
    assertFalse(CALL.equals(other));
    other = new CashOrNothingOptionDefinition(STRIKE, EXPIRY, IS_CALL, PAYMENT + 1);
    assertFalse(CALL.equals(other));
  }

  @Test
  public void testPayoffFunction() {
    OptionPayoffFunction<StandardOptionDataBundle> f = CALL.getPayoffFunction();
    final double delta = 40;
    assertEquals(f.getPayoff(DATA.withSpot(SPOT + delta), null), 0, 0);
    assertEquals(f.getPayoff(DATA.withSpot(SPOT - delta), null), PAYMENT, 0);
    f = PUT.getPayoffFunction();
    assertEquals(f.getPayoff(DATA.withSpot(SPOT + delta), null), PAYMENT, 0);
    assertEquals(f.getPayoff(DATA.withSpot(SPOT - delta), null), 0, 0);
  }

  @Test
  public void testExerciseFunction() {
    OptionExerciseFunction<StandardOptionDataBundle> f = CALL.getExerciseFunction();
    assertFalse(f.shouldExercise(DATA.withSpot(SPOT + 1), null));
    assertFalse(f.shouldExercise(DATA.withSpot(SPOT - 1), null));
    f = PUT.getExerciseFunction();
    assertFalse(f.shouldExercise(DATA.withSpot(SPOT + 1), null));
    assertFalse(f.shouldExercise(DATA.withSpot(SPOT - 1), null));
  }
}
