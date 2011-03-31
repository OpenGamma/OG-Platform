/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ZeroCouponBond extends Bond {

  public ZeroCouponBond(Currency currency, final double paymentTime, final String yieldCurveName) {
    super(currency, new double[] {paymentTime}, 0, yieldCurveName);
  }

}
