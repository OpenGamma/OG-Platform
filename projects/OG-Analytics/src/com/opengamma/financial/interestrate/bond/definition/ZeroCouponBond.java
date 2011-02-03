/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

/**
 * 
 */
public class ZeroCouponBond extends Bond {

  public ZeroCouponBond(final double paymentTime, final String yieldCurveName) {
    super(new double[] {paymentTime}, 0, yieldCurveName);
  }

}
