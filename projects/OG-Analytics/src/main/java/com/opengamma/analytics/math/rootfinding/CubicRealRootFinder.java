/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * Root finder that calculates the roots of a cubic equation using {@link CubicRootFinder} and returns only the real roots. If
 * there are no real roots, an exception is thrown.
 */
public class CubicRealRootFinder implements Polynomial1DRootFinder<Double> {
  private static final Double[] EMPTY_ARRAY = new Double[0];
  private static final Polynomial1DRootFinder<ComplexNumber> ROOT_FINDER = new CubicRootFinder();

  @Override
  public Double[] getRoots(final RealPolynomialFunction1D function) {
    ArgumentChecker.notNull(function, "function");
    final double[] coefficients = function.getCoefficients();
    if (coefficients.length != 4) {
      throw new IllegalArgumentException("Function is not a cubic");
    }
    final ComplexNumber[] result = ROOT_FINDER.getRoots(function);
    final List<Double> reals = new ArrayList<>();
    for (final ComplexNumber c : result) {
      if (CompareUtils.closeEquals(c.getImaginary(), 0, 1e-16)) {
        reals.add(c.getReal());
      }
    }
    ArgumentChecker.isTrue(reals.size() > 0, "Could not find any real roots");
    return reals.toArray(EMPTY_ARRAY);
  }
}
