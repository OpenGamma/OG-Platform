/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

/**
 * Quoted spread (sometimes misleadingly called flat spread) is an alternative to quoting PUF where people wish to see a
 * spread like number. It is numerical close in value to the equivalent par spread but is <b>absolutely not the same thing</b>.
 * To find the quoted spread of a CDS from its PUF (and premium) one first finds the unique flat hazard rate that will give
 * the CDS a clean present value equal to its PUF*Notional; one then finds the par spread (the coupon that makes the CDS have 
 * zero clean PV) of the CDS from this <b>flat hazard</b> curve - this is the quoted spread (and the reason for the confusing
 * name, flat spread).<br>
 * To go from a quoted spread to PUF, one does the reverse of the above.<br>
 * A zero hazard curve (or equivalent, e.g. the survival probability curve) cannot be directly implied from a set of quoted
 * spreads - one must first convert to PUF.
 */
public class QuotedSpread implements CDSQuoteConvention {

  private final double _coupon;
  private final double _quotedSpread;

  public QuotedSpread(final double coupon, final double quotedSpread) {
    _coupon = coupon;
    _quotedSpread = quotedSpread;
  }

  @Override
  public double getCoupon() {
    return _coupon;
  }

  public double getQuotedSpread() {
    return _quotedSpread;
  }

}
