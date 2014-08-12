package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * A function from a vector x ({@link DoubleMatrix1D } to Boolean that returns true iff all the elements of x are positive or zero
 */
public class PositiveOrZero extends Function1D<DoubleMatrix1D, Boolean> {

  @Override
  public Boolean evaluate(DoubleMatrix1D x) {
    final double[] data = x.getData();

    for (final double value : data) {
      if (value < 0.0) {
        return false;
      }
    }
    return true;
  }
}
