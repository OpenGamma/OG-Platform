/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.model.volatility.SimpleOptionData;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
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
   * @param curves yield curves (i.e. discount and Ibor-projection curves)
   * @return strip of European call or put options
   */
  public static SimpleOptionData[] toOptions(final CapFloor cap, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(cap, "null curve");
    return toOptions(cap.getPayments(), curves);
  }

  public static SimpleOptionData[] toOptions(final CapFloorIbor[] caplets, final MulticurveProviderInterface curves) {
    ArgumentChecker.noNulls(caplets, "null caplets");
    ArgumentChecker.notNull(curves, "null yield curves");
    final int n = caplets.length;

    final SimpleOptionData[] options = new SimpleOptionData[n];
    for (int i = 0; i < n; i++) {
      options[i] = toOption(caplets[i], curves);
    }
    return options;
  }

  /**
   * Turn a caplet/floorlet (as a {@link CapFloorIbor}) into a {@link SimpleOptionData}
   * @param caplet The caplet/floorlet
   * @param curves The yield curves
   * @return a {@link SimpleOptionData}
   */
  public static SimpleOptionData toOption(final CapFloorIbor caplet, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(caplet, "caplet");
    ArgumentChecker.notNull(curves, "null yield curves");

    double fwd = caplet.accept(PRC, curves);
    double t = caplet.getFixingTime();
    // Vol is at fixing time, discounting from payment. This included the year fraction
    double df = curves.getDiscountFactor(caplet.getCurrency(), caplet.getPaymentTime()) *
        caplet.getPaymentYearFraction();
    SimpleOptionData options = new SimpleOptionData(fwd, caplet.getStrike(), t, df, caplet.isCap());
    return options;
  }

}
