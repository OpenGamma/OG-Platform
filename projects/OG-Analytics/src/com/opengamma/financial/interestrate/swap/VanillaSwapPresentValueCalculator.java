/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class VanillaSwapPresentValueCalculator {

  public double getPresentValue(final double[] fixedPaymentTimes, final double[] fixedPayment, final double floatPaymentTime, final double floatPayment, final YieldAndDiscountCurve fundingCurve) {
    Validate.notNull(fixedPaymentTimes);
    Validate.notNull(fixedPayment);
    Validate.notNull(fundingCurve);
    if (fixedPaymentTimes.length != fixedPayment.length) {
      throw new IllegalArgumentException("Must have the same number of fixed payment times as payments");
    }
    double presentValue = 0;
    final int n = fixedPaymentTimes.length;
    for (int i = 0; i < n; i++) {
      presentValue += fixedPayment[i] * fundingCurve.getDiscountFactor(fixedPaymentTimes[i]);
    }
    presentValue += floatPayment * fundingCurve.getDiscountFactor(floatPaymentTime);
    return presentValue;
  }
}
