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
public class FloatingLegCalculator {
  private final LiborCalculator _liborCalculator = new LiborCalculator();

  public double getFloatLeg(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve, Swap swap) {
    double floating = 0.0;
    final double[] libors = _liborCalculator.getLiborRate(forwardCurve, swap);
    int nFloat = swap.getNumberOfFloatingPayments();
    double[] floatYearFractions = swap.getFloatingYearFractions();
    double[] floatPaymentTimes = swap.getFloatingPaymentTimes();
    for (int i = 0; i < nFloat; i++) {
      floating += floatYearFractions[i] * libors[i] * fundingCurve.getDiscountFactor(floatPaymentTimes[i]);
    }
    return floating;
  }

}
