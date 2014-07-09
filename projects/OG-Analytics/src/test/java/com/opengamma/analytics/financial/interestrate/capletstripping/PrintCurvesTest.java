/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class PrintCurvesTest extends CapletStrippingSetup {

  @Test
  //  (enabled = false)
  public void printCurves() {
    final MulticurveProviderDiscount yc = getYieldCurves();
    final YieldAndDiscountCurve discountCurve = yc.getDiscountingCurves().get(Currency.USD);
    final YieldAndDiscountCurve indexCurve = yc.getCurve(getIbor());
    final int nSamples = 200;
    for (int i = 0; i < nSamples; i++) {
      final double t = 0.05 + i / (nSamples - 1.0) * 9.95;
      final double r1 = discountCurve.getInterestRate(t);
      final double r2 = indexCurve.getInterestRate(t);
      System.out.println(t + "\t" + r1 + "\t" + r2);
    }
  }
}
