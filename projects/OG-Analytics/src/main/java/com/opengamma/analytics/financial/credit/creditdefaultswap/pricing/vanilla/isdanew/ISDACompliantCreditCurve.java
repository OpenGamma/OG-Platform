/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

/**
 *
 */
public class ISDACompliantCreditCurve extends ISDACompliantCurve {

  /**
   * Flat credit (hazard) curve at hazard rate h
   * @param t (arbitrary) single knot point (t > 0)
   * @param h the level
   */
  public ISDACompliantCreditCurve(final double t, final double h) {
    super(t, h);
  }

  /**
   * credit (hazard) curve with knots at times, t, zero hazard rates, h, at the knots and piecewise constant
   * forward hazard rates between knots (i.e. linear interpolation of h*t or the -log(survival-probability)
   * @param t knot (node) times
   * @param h zero hazard rates
   */
  public ISDACompliantCreditCurve(final double[] t, final double[] h) {
    super(t, h);
  }

  /**
   * Copy constructor - can be used to down cast from ISDACompliantCurve
   * @param from a ISDACompliantCurve
   */
  public ISDACompliantCreditCurve(final ISDACompliantCurve from) {
    super(from);
  }

  /**
   * @param t Set of times that form the knots of the curve. Must be ascending with the first value >= 0.
   * @param r Set of zero rates
   * @param rt Set of rates at the knot times
   * @param df Set of discount factors at the knot times
   * @param offsetTime The offset to the base date
   * @param offsetRT The offset rate
   * @deprecated This constructor is deprecated
   */
  @Deprecated
  public ISDACompliantCreditCurve(final double[] t, final double[] r, final double[] rt, final double[] df,  final double offsetTime, final double offsetRT) {
    super(t, r, rt, df, offsetTime, offsetRT);
  }

  /**
   * Get the zero hazard rate at time t (note: this simply a pseudonym for getZeroRate)
   * @param t time
   * @return zero hazard rate at time t
   */
  public double getHazardRate(final double t) {
    return getZeroRate(t);
  }

  /**
   * Get the survival probability at time t (note: this simply a pseudonym for getDiscountFactor)
   * @param t time
   * @return survival probability at time t
   */
  public double getSurvivalProbability(final double t) {
    return getDiscountFactor(t);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve withRates(final double[] r) {
    return new ISDACompliantCreditCurve(super.withRates(r));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ISDACompliantCreditCurve withRate(final double rate, final int index) {
    return new ISDACompliantCreditCurve(super.withRate(rate, index));
  }

}
