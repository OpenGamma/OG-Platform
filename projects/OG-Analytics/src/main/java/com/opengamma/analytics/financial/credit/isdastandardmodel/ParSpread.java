/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

/**
 * Par spread is the old (i.e. pre-April 2009) way of quoting CDSs. A CDS would be constructed to have an initial 
 * fair value of zero; the par-spread is the value of the coupon (premium) on the premium leg that makes this so. <br>
 * A zero hazard curve (or equivalent, e.g. the survival probability curve) can be implied from a set of par spread quotes
 * (on the same name at different maturities) by finding the curve that gives all the CDSs a PV of zero  (the curve is not
 * unique and will depend on other modeling choices). 
 */
public class ParSpread implements CDSQuoteConvention {

  private final double _parSpread;

  public ParSpread(final double parSpread) {
    _parSpread = parSpread;
  }

  @Override
  public double getCoupon() {
    return _parSpread;
  }

}
