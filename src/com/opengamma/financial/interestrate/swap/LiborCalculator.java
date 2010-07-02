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
public class LiborCalculator {

  public double[] getLiborRate(final YieldAndDiscountCurve forwardCurve, final Swap swap) {
    Validate.notNull(forwardCurve);
    Validate.notNull(swap);
    final int nFloat = swap.getNumberOfFloatingPayments();
    final double[] floatPaymentTimes = swap.getFloatingPaymentTimes();
    final double[] deltaStart = swap.getDeltaStart();
    final double[] deltaEnd = swap.getDeltaEnd();
    final double[] liborYearFractions = swap.getReferenceYearFractions();
    final double[] libors = new double[nFloat];
    double ta, tb;
    for (int i = 0; i < nFloat; i++) {
      ta = (i == 0 ? 0.0 : floatPaymentTimes[i - 1]) + deltaStart[i];
      tb = floatPaymentTimes[i] + deltaEnd[i];
      libors[i] = (forwardCurve.getDiscountFactor(ta) / forwardCurve.getDiscountFactor(tb) - 1.0) / liborYearFractions[i];
    }
    return libors;
  }
}
