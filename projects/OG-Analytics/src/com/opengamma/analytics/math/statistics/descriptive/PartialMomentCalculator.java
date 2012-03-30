/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * The second moment of a series of asset return data can be used as a measure
 * of the risk of that asset. However, in many instances a large positive
 * return is not regarded as a risk. The partial moment can be used as an
 * alternative.
 * <p>
 * The lower (higher) partial moment considers only those values that are below
 * (above) a threshold. Given a series of data $x_1, x_2, \dots, x_n$ sorted
 * from lowest to highest, the first (last) $k$ values are below (above) the
 * threshold $x_0$. The partial moment is given by:
 * $$ 
 * \begin{align*}
 * \text{pm} = \sqrt{\frac{1}{k}\sum\limits_{i=1}^{k}(x_i - x_0)^2}
 * \end{align*}
 * $$
 */
public class PartialMomentCalculator extends Function1D<double[], Double> {
  private final double _threshold;
  private final boolean _useDownSide;

  /**
   * Creates calculator with default values: threshold = 0 and useDownSide = true
   */
  public PartialMomentCalculator() {
    this(0, true);
  }

  /**
   * 
   * @param threshold The threshold value for the data
   * @param useDownSide If true, all data below the threshold is used
   */
  public PartialMomentCalculator(final double threshold, final boolean useDownSide) {
    _threshold = threshold;
    _useDownSide = useDownSide;
  }

  /**
   * @param x The array of data, not null or empty
   * @return The partial moment
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length > 0, "x cannot be empty");
    final int n = x.length;
    final double[] copyX = Arrays.copyOf(x, n);
    Arrays.sort(copyX);
    double sum = 0;
    if (_useDownSide) {
      int i = 0;
      if (copyX[i] > _threshold) {
        return 0.;
      }
      while (i < n && copyX[i] < _threshold) {
        sum += (copyX[i] - _threshold) * (copyX[i] - _threshold);
        i++;
      }
      return Math.sqrt(sum / i);
    }
    int i = n - 1;
    int count = 0;
    if (copyX[i] < _threshold) {
      return 0.;
    }
    while (i >= 0 && copyX[i] > _threshold) {
      sum += (copyX[i] - _threshold) * (copyX[i] - _threshold);
      count++;
      i--;
    }
    return Math.sqrt(sum / count);
  }

}
