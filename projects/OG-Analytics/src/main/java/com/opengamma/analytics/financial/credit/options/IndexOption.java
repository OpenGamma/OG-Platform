/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;

/**
 * 
 */
public class IndexOption {

  private final PortfolioSwapAdjustment _portfolioAdjustment = new PortfolioSwapAdjustment();

  public double price(final CDSAnalytic cdsForward, final CDSAnalytic futureCDS, final int indexSize, final double indexCoupon, final ISDACompliantYieldCurve yieldCurve,
      final ISDACompliantCreditCurve[] creditCurves, final double[] recoveryRate, final double timeToExpiry, final double defaultedValue) {
    final double expectedH = expectedDefaultValue(indexSize, creditCurves, recoveryRate, timeToExpiry) +
        _portfolioAdjustment.indexPV(cdsForward, indexCoupon, indexSize, yieldCurve, creditCurves, recoveryRate, PriceType.CLEAN);

    return 0.0;
  }

  public double expectedDefaultValue(final int indexSize, final ISDACompliantCreditCurve[] creditCurves, final double[] recoveryRate, final double timeToExpiry) {
    final int n = creditCurves.length;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += (1 - recoveryRate[i]) * (1 - creditCurves[i].getSurvivalProbability(timeToExpiry));
    }
    return sum / indexSize;
  }

}
