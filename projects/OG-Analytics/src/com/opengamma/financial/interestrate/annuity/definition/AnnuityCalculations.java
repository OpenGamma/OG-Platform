/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public abstract class AnnuityCalculations {

  public static double[] getLiborRates(final VariableAnnuity annuity, final YieldCurveBundle curves) {

    final YieldAndDiscountCurve curve = curves.getCurve(annuity.getLiborCurveName());
    final int n = annuity.getNumberOfPayments();
    final double[] indexFixing = annuity.getIndexFixingTimes();
    final double[] indexMaturity = annuity.getIndexMaturityTimes();
    final double[] alpha = annuity.getYearFractions();
    final double[] libors = new double[n];
    if (indexFixing[0] < 0.0) {
      libors[0] = annuity.getInitialRate();
    } else {
      libors[0] = (curve.getDiscountFactor(indexFixing[0]) / curve.getDiscountFactor(indexMaturity[0]) - 1.0) / alpha[0];
    }
    for (int i = 1; i < n; i++) {
      libors[i] = (curve.getDiscountFactor(indexFixing[i]) / curve.getDiscountFactor(indexMaturity[i]) - 1.0) / alpha[i];
    }
    return libors;
  }

}
