/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.model.interestrate.InterestRateModel;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.special.TopHatFunction;

/**
 * A curve that provides interest rate (continuously compounded) discount factor. 
 * <p>The relation between the rate <i>r(t)</i> at the maturity <i>t</i> and the discount factor <i>df(t)</i> is <i>df(t)=e<sup>-r(t)t</sup></i>.
 */

public abstract class YieldAndDiscountCurve implements InterestRateModel<Double> {

  private final String _name;

  /**
   * Constructor.
   * @param name The curve name.
   */
  public YieldAndDiscountCurve(String name) {
    _name = name;
  }

  /**
   * Returns the curve name.
   * @return The name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Returns the interest rate (zero-coupon continuously-compounded) at a given time.
   * @param t The time 
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   *           TODO: Review if the exception is the one required. Currently it is not implemented.
   */
  @Override
  public double getInterestRate(final Double t) {
    return -Math.log(getDiscountFactor(t)) / t;
  }

  /**
   * Returns the discount factor at a given time.
   * @param t The time 
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  public double getDiscountFactor(final Double t) {
    return Math.exp(-t * getInterestRate(t));
  }

  /**
   * Returns the interest rate in a given compounding per year at a given time.
   * @param t The time.
   * @param compoundingPeriodsPerYear The number of composition per year.
   * @return The rate in the requested composition.
   */
  public double getPeriodicInterestRate(final Double t, final int compoundingPeriodsPerYear) {
    final double rcc = getInterestRate(t);
    final ContinuousInterestRate cont = new ContinuousInterestRate(rcc);
    return cont.toPeriodic(compoundingPeriodsPerYear).getRate();
  }

  //  /**
  //   * Gets the underlying curve. 
  //   * TODO: do we want to return the underlying curve even if a priori we don't know what it contains as information?
  //   * @return The curve.
  //   */
  //  public Curve<Double, Double> getCurve() {
  //    return _curve;
  //  }

  /**
   * Create another YieldAndDiscountCurve with the zero-coupon rates shifted by a given amount.
   * @param shift The shift amount.
   * @return The new curve.
   */
  public YieldAndDiscountCurve withParallelShift(final double shift) {
    return new YieldAndDiscountAddZeroSpreadCurve(false, this, new YieldCurve(ConstantDoublesCurve.from(shift)));
  }

  /**
   * Create another YieldAndDiscountCurve with the zero-coupon rates shifted by a given amount at a given time.
   * The shift is done around the given time within the default range 1.0E-3.
   * @param t The time.
   * @param shift The shift amount.
   * @return The new curve.
   */
  public YieldAndDiscountCurve withSingleShift(final double t, final double shift) {
    double defaultRange = 1.0E-3; // 1 day ~ 3E-3
    return new YieldAndDiscountAddZeroSpreadCurve(false, this, new YieldCurve(new FunctionalDoublesCurve(new TopHatFunction(t - defaultRange, t + defaultRange, shift))));
  }
  //
  //  public YieldAndDiscountCurve withMultipleShifts(final double[] xShifts, final double[] yShifts) {
  //    return new YieldCurve(CurveShiftFunctionFactory.getShiftedCurve(_curve, xShifts, yShifts));
  //  }

}
