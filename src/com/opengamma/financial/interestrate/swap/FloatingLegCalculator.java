/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class FloatingLegCalculator {
  private final LiborCalculator _liborCalculator = new LiborCalculator();

  public double getFloatLeg(final YieldAndDiscountCurve forwardCurve, final YieldAndDiscountCurve fundingCurve, final Swap swap) {
    Validate.notNull(forwardCurve);
    Validate.notNull(fundingCurve);
    Validate.notNull(swap);
    double floating = 0.0;
    final double[] libors = _liborCalculator.getLiborRate(forwardCurve, swap);
    final int nFloat = swap.getNumberOfFloatingPayments();
    final double[] floatYearFractions = swap.getFloatingYearFractions();
    final double[] floatPaymentTimes = swap.getFloatingPaymentTimes();
    for (int i = 0; i < nFloat; i++) {
      floating += floatYearFractions[i] * libors[i] * fundingCurve.getDiscountFactor(floatPaymentTimes[i]);
    }
    return floating;
  }

}
