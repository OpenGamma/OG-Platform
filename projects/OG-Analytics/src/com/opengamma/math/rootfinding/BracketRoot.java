/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class BracketRoot {
  private static double s_ratio = 1.6;
  private static int s_maxSteps = 50;

  public double[] getBracketedPoints(final Function1D<Double, Double> f, final double xLower, final double xUpper) {

    if (xLower >= xUpper) {
      throw new IllegalArgumentException("Need xLower less than xUpper");
    }

    double x1 = xLower;
    double x2 = xUpper;
    double f1;
    double f2;

    for (int count = 0; count < s_maxSteps; count++) {
      f1 = f.evaluate(x1);
      f2 = f.evaluate(x2);
      if (f1 * f2 < 0) {
        return new double[] {x1, x2};
      }
      if (Math.abs(f1) < Math.abs(f2)) {
        x1 += s_ratio * (x1 - x2);
      } else {
        x2 += s_ratio * (x2 - x1);
      }
    }
    throw new MathException("Failed to bracket root");
  }

}
