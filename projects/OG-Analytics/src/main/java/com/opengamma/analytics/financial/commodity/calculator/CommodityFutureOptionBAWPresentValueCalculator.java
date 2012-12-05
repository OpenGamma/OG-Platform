/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculator;

import com.opengamma.analytics.financial.commodity.CommodityFutureOptionSameMethodVisitorAdapter;
import com.opengamma.analytics.financial.commodity.derivative.CommodityFutureOption;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of a commodity future option using the Barone-Adesi Whaley model {@see BaroneAdesiWhaleyModel}.
 */
public final class CommodityFutureOptionBAWPresentValueCalculator extends CommodityFutureOptionSameMethodVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** Static instance of this calculator */
  private static final CommodityFutureOptionBAWPresentValueCalculator INSTANCE = new CommodityFutureOptionBAWPresentValueCalculator();
  /** The pricing model */
  private static final BaroneAdesiWhaleyModel MODEL = new BaroneAdesiWhaleyModel();

  /**
   * @return A static instance of this class
   */
  public static CommodityFutureOptionBAWPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  private CommodityFutureOptionBAWPresentValueCalculator() {
  }

  @Override
  public Double visit(final CommodityFutureOption<?> option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double b = 0; //TODO
    final double volatility = data.getVolatilitySurface().getVolatility(t, k);
    final boolean isCall = option.isCall();
    return MODEL.price(s, k, r, b, t, volatility, isCall);
  }

}
