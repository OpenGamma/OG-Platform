/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.MathException;
import com.opengamma.math.UtilFunctions;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class ParabolicMinimumBracketerTest extends MinimumBracketerTestCase {
  private static final MinimumBracketer BRACKETER = new ParabolicMinimumBracketer();
  private static final Function1D<Double, Double> LINEAR = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 2 * x - 4;
    }

  };
  private static final Function1D<Double, Double> QUADRATIC = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x + 7 * x + 12;
    }

  };
  private static final Function1D<Double, Double> MOD_QUADRATIC = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return Math.abs(x * x - 4);
    }
  };

  private static final Function1D<Double, Double> STRETCHED_QUADRATIC = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return UtilFunctions.square((x - 50) / 50.0);
    }
  };

  @Test
  public void test() {
    testInputs(BRACKETER);
    try {
      BRACKETER.getBracketedPoints(LINEAR, 0., 1.);
      fail();
    } catch (final MathException e) {
      // Expected
    }
  }

  @Test
  public void testQuadratic() {
    testFunction(QUADRATIC, -100, 100);
    testFunction(QUADRATIC, 100, -100);
    testFunction(QUADRATIC, 100, 50);
    testFunction(QUADRATIC, -100, -50);
  }

  @Test
  public void testInitialGuessBracketsTwoMinima() {
    testFunction(MOD_QUADRATIC, -3, -1);
    testFunction(MOD_QUADRATIC, -3, 3.5);
  }

  @Test
  public void testStretchedQuadratic() {
    testFunction(STRETCHED_QUADRATIC, 0, 1);
  }

  private void testFunction(final Function1D<Double, Double> f, final double xLower, final double xUpper) {
    final double[] result = BRACKETER.getBracketedPoints(f, xLower, xUpper);
    if (result[0] < result[1]) {
      assertTrue(result[1] < result[2]);
    } else {
      assertTrue(result[2] < result[1]);
    }
    final double f2 = f.evaluate(result[1]);
    assertTrue(f.evaluate(result[0]) > f2);
    assertTrue(f.evaluate(result[2]) > f2);
  }
}
