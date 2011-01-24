/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class GoldenSectionMinimizer1D implements ScalarMinimizer {
  private static final double GOLDEN = 0.61803399;
  private static final double COMPLEMENT = 1 - GOLDEN;
  private static final MinimumBracketer BRACKETER = new ParabolicMinimumBracketer();
  private static final int MAX_ITER = 10000;
  private static final double EPS = 1e-12;

  @Override
  public double minimize(final Function1D<Double, Double> f, final double unusedStartPosition, final double point1, final double point2) {
    Validate.notNull(f, "function");
    double x0, x1, x2, x3, f1, f2, temp;
    int i = 0;
    final double[] triplet = BRACKETER.getBracketedPoints(f, point1, point2);
    x0 = triplet[0];
    x3 = triplet[2];
    if (Math.abs(triplet[2] - triplet[1]) > Math.abs(triplet[1] - triplet[0])) {
      x1 = triplet[1];
      x2 = triplet[1] + COMPLEMENT * (triplet[2] - triplet[1]);
    } else {
      x2 = triplet[1];
      x1 = triplet[1] - COMPLEMENT * (triplet[1] - triplet[0]);
    }
    f1 = f.evaluate(x1);
    f2 = f.evaluate(x2);
    while (Math.abs(x3 - x0) > EPS * (Math.abs(x1) + Math.abs(x2))) {
      if (f2 < f1) {
        temp = GOLDEN * x2 + COMPLEMENT * x3;
        x0 = x1;
        x1 = x2;
        x2 = temp;
        f1 = f2;
        f2 = f.evaluate(temp);
      } else {
        temp = GOLDEN * x1 + COMPLEMENT * x0;
        x3 = x2;
        x2 = x1;
        x1 = temp;
        f2 = f1;
        f1 = f.evaluate(temp);
      }
      i++;
      if (i > MAX_ITER) {
        throw new MathException("Could not find minimum: this should not happen because minimum should have been successfully bracketted");
      }
    }
    if (f1 < f2) {
      return x1;
    }
    return x2;
  }

  @Override
  public Double minimize(final Function1D<Double, Double> function, final Double startPosition) {
    throw new NotImplementedException();
  }
}
