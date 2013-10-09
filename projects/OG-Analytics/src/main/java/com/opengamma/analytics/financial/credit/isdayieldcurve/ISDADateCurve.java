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
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.PeriodicInterestRate;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 *The underlying curve is the zero default rate to time t, H(t), defined as H(t) = -ln[Q(0,t)]/t, where Q(0,t) is the survival probably from now (time 0)
 *to time t. This currently extends YieldAndDiscountCurve and thus uses the nomenclature of the rates world. The links are
 *<ul>
 *<li>Discount factor P(0,t) <==> survival probability Q(0,t) </li>
 *<li>Zero rate (or yield) R(t) = -ln[P(0,t)]/t <==> Zero default rate H(t) = -ln[Q(0,t)]/t</li>
 *</ul>
 *The underlying curve is a linear interpolation of the quantity t*H(t) = -ln[Q(0,t)], which is the ISDA model standard 
 */
public class ISDADateCurve extends YieldAndDiscountCurve {

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  @SuppressWarnings("unused")
  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("ACT/360");

  // ------------------------------------------------------------------------------------------------------------------------------------

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(ISDA_INTERPOLATOR, FLAT_EXTRAPOLATOR, ISDA_EXTRAPOLATOR);

  private final String _name;

  private final ZonedDateTime _baseDates; // RW added to aid conversion

  // TODO what is this?
  private final double _offset;
  private final ZonedDateTime[] _curveDates;
  private final DoublesCurve _curve;
  private final double[] _shiftedTimePoints;
  private final double _zeroDiscountFactor;
  private final int _n;

  // ------------------------------------------------------------------------------------------------------------------------------------

  /**
   * A ISDA model zero default curve with ACT/365 day-count convention. This can take in the output from the native ISDA yield curve construction model  
   * @param name The curve name
   * @param baseDate base date to convert other (future) dates into year fractions using the day-count convention  
   * @param curveDates The dates of points on the curve 
   * @param rates The zero default rates at points on the curve. <b>Note:</b>These as annually compounded rates   
   * @param offset TODO find out what this does 
   */
  public ISDADateCurve(final String name, final ZonedDateTime baseDate, final ZonedDateTime[] curveDates, final double[] rates, final double offset) {
    this(name, baseDate, curveDates, rates, offset, ACT_365);
  }

  /**
   * A ISDA model zero default curve 
   * @param name The curve name
   * @param baseDate base date to convert other (future) dates into year fractions using the day-count convention  
   * @param curveDates The dates of points on the curve 
   * @param rates The zero default rates at points on the curve. <b>Note:</b>These as annually compounded rates   
   * @param offset TODO find out what this does 
   * @param dayCount The day-count convention 
   */
  public ISDADateCurve(final String name, final ZonedDateTime baseDate, final ZonedDateTime[] curveDates, final double[] rates, final double offset, final DayCount dayCount) {
    super(name);
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(baseDate, "base date");
    ArgumentChecker.noNulls(curveDates, "curve dates");
    ArgumentChecker.notEmpty(rates, "rates");
    ArgumentChecker.notNull(dayCount, "day count");
    _n = curveDates.length;
    ArgumentChecker.isTrue(_n != 0, "Data arrays were empty");
    // TODO why is this test commented out?
    // ArgumentChecker.isTrue(_n == rates.length, "Have {} rates for {} dates", rates.length, _n);

    _baseDates = baseDate;

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

    // Choose interpolation/extrapolation to match the behavior of curves in the ISDA CDS reference code
    if (_n > 1) {
      _curve = InterpolatedDoublesCurve.fromSorted(times, continuousRates, INTERPOLATOR);
    } else {
      _curve = ConstantDoublesCurve.from(continuousRates[0]); // Unless the curve is flat, in which case use a constant curve
    }
    _zeroDiscountFactor = Math.exp(_offset * getInterestRate(0.0));
  }

  /**
   * A ISDA model zero default curve 
   * @param name The curve name
   * @param curveDates The dates of points on the curve 
   * @param times Time in years to points on the curve (these should have been calculated from a base date using some day-count convention)
   * @param rates The zero default rates at points on the curve. <b>Note:</b>These are continually compounded rates, while the other constructors take
   * annually compounded rates    
   * @param offset TODO find out what this does 
   */
  public ISDADateCurve(final String name, final ZonedDateTime[] curveDates, final double[] times, final double[] rates, final double offset) {
    super(name);
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.noNulls(curveDates, "curve dates");
    ArgumentChecker.notEmpty(times, "times");
    ArgumentChecker.notEmpty(rates, "rates");
    _n = curveDates.length;
    ArgumentChecker.isTrue(_n != 0, "Data arrays were empty");
    // TODO why commented out?
    // ArgumentChecker.isTrue(_n == times.length, "Have {} times for {} dates", times.length, _n);
    // ArgumentChecker.isTrue(_n == rates.length, "Have {} rates for {} dates", rates.length, _n);

    _baseDates = null;

    _name = name;
    _offset = offset;
    _curveDates = new ZonedDateTime[_n];
    System.arraycopy(curveDates, 0, _curveDates, 0, _n);

    // Choose interpolation/extrapolation to match the behavior of curves in the ISDA CDS reference code
    if (_n > 1) {
      _curve = InterpolatedDoublesCurve.fromSorted(times, rates, INTERPOLATOR);
    } else {
      _curve = ConstantDoublesCurve.from(rates[0]); // Unless the curve is flat, in which case use a constant curve
    }

    _shiftedTimePoints = new double[times.length];
    for (int i = 0; i < times.length; ++i) {
      _shiftedTimePoints[i] = times[i] + _offset;
    }
    _zeroDiscountFactor = Math.exp(_offset * getInterestRate(0.0));
  }

  // ------------------------------------------------------------------------------------------------------------------------------------

  /**
   * 
   * @return The dates of points on the curve 
   */
  public ZonedDateTime[] getCurveDates() {
    return _curveDates;
  }

  public ZonedDateTime getBaseDate() {
    return _baseDates;
  }

  @Override
  public String getName() {
    return _name;
  }

  /**
   * The zero default rate to time t
   * @param t time in years
   * @return The zero default rate as a fraction
   */
  @Override
  public double getInterestRate(final Double t) {
    return _curve.getYValue(t - _offset);
  }

  /**
   * Get the time (in years) of point m on the curve (indexed from zero)
   * @param m the index 
   * @return the time (in years)
   */
  public double getTimenode(final int m) {
    return _curve.getXData()[m];
  }

  /**
   * Get the zero default rate of point m on the curve (indexed from zero)
   * @param m the index 
   * @return The zero default rate
   */
  public double getInterestRate(final int m) {
    return _curve.getYData()[m];
  }

  /**
   * The survival probability
   * @param t time in years
   * @return The survival probability to time t
   */
  @Override
  public double getDiscountFactor(final double t) {
    return Math.exp((_offset - t) * getInterestRate(t)) / _zeroDiscountFactor;
  }

  /**
   * get the shifted time points 
   * @return The shifted time points 
   */
  public double[] getTimePoints() {
    return _shiftedTimePoints;
  }

  /**
   * 
   * @return The underlying curve 
   */
  public DoublesCurve getCurve() {
    return _curve;
  }

  /**
   * 
   * @return The offset
   */
  public double getOffset() {
    return _offset;
  }

  /**
   * This is 1.0 unless an offset is used, in which case it is Math.exp(offset * H(-offset))
   * @return Zero Discount Factor
   */
  public double getZeroDiscountFactor() {
    return _zeroDiscountFactor;
  }

  /**
   * 
   * @return Number of points on curve 
   */
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

  @Override
  public double[] getInterestRateParameterSensitivity(final double time) {
    throw new NotImplementedException();
  }

  @Override
  public int getNumberOfParameters() {
    throw new NotImplementedException();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    throw new NotImplementedException();
  }

  @Override
  public double getForwardRate(final double t) {
    throw new NotImplementedException();
  }

}
