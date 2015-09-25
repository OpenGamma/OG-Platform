/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.ContinuousInterestRate;
import com.opengamma.analytics.financial.model.interestrate.InterestRateModel;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.special.TopHatFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * A curve that provides interest rate (continuously compounded) discount factor.
 * <p>The relation between the rate <i>r(t)</i> at the maturity <i>t</i> and the discount factor
 * <i>df(t)</i> is <i>df(t)=e<sup>-r(t)t</sup></i>.
 */
public abstract class YieldAndDiscountCurve implements InterestRateModel<Double>, Serializable {

  /**
   * The curve name.
   */
  private final String _name;

  /**
   * Constructor.
   * @param name The curve name.
   */
  public YieldAndDiscountCurve(final String name) {
    ArgumentChecker.notNull(name, "Name");
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
   * Returns the discount factor at a given time.
   * @param t The time
   * @return The discount factor for time to maturity <i>t</i>.
   */
  public double getDiscountFactor(final double t) {
    if (t == 0) { //short cut rate lookup
      return 1.0;
    }
    return Math.exp(-t * getInterestRate(t));
  }

  /**
   * Gets the forward rate at a given time.
   * @param t The time
   * @return The forward rate
   */
  public abstract double getForwardRate(final double t);

  /**
   * Returns the interest rate in a given compounding per year at a given time.
   * @param t The time.
   * @param compoundingPeriodsPerYear The number of composition per year.
   * @return The rate in the requested composition.
   */
  public double getPeriodicInterestRate(final double t, final int compoundingPeriodsPerYear) {
    final double rcc = getInterestRate(t);
    final ContinuousInterestRate cont = new ContinuousInterestRate(rcc);
    return cont.toPeriodic(compoundingPeriodsPerYear).getRate();
  }

  /**
   * Returns the sensitivity (derivative) of the continuously compounded interest rate at a given time with respect 
   * to the parameters defining the curve.
   * @param time The time.
   * @return The sensitivity.
   */
  public abstract double[] getInterestRateParameterSensitivity(final double time);

  /**
   * Return the number of parameters for the definition of the curve.
   * @return The number of parameters.
   */
  public abstract int getNumberOfParameters();

  /**
   * The list of underlying curves one level down. This is intended for curve that are build from other curves to avoid
   * a double counting of sensitivities. For standard curves, this list will be empty. Only when the curve is build 
   * with several layers will this method return a non-empty list.
   * @return The list.
   */
  public abstract List<String> getUnderlyingCurvesNames();

  /**
   * Return the number of intrinsic parameters for the definition of the curve. Which is the total number of 
   * parameters minus the parameters of the curves in curvesNames (If they are in curves).
   *  @param curvesNames The list of curves names.
   *  @return The number of parameters.
   */
  public int getNumberOfIntrinsicParameters(final Set<String> curvesNames) {
    return getNumberOfParameters();
  }

  /**
   * Create another {@link YieldAndDiscountCurve} with the zero-coupon rates shifted by a given amount.
   * @param shift The shift amount.
   * @return The new curve.
   * @deprecated Use {@link YieldCurveUtils#withParallelShift}
   */
  @Deprecated
  public YieldAndDiscountCurve withParallelShift(final double shift) {
    return new YieldAndDiscountAddZeroSpreadCurve(_name + "WithParallelShift", false, this, 
        YieldCurve.from(ConstantDoublesCurve.from(shift)));
  }

  /**
   * Create another {@link YieldAndDiscountCurve} with the zero-coupon rates shifted by a given amount at a given time.
   * The shift is done around the given time within the default range 1.0E-3.
   * @param t The time.
   * @param shift The shift amount.
   * @return The new curve.
   */
  public YieldAndDiscountCurve withSingleShift(final double t, final double shift) {
    final double defaultRange = 1.0E-3; // 1 day ~ 3E-3
    return new YieldAndDiscountAddZeroSpreadCurve(_name + "WithSingleShift", false, this,
        YieldCurve.from(new FunctionalDoublesCurve(new TopHatFunction(t - defaultRange, t + defaultRange, shift))));
  }

}
