/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.hazardratecurve;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.ISDA_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.ISDA_INTERPOLATOR;

import java.util.Arrays;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class for constructing and querying a term structure of (calibrated) hazard rates
 * Partially adopted from the RiskCare implementation of the ISDA model
 */
public class HazardRateCurve {

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(ISDA_INTERPOLATOR, FLAT_EXTRAPOLATOR, ISDA_EXTRAPOLATOR);

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final double _offset;

  private final DoublesCurve _curve;

  private final double[] _shiftedTimePoints;

  private final ZonedDateTime[] _curveTenors;

  private final double _zeroDiscountFactor;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public HazardRateCurve(final ZonedDateTime[] curveTenors, final double[] times, final double[] rates, final double offset) {
    ArgumentChecker.notNull(curveTenors, "curve tenors");
    ArgumentChecker.notNull(times, "times");
    ArgumentChecker.notNull(rates, "rates");
    final int n = curveTenors.length;
    ArgumentChecker.isTrue(n > 0, "Must have at least one data point");
    //ArgumentChecker.isTrue(times.length == n, "number of times {} must equal number of dates {}", times.length, n);
    //ArgumentChecker.isTrue(rates.length == n, "number of rates {} must equal number of dates {}", rates.length, n);
    _offset = offset;

    _curveTenors = new ZonedDateTime[n];
    System.arraycopy(curveTenors, 0, _curveTenors, 0, n);

    // Choose interpolation/extrapolation to match the behaviour of curves in the ISDA CDS reference code
    if (times.length > 1) {
      _curve = InterpolatedDoublesCurve.fromSorted(times, rates, INTERPOLATOR);
    } else {
      _curve = ConstantDoublesCurve.from(rates[0]);  // Unless the curve is flat, in which case use a constant curve
    }
    _shiftedTimePoints = new double[n];
    for (int i = 0; i < n; ++i) {
      _shiftedTimePoints[i] = times[i] + _offset;
    }
    _zeroDiscountFactor = Math.exp(_offset * getHazardRate(0.0));
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to build a new SurvivalCurve object given the tenor and hazard rate inputs

  public HazardRateCurve bootstrapHelperHazardRateCurve(final ZonedDateTime[] curveTenors, final double[] tenorsAsDoubles, final double[] hazardRates) {
    ArgumentChecker.notNull(curveTenors, "curve tenors");
    ArgumentChecker.notNull(tenorsAsDoubles, "Tenors as doubles field");
    ArgumentChecker.notNull(hazardRates, "Hazard rates field");
    return new HazardRateCurve(curveTenors, tenorsAsDoubles, hazardRates, 0.0);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getOffset() {
    return _offset;
  }

  public DoublesCurve getCurve() {
    return _curve;
  }

  public ZonedDateTime[] getCurveTenors() {
    return _curveTenors;
  }

  public double[] getShiftedTimePoints() {
    return _shiftedTimePoints;
  }

  public double getZeroDiscountFactor() {
    return _zeroDiscountFactor;
  }

  public double getHazardRate(final Double t) {
    return _curve.getYValue(t - _offset);
  }

  public double getSurvivalProbability(final double t) {
    return Math.exp((_offset - t) * getHazardRate(t)) / _zeroDiscountFactor;
  }

  public int getNumberOfCurvePoints() {
    return _shiftedTimePoints.length;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_curveTenors);
    result = prime * result + Arrays.hashCode(_shiftedTimePoints);
    long temp;
    temp = Double.doubleToLongBits(_zeroDiscountFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HazardRateCurve)) {
      return false;
    }
    final HazardRateCurve other = (HazardRateCurve) obj;
    if (Double.compare(_zeroDiscountFactor, other._zeroDiscountFactor) != 0) {
      return false;
    }
    if (!Arrays.equals(_curveTenors, other._curveTenors)) {
      return false;
    }
    if (!Arrays.equals(_shiftedTimePoints, other._shiftedTimePoints)) {
      return false;
    }
    return true;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

}
