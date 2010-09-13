/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * The sample standard deviation of a series of asset return data can be used as a measure of the risk of that asset. However, in many instances 
 * a large positive return is not regarded as a risk. The semi-standard deviation can be used as an alternative.
 * <p>
 * The semi-standard deviation considers only those values that are above or below a threshold. Given a series of data {@latex.inline $x_1, x_2, \\dots, x_n$} sorted 
 * from lowest to highest, the first (last) {@latex.inline $k$} values are below (above) the threshold {@latex.inline $x_0$}. The semi-standard deviation is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}} 
 * \\begin{eqnarray*}
 * \\sigma_{semi} = \\sqrt{\\frac{1}{k}\\sum\\limits_{i=1}^{k}(x_i - x_0)^2}
 * \\end{eqnarray*}}
 */
public class SampleSemiStandardDeviationCalculator extends Function1D<double[], Double> {
  private final double _threshold;
  private final boolean _useDownSide;

  /**
   * Creates calculator with default values: threshold = 0 and useDownSide = true
   */
  public SampleSemiStandardDeviationCalculator() {
    this(0, true);
  }

  /**
   * 
   * @param threshold The threshold value for the data
   * @param useDownSide If true, all data below the threshold is used
   */
  public SampleSemiStandardDeviationCalculator(final double threshold, final boolean useDownSide) {
    _threshold = threshold;
    _useDownSide = useDownSide;
  }

  /**
   * @param x The array of data
   * @return The semi-standard deviation
   * @throws IllegalArgumentException If the array is null
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    final int n = x.length;
    final double[] copyX = Arrays.copyOf(x, n);
    Arrays.sort(copyX);
    double sum = 0;
    if (_useDownSide) {
      int i = 0;
      while (copyX[i] < _threshold) {
        sum += (copyX[i] - _threshold) * (copyX[i] - _threshold);
        i++;
      }
      return Math.sqrt(sum / i);
    }
    int i = n - 1;
    int count = 0;
    while (copyX[i] > _threshold) {
      sum += (copyX[i] - _threshold) * (copyX[i] - _threshold);
      count++;
      i--;
    }
    return Math.sqrt(sum / count);
  }

}
