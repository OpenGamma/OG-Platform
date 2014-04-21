/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the DoubleRampFunction.
 */
@Test(groups = TestGroup.UNIT)
public class DoubleRampFunctionTest {

  private static final double X1 = 0.5;
  private static final double X2 = 0.6;
  private static final double Y1 = 1;
  private static final double Y2 = 0.99;
  private static final Function1D<Double, Double> F = new DoubleRampFunction(X1, X2, Y1, Y2);

  private static final double TOLERANCE_EVALUATE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongOrder() {
    new DoubleRampFunction(X2, X1, Y1, Y2);
  }

  @Test
  public void evaluate() {
    assertEquals("DoubleRampFunction: evaluate", F.evaluate(X1 - 1e-15), Y1, TOLERANCE_EVALUATE);
    assertEquals("DoubleRampFunction: evaluate", F.evaluate(X2 + 1e-15), Y2, TOLERANCE_EVALUATE);
    assertEquals("DoubleRampFunction: evaluate", F.evaluate(0.5 * X1 + 0.5 * X2), 0.5 * Y1 + 0.5 * Y2, TOLERANCE_EVALUATE);
    assertEquals("DoubleRampFunction: evaluate", F.evaluate(0.2 * X1 + 0.8 * X2), 0.2 * Y1 + 0.8 * Y2, TOLERANCE_EVALUATE);
  }

}
