/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity;

import com.opengamma.financial.equity.future.derivative.EquityFuture;
import com.opengamma.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.financial.equity.future.pricing.EquityFutureMarkToMarket;

import org.apache.commons.lang.Validate;

/**
 * TODO: Case - Review 2nd argument. In IR, it's YieldCurveBundle. Here I've put mktPrice for now..
 */
public final class PresentValueCalculator extends AbstractEquityDerivativeVisitor<Double, Double> {

  private static final PresentValueCalculator s_instance = new PresentValueCalculator();

  public static PresentValueCalculator getInstance() {
    return s_instance;
  }

  private PresentValueCalculator() {
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
    Validate.notNull(future);
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support Futures without a mktPrice");
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final Double mktPrice) {
    return visitEquityFuture(future, mktPrice);
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
    return visitEquityFuture(future);
  }

}
