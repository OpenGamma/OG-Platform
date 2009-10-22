/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.CompareUtils;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class MinimumBracketer {
  private static final double ZERO = 1e-15;
  protected static final double GOLDEN = 0.61803399;

  public abstract Double[] getBracketedPoints(Function1D<Double, Double> f, Double xLower, Double xUpper);

  protected void checkInputs(final Function1D<Double, Double> f, final Double xLower, final Double xUpper) {
    if (f == null)
      throw new IllegalArgumentException("Function was null");
    if (xLower == null)
      throw new IllegalArgumentException("Lower value was null");
    if (xUpper == null)
      throw new IllegalArgumentException("Upper value was null");
    if (CompareUtils.closeEquals(xLower, xUpper, ZERO))
      throw new IllegalArgumentException("Lower and upper values were not distinct");
  }
}
