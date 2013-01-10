/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the greeks of a commodity future option using the Barone-Adesi Whaley model {@link BaroneAdesiWhaleyModel}.
 * <p>
 * The greeks returned are delta, dual-delta, rho, carry rho, theta and vega.
 */
public final class EqyOptBaroneAdesiWhaleyGreekCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, GreekResultCollection> {
  /** A static instance of this calculator */
  private static final EqyOptBaroneAdesiWhaleyGreekCalculator INSTANCE = new EqyOptBaroneAdesiWhaleyGreekCalculator();
  /** The pricing model */
  private static final BaroneAdesiWhaleyModel MODEL = new BaroneAdesiWhaleyModel();

  /**
   * @return A static instance of this class
   */
  public static EqyOptBaroneAdesiWhaleyGreekCalculator getInstance() {
    return INSTANCE;
  }

  private EqyOptBaroneAdesiWhaleyGreekCalculator() {
  }

  @Override
  public GreekResultCollection visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double b = r; //TODO
    final double volatility = data.getVolatilitySurface().getVolatility(t, k);
    final boolean isCall = option.isCall();
    final double notional = option.getUnitAmount();
    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, b, t, volatility, isCall);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, notional * greeks[1]);
    result.put(Greek.DUAL_DELTA, notional * greeks[2]);
    result.put(Greek.RHO, notional * greeks[3]);
    result.put(Greek.CARRY_RHO, notional * greeks[4]);
    result.put(Greek.THETA, notional * greeks[5]);
    result.put(Greek.VEGA, notional * greeks[6]);

    final double[] pdg = MODEL.getPriceDeltaGamma(s, k, r, b, volatility, s, isCall);
    result.put(Greek.GAMMA, notional * pdg[2]);
    return result;
  }

}
