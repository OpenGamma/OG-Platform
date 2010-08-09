/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.interestrate.annuity.definition.ContinouslyCompoundedYieldCalculator;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;

/**
 * 
 */
public class EffectiveDurationCalculator {
  private final ContinouslyCompoundedYieldCalculator _yield = new ContinouslyCompoundedYieldCalculator();
  private final PV01Calculator _pv01 = new PV01Calculator();

  public double calculate(final FixedAnnuity annuity, final double price) {
    Validate.notNull(annuity, "annuity");
    if (price <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    double yield = _yield.calculate(annuity, price);
    double[] c = annuity.getPaymentAmounts();
    double[] t = annuity.getPaymentTimes();
    double sum = 0.0;
    for (int i = 0; i < annuity.getNumberOfPayments(); i++) {
      sum += t[i] * c[i] * Math.exp(-yield * t[i]);
    }
    return sum / price;
  }
}
