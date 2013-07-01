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

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to add a check for when there are equal nodes in the interpolator e.g. if valuationDate = the maturity of the first calibration instrument

  /**
   * The curve dates.
   */
  private final ZonedDateTime[] _curveDates;
  /**
   * The interpolated or constant (if only 1 point) curve with time and rates.
   */
  private final DoublesCurve _curve;
  /**
  * TODO: 
  */
  private final double _offset;
  /**
   * The curve times shifted by the offset. 
   */
  private final double[] _shiftedTimePoints;
  /**
   * TODO:
   */
  private final double _zeroDiscountFactor;
  /**
   * The times (from base date) for the interpolated hazard rate curve. The times should match the dates.
   * Note: The times passed to the curve are often 0 an then the times related to the dates. The size of the two arrays will be different by one.
   */
  private final double[] _curveTimes;
  /**
   * The hazard rates corresponding to the different times. The rates should have the same size as the times.
   */
  private final double[] _curveRates;

  /**
   * The interpolator matching the ISDA prescription.
   */
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(ISDA_INTERPOLATOR, FLAT_EXTRAPOLATOR, ISDA_EXTRAPOLATOR);

  /**
   * Constructor from the times/tenors and rates.
   * @param curveDates The dates of the curve.
   * @param curveTimes The curve times.
   * @param curveRates The curve hazard rates.
   * @param offset The offset.
   */
  public HazardRateCurve(final ZonedDateTime[] curveDates, final double[] curveTimes, final double[] curveRates, final double offset) {
    ArgumentChecker.notNull(curveDates, "curve dates");
    ArgumentChecker.notNull(curveTimes, "curve times");
    ArgumentChecker.notNull(curveRates, "curve rates");
    final int n = curveDates.length;
    ArgumentChecker.isTrue(n > 0, "Must have at least one data point");
    // ArgumentChecker.isTrue(times.length == n, "number of times {} must equal number of dates {}", times.length, n);
    //TODO: Decide how to represent dates/times. Do we include the pricing date in dates and 0 in times? Currently it is not coherent between all usages.
    ArgumentChecker.isTrue(curveRates.length == curveTimes.length, "number of rates {} must equal number of times {}", curveRates.length, n);
    _offset = offset;
    _curveDates = new ZonedDateTime[n];
    System.arraycopy(curveDates, 0, _curveDates, 0, n);
    final int length = curveTimes.length;
    _curveTimes = new double[length];
    System.arraycopy(curveTimes, 0, _curveTimes, 0, length);
    _curveRates = new double[length];
    System.arraycopy(curveRates, 0, _curveRates, 0, length);
    // Choose interpolation/extrapolation to match the behaviour of curves in the ISDA CDS reference code
    if (length > 1) {
      _curve = InterpolatedDoublesCurve.fromSorted(curveTimes, curveRates, INTERPOLATOR);
    } else {
      _curve = ConstantDoublesCurve.from(curveRates[0]); // Unless the curve is flat, in which case use a constant curve
    }
    _shiftedTimePoints = new double[n]; // TODO: [PLAT-3571] Check the size. Should it be curveTenors.length or times.length?
    for (int i = 0; i < n; ++i) {
      _shiftedTimePoints[i] = curveTimes[i] + _offset;
    }
    _zeroDiscountFactor = Math.exp(_offset * getHazardRate(0.0));
  }

  /**
   * Helper.
   * @param curveDates The dates of the curve.
   * @param curveTimes The curve times.
   * @param curveRates The curve hazard rates.
   * @return The hazard rate curve with an offset of 0.
   * @deprecated Do not use.
   */
  @Deprecated
  public HazardRateCurve bootstrapHelperHazardRateCurve(final ZonedDateTime[] curveDates, final double[] curveTimes, final double[] curveRates) {
    ArgumentChecker.notNull(curveDates, "curve tenors");
    ArgumentChecker.notNull(curveTimes, "Tenors as doubles field");
    ArgumentChecker.notNull(curveRates, "Hazard rates field");
    return new HazardRateCurve(curveDates, curveTimes, curveRates, 0.0);
  }

  /**
   * Returns the offset quantity.
   * @return The offset.
   */
  public double getOffset() {
    return _offset;
  }

  /**
   * Returns the hazard curve.
   * @return The curve.
   */
  public DoublesCurve getCurve() {
    return _curve;
  }

  /**
   * Returns the curve dates.
   * @return The dates.
   */
  public ZonedDateTime[] getCurveTenors() {
    return _curveDates;
  }

  /**
   * Returns the curve times shifted by the offset.
   * @return The shifted times.
   */
  public double[] getShiftedTimePoints() {
    return _shiftedTimePoints;
  }

  public double getZeroDiscountFactor() {
    return _zeroDiscountFactor;
  }

  /**
   * Returns the hazard rate at a given time.
   * @param t The time.
   * @return The hazard rate.
   */
  public double getHazardRate(final Double t) {
    return _curve.getYValue(t - _offset);
  }

  /**
   * Returns the survival probability at a given time.
   * @param t The time.
   * @return The probability.
   */
  public double getSurvivalProbability(final double t) {
    return Math.exp((_offset - t) * getHazardRate(t)) / _zeroDiscountFactor;
  }

  public int getNumberOfCurvePoints() {
    return _shiftedTimePoints.length;
  }

  /**
   * Returns the curve times.
   * @return The curve times.
   */
  public double[] getTimes() {
    return _curveTimes;
  }

  /**
   * Returns the curve rates.
   * @return The curve rates.
   */
  public double[] getRates() {
    return _curveRates;
  }

  /**
   * Create a new hazard curve with the nonzero time rates in the curve. 
   * Note the first rate is a t = 0.0 so its value (in the context of a r(t)*t interpolation) is irrelevant 
   * @param rates The new rates.
   * @return The new hazard rate curve.
   */
  public HazardRateCurve withRates(final double[] rates) {
    ArgumentChecker.notEmpty(rates, "null rates");
    final int n = _curveRates.length - 1;
    ArgumentChecker.isTrue(n == rates.length, "rates length {}, must be {}", rates.length, n);
    double[] temp = new double[n + 1];
    System.arraycopy(rates, 0, temp, 1, n);
    temp[0] = rates[0];
    return new HazardRateCurve(_curveDates, _curveTimes, temp, _offset);
  }

  public HazardRateCurve withRate(final double rate, final int index) {
    final int n = _curveRates.length;
    double[] temp = new double[n];
    System.arraycopy(_curveRates, 0, temp, 0, n);
    temp[index] = rate;
    return new HazardRateCurve(_curveDates, _curveTimes, temp, _offset);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  // TODO: Recreate hash-code and equals with the relevant data (rates, etc.)
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_curveDates);
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
    if (!Arrays.equals(_curveDates, other._curveDates)) {
      return false;
    }
    if (!Arrays.equals(_shiftedTimePoints, other._shiftedTimePoints)) {
      return false;
    }
    return true;
  }

}
