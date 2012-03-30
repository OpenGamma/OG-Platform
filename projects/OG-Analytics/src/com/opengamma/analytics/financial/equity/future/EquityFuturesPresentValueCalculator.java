/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.equity.AbstractEquityDerivativeVisitor;
import com.opengamma.analytics.financial.equity.EquityDerivative;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.pricing.EquityFutureMarkToMarket;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;

/**
 * Present value calculator for futures on Equity underlying assets
 */
public final class EquityFuturesPresentValueCalculator extends AbstractEquityDerivativeVisitor<EquityFutureDataBundle, Double> {

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
  public Double visit(final EquityDerivative derivative, final EquityFutureDataBundle dataBundle) {
    Validate.notNull(derivative);
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getMarketPrice());
    return derivative.accept(this, dataBundle);
  }

  @Override
  public Double visit(final EquityDerivative derivative) {
    Validate.notNull(derivative);
    return derivative.accept(this);
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future, final EquityFutureDataBundle dataBundle) {
    Validate.notNull(future);
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getMarketPrice());
    return EquityFutureMarkToMarket.getInstance().presentValue(future, dataBundle);
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support futures without a mktPrice");
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final EquityFutureDataBundle dataBundle) {
    return visitEquityFuture(future, dataBundle);
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
    return visitEquityFuture(future);
  }

  @Override
  public Double visitVarianceSwap(VarianceSwap derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitVarianceSwap(). Try VarianceSwapPresentValueCalculator");
  }

}
