/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculator;

import com.opengamma.analytics.financial.commodity.CommodityFutureOptionSameMethodVisitorAdapter;
import com.opengamma.analytics.financial.commodity.derivative.CommodityFutureOption;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the price of a commodity future option using the Bjerksund Stensland model {@link BjerksundStenslandModel}.
 * <p>
 * The greeks returned are delta, dual-delta, rho, carry rho, theta and vega.
 */
public final class ComFutOptBjerksundStenslandPresentValueCalculator extends CommodityFutureOptionSameMethodVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** A static instance of this calculator */
  private static final ComFutOptBjerksundStenslandPresentValueCalculator INSTANCE = new ComFutOptBjerksundStenslandPresentValueCalculator();
  /** The pricing model */
  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  /**
   * @return A static instance of this class
   */
  public static ComFutOptBjerksundStenslandPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ComFutOptBjerksundStenslandPresentValueCalculator() {
  }

  @Override
  public Double visit(final CommodityFutureOption<?> option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double b = r; //TODO
    final double volatility = data.getVolatilitySurface().getVolatility(t, k);
    final boolean isCall = option.isCall();
    final double[] result = MODEL.getPriceDeltaGamma(s, k, r, b, t, volatility, isCall);
    return result[0];
  }

}
