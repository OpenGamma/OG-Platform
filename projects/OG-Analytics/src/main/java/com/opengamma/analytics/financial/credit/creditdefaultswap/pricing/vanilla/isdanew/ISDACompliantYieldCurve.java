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

  public ISDACompliantYieldCurve(final double[] t, final double[] r) {
    super(t, r);
  }

  public ISDACompliantYieldCurve(final ISDACompliantCurve from) {
    super(from);
  }

  @Override
  public ISDACompliantYieldCurve withRates(final double[] r) {
    return new ISDACompliantYieldCurve(super.withRates(r));
  }

  @Override
  public ISDACompliantYieldCurve withRate(final double rate, final int index) {
    return new ISDACompliantYieldCurve(super.withRate(rate, index));
  }

}
