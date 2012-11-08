/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.equity.AbstractDerivativeVisitor;
import com.opengamma.analytics.financial.equity.Derivative;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.pricing.EquityFutureMarkToMarket;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;

/**
 * Present value calculator for futures on Equity underlying assets
 */
public final class EquityFuturesPresentValueCalculator extends AbstractDerivativeVisitor<SimpleFutureDataBundle, Double> {

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
  public Double visit(final Derivative derivative, final SimpleFutureDataBundle dataBundle) {
    Validate.notNull(derivative);
    Validate.notNull(dataBundle);
    Validate.notNull(dataBundle.getMarketPrice());
    return derivative.accept(this, dataBundle);
  }

  @Override
  public Double visit(final Derivative derivative) {
    Validate.notNull(derivative);
    return derivative.accept(this);
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
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
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final SimpleFutureDataBundle dataBundle) {
    return visitEquityFuture(future, dataBundle);
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support futures without a mktPrice");
  }

  @Override
  public Double visitVarianceSwap(final VarianceSwap derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitVarianceSwap(). Try VarianceSwapPresentValueCalculator");
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption equityIndexOption, final SimpleFutureDataBundle data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass()
        + ") does not support visitEquityIndexOption(). Try EquityIndexOptionPresentValueCalculator");
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption equityIndexOption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEquityIndexOption(). It also requires an DataBundle.");
  }

}
