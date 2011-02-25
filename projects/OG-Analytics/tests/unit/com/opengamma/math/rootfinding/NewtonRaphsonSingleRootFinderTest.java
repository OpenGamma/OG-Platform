/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.junit.Test;

import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class NewtonRaphsonSingleRootFinderTest {
  private static final DoubleFunction1D F1 = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x - 6 * x * x + 11 * x - 106;
    }

    @Override
    public DoubleFunction1D derivative() {
      return new DoubleFunction1D() {

        @Override
        public Double evaluate(final Double x) {
          return 3 * x * x - 12 * x + 11;
        }

      };
    }

  };
  private static final Function1D<Double, Double> F2 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x - 6 * x * x + 11 * x - 106;
    }

  };
  private static final Function1D<Double, Double> DF1 = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 3 * x * x - 12 * x + 11;
    }

  };
  private static final NewtonRaphsonSingleRootFinder ROOT_FINDER = new NewtonRaphsonSingleRootFinder();
  private static final double X1 = 4;
  private static final double X2 = 10;
  private static final double X3 = -10;
  private static final double ROOT = 6.713397681556;

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction1() {
    ROOT_FINDER.getRoot((Function1D<Double, Double>) null, X1, X2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLower1() {
    ROOT_FINDER.getRoot(F2, null, X2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullHigher1() {
    ROOT_FINDER.getRoot(F2, X1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction2() {
    ROOT_FINDER.getRoot(null, DF1, X1, X2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDerivative1() {
    ROOT_FINDER.getRoot(F2, null, X1, X2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullLower2() {
    ROOT_FINDER.getRoot(F2, DF1, null, X2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullHigher2() {
    ROOT_FINDER.getRoot(F2, DF1, X1, null);
  }
}
