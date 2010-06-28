/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class AnnuityCalculator {

  public double getAnnuity(YieldAndDiscountCurve fundingCurve, Swap swap) {
    double fixed = 0.0;
    double[] fixedYearFractions = swap.getFixedYearFractions();
    double[] fixedPaymentTimes = swap.getFixedPaymentTimes();
    for (int i = 0; i < swap.getNumberOfFixedPayments(); i++) {
      fixed += fixedYearFractions[i] * fundingCurve.getDiscountFactor(fixedPaymentTimes[i]);
    }
    return fixed;
  }
}
