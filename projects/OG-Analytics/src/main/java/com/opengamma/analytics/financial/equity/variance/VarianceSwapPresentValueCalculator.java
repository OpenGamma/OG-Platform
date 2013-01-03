/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class VarianceSwapPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {

  private static final VarianceSwapPresentValueCalculator s_instance = new VarianceSwapPresentValueCalculator();
  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication();

  public static VarianceSwapPresentValueCalculator getInstance() {
    return s_instance;
  }

  public VarianceSwapPresentValueCalculator() {
  }

  @Override
  public Double visitVarianceSwap(final VarianceSwap derivative, final StaticReplicationDataBundle market) {
    ArgumentChecker.notNull(market, "market");
    ArgumentChecker.notNull(derivative, "derivative");
    return PRICER.presentValue(derivative, market);
  }

  @Override
  public Double visitEquityVarianceSwap(final EquityVarianceSwap derivative, final StaticReplicationDataBundle market) {
    ArgumentChecker.notNull(market, "market");
    ArgumentChecker.notNull(derivative, "derivative");
    return PRICER.presentValue(derivative, market);
  }
}
