/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.ContinouslyCompoundedYieldCalculator;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;

/**
 *
 */
public class EffectiveConvexityCalculator {
  private final ContinouslyCompoundedYieldCalculator _yield = new ContinouslyCompoundedYieldCalculator();

  public double calculate(FixedAnnuity annuity, final double price) {
    Validate.notNull(annuity, "");
    Validate.notNull(annuity, "annuity");
    if (price <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    double yield = _yield.calculate(annuity, price);
    double sum = 0.0;
    double[] c = annuity.getPaymentAmounts();
    double[] t = annuity.getPaymentTimes();
    for (int i = 0; i < annuity.getNumberOfPayments(); i++) {
      sum += t[i] * t[i] * c[i] * Math.exp(-yield * t[i]);
    }
    return sum / price;
  }

}
