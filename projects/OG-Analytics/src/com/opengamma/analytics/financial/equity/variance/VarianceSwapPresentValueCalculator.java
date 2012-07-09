/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.equity.AbstractDerivativeVisitor;
import com.opengamma.analytics.financial.equity.Derivative;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;

/**
 * 
 */
public class VarianceSwapPresentValueCalculator extends AbstractDerivativeVisitor<StaticReplicationDataBundle, Double> {

  private static final VarianceSwapPresentValueCalculator s_instance = new VarianceSwapPresentValueCalculator();
  private static final VarianceSwapStaticReplication PRICER = new VarianceSwapStaticReplication();

  public static VarianceSwapPresentValueCalculator getInstance() {
    return s_instance;
  }

  public VarianceSwapPresentValueCalculator() {
  }

  @Override
  public Double visit(Derivative derivative) {
    return null;
  }

  @Override
  public Double visitVarianceSwap(final VarianceSwap derivative, final StaticReplicationDataBundle market) {
    Validate.notNull(market);
    Validate.notNull(derivative);
    return PRICER.presentValue(derivative, market);
  }

  @Override
  public Double visitVarianceSwap(final VarianceSwap derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support a VarianceSwap without a EquityOptionDataBundle");
  }

  @Override
  public Double visitEquityFuture(EquityFuture equityFuture) {
    return null;
  }

  @Override
  public Double visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture) {
    return null;
  }

  @Override
  public Double visitEquityIndexOption(EquityIndexOption equityIndexOption, StaticReplicationDataBundle data) {
    return null;
  }

  @Override
  public Double visitEquityIndexOption(EquityIndexOption equityIndexOption) {
    return null;
  }

}
