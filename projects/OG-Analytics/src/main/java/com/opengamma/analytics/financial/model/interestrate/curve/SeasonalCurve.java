/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.StepInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Class describing a monthly seasonal adjustment curve. The curve is piecewise constant on intervals defined by a set of times. 
 * Those times should be calculated using first of month dates and  the act/act day counter (the one used for derivatives file).
 */
public final class SeasonalCurve extends FunctionalDoublesCurve {

  /**
   * Construct a seasonal curve from a reference time and the monthly factors.
   * @param steps the
   * @param monthlyFactors The monthly seasonal factors from one month to the next. The size of the array is 11 (the 12th factor is deduced from the 11 other
   * @param isAdditive 
   * as the cumulative yearly adjustment is 1). The factors represent the multiplicative factor from one month to the next. The reference time represent the initial month
   * for which there is no adjustment.
   */
  public SeasonalCurve(double[] steps, double[] monthlyFactors, boolean isAdditive) {
    super(new SeasonalFunction(steps, monthlyFactors, isAdditive));
  }

}

class SeasonalFunction extends Function1D<Double, Double> {

  /**
   * The cumulative multiplicative seasonal factors from the reference time to the next. Array of size 12 (the 1st is 1.0, it is added to simplify the implementation).
   */
  private final double[] _monthlyCumulativeFactors;

  /**
   * The cumulative multiplicative seasonal factors from the reference time to the next. Array of size 12 (the 1st is 1.0, it is added to simplify the implementation).
   */
  private final double[] _steps;

  /**
   * The number of month in a year.
   */
  private static final int NB_MONTH = 12;

  public SeasonalFunction(double[] steps, double[] monthlyFactors, boolean isAdditive) {
    Validate.notNull(monthlyFactors, "Monthly factors");
    Validate.notNull(steps, "steps");
    Validate.isTrue(monthlyFactors.length == 11, "Monthly factors with incorrect length; should be 11");
    Validate.notNull(isAdditive, "isAdditive");
    _steps = steps;

    double[] cumulativeFactors = new double[NB_MONTH];
    cumulativeFactors[0] = 1.0;

    /**
     *  monthlyFactors  
     */
    for (int loopmonth = 1; loopmonth < NB_MONTH; loopmonth++) {
      if (isAdditive) {
        cumulativeFactors[loopmonth] = cumulativeFactors[loopmonth - 1] + monthlyFactors[loopmonth - 1];
      } else {
        cumulativeFactors[loopmonth] = cumulativeFactors[loopmonth - 1] * monthlyFactors[loopmonth - 1];
      }
    }
    /**
     * Here we are constructing a 12-periodic vector of the same size of the step vector, and using the vector cumulative. 
     */
    final int numberOfSteps = steps.length;
    _monthlyCumulativeFactors = new double[numberOfSteps];
    for (int loopmonth = 0; loopmonth < numberOfSteps; loopmonth++) {
      _monthlyCumulativeFactors[loopmonth] = cumulativeFactors[loopmonth % 12];
    }
  }

  @Override
  public Double evaluate(Double x) {
    StepInterpolator1D interpolator = new StepInterpolator1D();
    Interpolator1DDataBundle dataBundle = interpolator.getDataBundleFromSortedArrays(_steps, _monthlyCumulativeFactors);
    return interpolator.interpolate(dataBundle, x);
  }
}
