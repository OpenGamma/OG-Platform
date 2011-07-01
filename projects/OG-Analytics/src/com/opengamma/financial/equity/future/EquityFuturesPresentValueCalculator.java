/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future;

import com.opengamma.financial.equity.AbstractEquityDerivativeVisitor;
import com.opengamma.financial.equity.EquityDerivative;
import com.opengamma.financial.equity.future.derivative.EquityFuture;
import com.opengamma.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.financial.equity.future.pricing.EquityFutureMarkToMarket;
import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;

import org.apache.commons.lang.Validate;

/**
 * Present value calculator for futures on Equity underlying assets
 */
public final class EquityFuturesPresentValueCalculator extends AbstractEquityDerivativeVisitor<Double, Double> {

  private static final EquityFuturesPresentValueCalculator s_instance = new EquityFuturesPresentValueCalculator();

  public static EquityFuturesPresentValueCalculator getInstance() {
    return s_instance;
  }

  private EquityFuturesPresentValueCalculator() {
  }

  @Override
  public Double visit(final EquityDerivative derivative, final Double mktPrice) {
    Validate.notNull(mktPrice);
    Validate.notNull(derivative);
    return derivative.accept(this, mktPrice);
  }

  @Override
  public Double visit(final EquityDerivative derivative) {
    Validate.notNull(derivative);
    return derivative.accept(this);
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future, final Double mktPrice) {
    Validate.notNull(mktPrice);
    Validate.notNull(future);
    return EquityFutureMarkToMarket.presentValue(future, mktPrice);
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support futures without a mktPrice");
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final Double mktPrice) {
    return visitEquityFuture(future, mktPrice);
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
