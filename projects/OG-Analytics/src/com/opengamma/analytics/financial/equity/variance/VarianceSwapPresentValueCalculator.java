/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import com.opengamma.analytics.financial.equity.AbstractEquityDerivativeVisitor;
import com.opengamma.analytics.financial.equity.EquityDerivative;
import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class VarianceSwapPresentValueCalculator extends AbstractEquityDerivativeVisitor<EquityOptionDataBundle, Double> {

  private static final VarianceSwapPresentValueCalculator s_instance = new VarianceSwapPresentValueCalculator();
  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication();

  public static VarianceSwapPresentValueCalculator getInstance() {
    return s_instance;
  }

  public VarianceSwapPresentValueCalculator() {
  }

  @Override
  public Double visitVarianceSwap(final VarianceSwap derivative, final EquityOptionDataBundle market) {
    Validate.notNull(market);
    Validate.notNull(derivative);
    return PRICER.presentValue(derivative, market);
  }

  @Override
  public Double visitVarianceSwap(final VarianceSwap derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support a VarianceSwap without a EquityOptionDataBundle");
  }

  @Override
  public Double visit(EquityDerivative derivative) {
    if (derivative instanceof VarianceSwap) {
      return visitVarianceSwap((VarianceSwap) derivative);
    }
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(EquityDerivative). See Type Hierarchy of EquityDerivativeVisitor.");
  }

  @Override
  public Double visitEquityFuture(EquityFuture equityFuture) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEquityFuture(). Try EquityFuturesPresentValueCalculator");
  }

  @Override
  public Double visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEquityIndexDividendFuture(). Try EquityFuturesPresentValueCalculator");
  }

  @Override
  public Double visitEquityIndexOption(EquityIndexOption equityIndexOption, EquityOptionDataBundle data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEquityIndexOption(). Try EquityIndexOptionPresentValueCalculator");
  }

  @Override
  public Double visitEquityIndexOption(EquityIndexOption equityIndexOption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEquityIndexOption(). Try EquityIndexOptionPresentValueCalculator");
  }

}
