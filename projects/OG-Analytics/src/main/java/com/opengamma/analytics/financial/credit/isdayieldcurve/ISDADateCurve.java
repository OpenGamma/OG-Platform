/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.ISDA_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.ISDA_INTERPOLATOR;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ISDADateCurve {

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  // ------------------------------------------------------------------------------------------------------------------------------------

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(ISDA_INTERPOLATOR, FLAT_EXTRAPOLATOR, ISDA_EXTRAPOLATOR);

  private final String _name;

  private final double _offset;

  private final ZonedDateTime[] _curveDates;

  private final DoublesCurve _curve;

  private final double[] _shiftedTimePoints;

  private final double _zeroDiscountFactor;

  private final int _n;

  // ------------------------------------------------------------------------------------------------------------------------------------

  // Overloaded ctor to take in the output from the native ISDA yield curve construction model
  public ISDADateCurve(final String name, final ZonedDateTime baseDate, final ZonedDateTime[] curveDates, final double[] rates, final double offset) {
    this(name, baseDate, curveDates, rates, offset, ACT_365);
  }

  public ISDADateCurve(final String name, final ZonedDateTime baseDate, final ZonedDateTime[] curveDates, final double[] rates, final double offset,
      final DayCount dayCount) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(baseDate, "base date");
    ArgumentChecker.notNull(curveDates, "curve dates");
    ArgumentChecker.notNull(rates, "rates");
    ArgumentChecker.notNull(dayCount, "day count");
    _n = curveDates.length;
    ArgumentChecker.isTrue(_n != 0, "Data arrays were empty");
    //ArgumentChecker.isTrue(_n == rates.length, "Have {} rates for {} dates", rates.length, _n);

    _name = name;
    _offset = offset;
    _curveDates = new ZonedDateTime[_n];
    System.arraycopy(curveDates, 0, _curveDates, 0, _n);

    final double[] times = new double[_n];
    final double[] continuousRates = new double[_n];
    _shiftedTimePoints = new double[_n];

    for (int i = 0; i < _n; i++) {
      // Convert the ZonedDateTimes to doubles
      final double dayCountFraction = dayCount.getDayCountFraction(baseDate, curveDates[i]);
      times[i] = dayCountFraction;
      // Convert the discrete rates to continuous ones
      continuousRates[i] = new PeriodicInterestRate(rates[i], 1).toContinuous().getRate();
      _shiftedTimePoints[i] = dayCountFraction + _offset;
    }

    // Choose interpolation/extrapolation to match the behaviour of curves in the ISDA CDS reference code
    if (_n > 1) {
      _curve = InterpolatedDoublesCurve.fromSorted(times, continuousRates, INTERPOLATOR);
    } else {
      _curve = ConstantDoublesCurve.from(continuousRates[0]);  // Unless the curve is flat, in which case use a constant curve
    }
    _zeroDiscountFactor = Math.exp(_offset * getInterestRate(0.0));
  }

  // ------------------------------------------------------------------------------------------------------------------------------------

  public ISDADateCurve(final String name, final ZonedDateTime[] curveDates, final double[] times, final double[] rates, final double offset) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(curveDates, "curve dates");
    ArgumentChecker.notNull(times, "times");
    ArgumentChecker.notNull(rates, "rates");
    _n = curveDates.length;
    ArgumentChecker.isTrue(_n != 0, "Data arrays were empty");
    ArgumentChecker.isTrue(_n == times.length, "Have {} times for {} dates", times.length, _n);
    ArgumentChecker.isTrue(_n == rates.length, "Have {} rates for {} dates", rates.length, _n);

    _name = name;
    _offset = offset;
    _curveDates = new ZonedDateTime[_n];
    System.arraycopy(curveDates, 0, _curveDates, 0, _n);

    // Choose interpolation/extrapolation to match the behaviour of curves in the ISDA CDS reference code
    if (_n > 1) {
      _curve = InterpolatedDoublesCurve.fromSorted(times, rates, INTERPOLATOR);
    } else {
      _curve = ConstantDoublesCurve.from(rates[0]);  // Unless the curve is flat, in which case use a constant curve
    }

    _shiftedTimePoints = new double[times.length];
    for (int i = 0; i < _n; ++i) {
      _shiftedTimePoints[i] = times[i] + _offset;
    }
    _zeroDiscountFactor = Math.exp(_offset * getInterestRate(0.0));
  }

  // ------------------------------------------------------------------------------------------------------------------------------------

  public ZonedDateTime[] getCurveDates() {
    return _curveDates;
  }

  public String getName() {
    return _name;
  }

  public double getInterestRate(final Double t) {
    return _curve.getYValue(t - _offset);
  }

  public double getTimenode(final int m) {
    return _curve.getXData()[m];
  }

  public double getInterestRate(final int m) {
    return _curve.getYData()[m];
  }

  public double getDiscountFactor(final double t) {
    return Math.exp((_offset - t) * getInterestRate(t)) / _zeroDiscountFactor;
  }

  public double[] getTimePoints() {
    return _shiftedTimePoints;
  }

  public DoublesCurve getCurve() {
    return _curve;
  }

  public double getOffset() {
    return _offset;
  }

  public double getZeroDiscountFactor() {
    return _zeroDiscountFactor;
  }

  public int getNumberOfCurvePoints() {
    return _n;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ISDADateCurve[name=");
    sb.append(_name);
    sb.append(", offset=");
    sb.append(_offset);
    sb.append(", curve dates=");
    sb.append(Arrays.asList(_curveDates));
    sb.append(", shifted time points=");
    sb.append(Arrays.asList(_shiftedTimePoints));
    sb.append(", interpolator=");
    sb.append(INTERPOLATOR);
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_curveDates);
    result = prime * result + _name.hashCode();
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
    if (!(obj instanceof ISDADateCurve)) {
      return false;
    }
    final ISDADateCurve other = (ISDADateCurve) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
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
