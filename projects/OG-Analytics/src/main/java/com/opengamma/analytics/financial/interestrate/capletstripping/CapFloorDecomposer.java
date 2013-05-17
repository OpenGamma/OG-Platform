/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;

/**
 * 
 */
public abstract class CapFloorDecomposer {
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();

  /**
   * Express a cap or floor as a strip of European call or put options 
   * @param cap The cap or floor
   * @param ycb yield curves (i.e. discount and Ibor-projection curves)
   * @return strip of European call or put options
   */
  public static SimpleOptionData[] toOptions(final CapFloor cap, final YieldCurveBundle ycb) {
    Validate.notNull(cap, "null cap"); 
    return toOptions(cap.getPayments(), ycb);
  }

  public static SimpleOptionData[] toOptions(final CapFloorIbor[] caplets, final YieldCurveBundle ycb) {
    Validate.noNullElements(caplets, "null caplets");
    Validate.notNull(ycb, "null yield curves");
    final int n = caplets.length;

    SimpleOptionData[] options = new SimpleOptionData[n];
    for (int i = 0; i < n; i++) {
      final YieldAndDiscountCurve discountCurve = ycb.getCurve(caplets[i].getFundingCurveName());
      double fwd = caplets[i].accept(PRC, ycb);
      double t = caplets[i].getFixingTime();
      // Vol is at fixing time, discounting from payment. This included the year fraction
      double df = discountCurve.getDiscountFactor(caplets[i].getPaymentTime()) * caplets[i].getPaymentYearFraction();
      options[i] = new SimpleOptionData(fwd, caplets[i].getStrike(), t, df, caplets[i].isCap());
    }
    return options;
  }

}
