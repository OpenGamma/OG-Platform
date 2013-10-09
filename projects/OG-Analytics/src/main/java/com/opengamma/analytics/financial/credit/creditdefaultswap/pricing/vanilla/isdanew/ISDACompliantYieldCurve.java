/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

/**
 *
 */
public class ISDACompliantYieldCurve extends ISDACompliantCurve {

  /**
   * Flat yield curve at level r
   * @param t (arbitrary) single knot point (t > 0)
   * @param r the level
   */
  public ISDACompliantYieldCurve(final double t, final double r) {
    super(t, r);
  }

  /**
   * yield (discount) curve with knots at times, t, zero rates, r, at the knots and piecewise constant
   * forward  rates between knots (i.e. linear interpolation of r*t or the -log(discountFactor)
   * @param t Set of times that form the knots of the curve. Must be ascending with the first value >= 0.
   * @param r Set of zero rates
   */
  public ISDACompliantYieldCurve(final double[] t, final double[] r) {
    super(t, r);
  }

  /**
   * A curve in which the knots are measured (in fractions of a year) from a particular base-date but the curve is 'observed'
   * from a different base-date. As an example<br>
   * Today (the observation point) is 11-Jul-13, but the yield curve is snapped (bootstrapped from money market and swap rates)
   * on 10-Jul-13 - seen from today there is an offset of -1/365 (assuming a day count of ACT/365) that must be applied to use
   * the yield curve today.  <br>
   * In general, a discount curve observed at time $t_1$ can be written as $P(t_1,T)$. Observed from time $t_2$ this is
   * $P(t_2,T) = \frac{P(t_1,T)}{P(t_1,t_2)}$
   * @param timesFromBaseDate times measured from the base date of the curve
   * @param r zero rates
   * @param offsetFromNewBaseDate if this curve is to be used from a new base-date, what is the offset from the curve base
   */
  ISDACompliantYieldCurve(final double[] timesFromBaseDate, final double[] r, final double offsetFromNewBaseDate) {
    super(timesFromBaseDate, r, offsetFromNewBaseDate);
  }

  /**
   * Copy constructor - can be used to down cast from ISDACompliantCurve
   * @param from a ISDACompliantCurve
   */
  public ISDACompliantYieldCurve(final ISDACompliantCurve from) {
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
  public ISDACompliantYieldCurve(final double[] t, final double[] r, final double[] rt, final double[] df, final double offsetTime, final double offsetRT) {
    super(t, r, rt, df, offsetTime, offsetRT);
  }

  /**
   * {@inheritDoc}
    */
  @Override
  public ISDACompliantYieldCurve withOffset(final double offsetFromNewBaseDate) {
    return new ISDACompliantYieldCurve(super.withOffset(offsetFromNewBaseDate));
  }

  /**
   * {@inheritDoc}
    */
  @Override
  public ISDACompliantYieldCurve withRates(final double[] r) {
    return new ISDACompliantYieldCurve(super.withRates(r));
  }

  /**
   * {@inheritDoc}
    */
  @Override
  public ISDACompliantYieldCurve withRate(final double rate, final int index) {
    return new ISDACompliantYieldCurve(super.withRate(rate, index));
  }

}
