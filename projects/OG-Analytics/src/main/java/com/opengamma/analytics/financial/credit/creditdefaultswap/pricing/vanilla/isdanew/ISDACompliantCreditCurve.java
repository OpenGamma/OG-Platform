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

  public ISDACompliantCreditCurve(final double[] t, final double[] r) {
    super(t, r);
  }

  public ISDACompliantCreditCurve(final ISDACompliantCurve from) {
    super(from);
  }

  public double getHazardRate(double t) {
    return getZeroRate(t);
  }

  public double getSurvivalProbability(double t) {
    return getDiscountFactor(t);
  }

  @Override
  public ISDACompliantCreditCurve withRates(final double[] r) {
    return new ISDACompliantCreditCurve(super.withRates(r));
  }

  @Override
  public ISDACompliantCreditCurve withRate(final double rate, final int index) {
    return new ISDACompliantCreditCurve(super.withRate(rate, index));
  }

}
