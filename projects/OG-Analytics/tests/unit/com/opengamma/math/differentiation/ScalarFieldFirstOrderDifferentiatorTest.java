/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class ScalarFieldFirstOrderDifferentiatorTest {

  private static final Function1D<DoubleMatrix1D, Double> F = new Function1D<DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(final DoubleMatrix1D x) {
      double x1 = x.getEntry(0);
      double x2 = x.getEntry(1);
      return x1 * x1 + 2 * x2 * x2 - x1 * x2 + x1 * Math.cos(x2) - x2 * Math.sin(x1);
    }
  };

  private static final Function1D<DoubleMatrix1D, DoubleMatrix1D> G = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

    @Override
    public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
      double x1 = x.getEntry(0);
      double x2 = x.getEntry(1);
      double[] y = new double[2];
      y[0] = 2 * x1 - x2 + Math.cos(x2) - x2 * Math.cos(x1);
      y[1] = 4 * x2 - x1 - x1 * Math.sin(x2) - Math.sin(x1);
      return new DoubleMatrix1D(y);
    }

  };
  private static final double EPS = 1e-5;
  private static final ScalarFieldFirstOrderDifferentiator FORWARD = new ScalarFieldFirstOrderDifferentiator(FiniteDifferenceType.FORWARD, EPS);
  private static final ScalarFieldFirstOrderDifferentiator CENTRAL = new ScalarFieldFirstOrderDifferentiator(FiniteDifferenceType.CENTRAL, EPS);
  private static final ScalarFieldFirstOrderDifferentiator BACKWARD = new ScalarFieldFirstOrderDifferentiator(FiniteDifferenceType.BACKWARD, EPS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDifferenceType() {
    new ScalarFirstOrderDifferentiator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    CENTRAL.derivative((Function1D<DoubleMatrix1D, Double>) null);
  }

  @Test
  public void test() {
    final DoubleMatrix1D x = new DoubleMatrix1D(new double[] {.2245, -1.2344});
    DoubleMatrix1D anGrad = G.evaluate(x);
    DoubleMatrix1D fdFwdGrad = FORWARD.derivative(F).evaluate(x);
    DoubleMatrix1D fdCentGrad = CENTRAL.derivative(F).evaluate(x);
    DoubleMatrix1D fdBackGrad = BACKWARD.derivative(F).evaluate(x);

    for (int i = 0; i < 2; i++) {
      assertEquals(fdFwdGrad.getEntry(i), anGrad.getEntry(i), 10 * EPS);
      assertEquals(fdCentGrad.getEntry(i), anGrad.getEntry(i), EPS * EPS);
      assertEquals(fdBackGrad.getEntry(i), anGrad.getEntry(i), 10 * EPS);
    }
  }

}
