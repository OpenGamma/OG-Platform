/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Class describing a monthly seasonal adjustment curve. The curve is piecewise constant on intervals centered on a reference time plus + j/12.
 */
public final class SeasonalCurve extends FunctionalDoublesCurve {

  /**
   * Construct a seasonal curve from a reference time and the monthly factors.
   * @param referenceTime The reference time for with there is no seasonal adjustment.
   * @param monthlyFactors The monthly seasonal factors from one month to the next. The size of the array is 11 (the 12th factor is deduced from the 11 other 
   * as the cumulative yearly adjustment is 1). The factors represent the multiplicative factor from one month to the next. The reference time represent the initial month
   * for which there is no adjustment.
   */
  public SeasonalCurve(double referenceTime, double[] monthlyFactors) {
    super(new SeasonalFunction(referenceTime, monthlyFactors));
  }

}

class SeasonalFunction extends Function1D<Double, Double> {

  /**
   * The reference time for with there is no seasonal adjustment.
   */
  private final double _referenceTime;
  /**
   * The cumulative multiplicative seasonal factors from the reference time to the next. Array of size 12 (the 1st is 1.0, it is added to simplify the implementation).
   */
  private final double[] _monthlyCumulativeFactors;
  /**
   * The number of month in a year.
   */
  private static final int NB_MONTH = 12;

  public SeasonalFunction(double referenceTime, double[] monthlyFactors) {
    Validate.notNull(monthlyFactors, "Monthly factors");
    Validate.isTrue(monthlyFactors.length == 11, "Monthly factors with incorrect length; should be 11");
    _monthlyCumulativeFactors = new double[NB_MONTH];
    _monthlyCumulativeFactors[0] = 1.0;
    for (int loopmonth = 1; loopmonth < NB_MONTH; loopmonth++) {
      _monthlyCumulativeFactors[loopmonth] = _monthlyCumulativeFactors[loopmonth - 1] * monthlyFactors[loopmonth - 1];
    }
    _referenceTime = referenceTime;
  }

  @Override
  public Double evaluate(Double x) {
    long relativeXRounded = Math.round((x - _referenceTime) * NB_MONTH);
    int relativeMonth = (int) (relativeXRounded % NB_MONTH);
    relativeMonth = (relativeMonth < 0) ? relativeMonth + NB_MONTH : relativeMonth;
    return _monthlyCumulativeFactors[relativeMonth];
  }

}
