/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.pricing.EquityFutureMarkToMarket;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Present value calculator for futures on Equity underlying assets
 */
public final class EquityFuturesPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<SimpleFutureDataBundle, Double> {

  private static final EquityFuturesPresentValueCalculator s_instance = new EquityFuturesPresentValueCalculator();

  /**
   * FIXME Extend EquityFuturesPresentValueCalculator to each of the EquityFuturesPricingMethod's
   * @return Singleton instance of the presentValueCalculator
   */
  public static EquityFuturesPresentValueCalculator getInstance() {
    return s_instance;
  }

  private EquityFuturesPresentValueCalculator() {
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    ArgumentChecker.notNull(dataBundle.getMarketPrice(), "market price");
    return EquityFutureMarkToMarket.getInstance().presentValue(future, dataBundle);
  }

  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    ArgumentChecker.notNull(dataBundle.getMarketPrice(), "market price");
    return EquityFutureMarkToMarket.getInstance().presentValue(future, dataBundle);
  }
}
