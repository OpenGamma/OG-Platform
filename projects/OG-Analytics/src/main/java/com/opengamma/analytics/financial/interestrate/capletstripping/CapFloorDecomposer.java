/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.util.ArgumentChecker;

/**
 * Decomposes a {@link CapFloor} into an array of {@link SimpleOptionData}
 */
public final class CapFloorDecomposer {
  private static final ParRateDiscountingCalculator PRC = ParRateDiscountingCalculator.getInstance();

  /**
   * Private constructor
   */
  private CapFloorDecomposer() {
  }

  /**
   * Express a cap or floor as a strip of European call or put options
   * @param cap The cap or floor
   * @param ycb yield curves (i.e. discount and Ibor-projection curves)
   * @return strip of European call or put options
   */
  public static SimpleOptionData[] toOptions(final CapFloor cap, final MulticurveProviderDiscount ycb) {
    ArgumentChecker.notNull(cap, "null cap");
    return toOptions(cap.getPayments(), ycb);
  }

  public static SimpleOptionData[] toOptions(final CapFloorIbor[] caplets, final MulticurveProviderDiscount ycb) {
    ArgumentChecker.noNulls(caplets, "null caplets");
    ArgumentChecker.notNull(ycb, "null yield curves");
    final int n = caplets.length;

    final SimpleOptionData[] options = new SimpleOptionData[n];
    for (int i = 0; i < n; i++) {
      final YieldAndDiscountCurve discountCurve = ycb.getCurve(caplets[i].getCurrency());

      final double fwd = caplets[i].accept(PRC, ycb);
      final double t = caplets[i].getFixingTime();
      // Vol is at fixing time, discounting from payment. This included the year fraction
      final double df = discountCurve.getDiscountFactor(caplets[i].getPaymentTime()) * caplets[i].getPaymentYearFraction();
      options[i] = new SimpleOptionData(fwd, caplets[i].getStrike(), t, df, caplets[i].isCap());
    }
    return options;
  }

}
