/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public abstract class AnnuityCalculations {

  public static double[] getLiborRates(final ForwardLiborAnnuity annuity, final YieldCurveBundle curves) {

    int n = annuity.getNumberOfPayments();
    double[] libors = new double[n];

    for (int i = 0; i < n; i++) {
      ForwardLiborPayment payment = annuity.getNthPayment(i);
      YieldAndDiscountCurve curve = curves.getCurve(payment.getLiborCurveName());
      libors[i] = (curve.getDiscountFactor(payment.getLiborFixingTime()) / curve.getDiscountFactor(payment.getLiborMaturityTime()) - 1.0) / payment.getForwardYearFraction();
    }
    return libors;
  }

}
