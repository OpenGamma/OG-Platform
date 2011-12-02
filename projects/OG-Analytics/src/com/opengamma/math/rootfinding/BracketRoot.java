/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * Class that brackets single root of a function. For a 1-D function ({@link com.opengamma.math.function.Function1D}) {@latex.inline $f(x)$}, 
 * initial values for the interval, {@latex.inline $x_1$} and {@latex.inline $x_2$}, are supplied.
 * <p>
 * A root is assumed to be bracketed if {@latex.inline $f(x_1)f(x_2) < 0$}. If this condition is not satisfied, then either
 * {@latex.inline $|f(x_1)| < |f(x_2)|$}, in which case the lower value {@latex.inline $x_1$} is shifted in the negative {@latex.inline $x$} direction, or
 * the upper value {@latex.inline $x_2$} is shifted in the positive {@latex.inline $x$} direction. The amount by which to shift is the difference between
 * the two {@latex.inline $x$} values multiplied by a constant ratio (1.5). If a root is not bracketed after 50 attempts, an exception is thrown.
 */
public class BracketRoot {
  private static final double RATIO = 1.6;
  private static final int MAX_STEPS = 50;

  /**
   * @param f The function, not null
   * @param xLower Initial value of lower bracket
   * @param xUpper Initial value of upper bracket
   * @return The bracketed points as an array, where the first element is the lower bracket and the second the upper bracket.
   * @throws MathException If a root is not bracketed in 50 attempts.
   */
  public double[] getBracketedPoints(final Function1D<Double, Double> f, final double xLower, final double xUpper) {
    Validate.notNull(f, "f");
    double x1 = xLower;
    double x2 = xUpper;
    double f1 = 0;
    double f2 = 0;
    for (int count = 0; count < MAX_STEPS; count++) {
      f1 = f.evaluate(x1);
      f2 = f.evaluate(x2);
      if (f1 * f2 < 0) {
        return new double[] {x1, x2 };
      }
      if (Math.abs(f1) < Math.abs(f2)) {
        x1 += RATIO * (x1 - x2);
      } else {
        x2 += RATIO * (x2 - x1);
      }
    }
    throw new MathException("Failed to bracket root");
  }

  public double[] getBracketedPoints(final Function1D<Double, Double> f, final double xLower, final double xUpper, final double minX, final double maxX) {
    Validate.notNull(f, "f");
    Validate.isTrue(xLower >= minX, "xLower < minX");
    Validate.isTrue(xUpper <= maxX, "xUpper < maxX");
    double x1 = xLower;
    double x2 = xUpper;
    double f1 = 0;
    double f2 = 0;
    for (int count = 0; count < MAX_STEPS; count++) {
      f1 = f.evaluate(x1);
      f2 = f.evaluate(x2);
      if (f1 * f2 < 0) {
        return new double[] {x1, x2 };
      }
      if (x1 == minX && x2 == maxX) {
        throw new MathException("Failed to bracket root: no root between minX and maxX");
      }
      if (Math.abs(f1) < Math.abs(f2)) {
        x1 += RATIO * (x1 - x2);
        x1 = Math.max(minX, x1);
      } else {
        x2 += RATIO * (x2 - x1);
        x2 = Math.min(maxX, x2);
      }
    }
    throw new MathException("Failed to bracket root: max interations");
  }

}
