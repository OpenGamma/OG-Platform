/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import com.opengamma.analytics.financial.equity.AbstractEquityDerivativeVisitor;
import com.opengamma.analytics.financial.equity.EquityDerivative;
import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public final class EquityIndexOptionPresentValueCalculator extends AbstractEquityDerivativeVisitor<EquityOptionDataBundle, Double> {

  private static final EquityIndexOptionPresentValueCalculator s_instance = new EquityIndexOptionPresentValueCalculator();
  private static final EquityIndexOptionBlackMethod PRICER = EquityIndexOptionBlackMethod.getInstance();

  public static EquityIndexOptionPresentValueCalculator getInstance() {
    return s_instance;
  }

  private EquityIndexOptionPresentValueCalculator() {
  }

  @Override
  /** The meat of this class 
   * @param derivative The OG-Analytics form, in terms of time, not calendars
   * @param market The market curve bundle includes BlackVolatilitySurface, Forward Equity Curve, and Funding Curve
   * @return The fair value of the option
   */
  public Double visitEquityIndexOption(EquityIndexOption derivative, EquityOptionDataBundle market) {
    Validate.notNull(market);
    Validate.notNull(derivative);
    return PRICER.presentValue(derivative, market);
  }

  @Override
  public Double visit(final EquityDerivative derivative, final EquityOptionDataBundle data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public Double visit(EquityDerivative derivative) {
    if (derivative instanceof EquityIndexOption) {
      return visitEquityIndexOption((EquityIndexOption) derivative);
    }
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visit(EquityFuture).");
  }

  @Override
  public Double visitEquityIndexOption(EquityIndexOption equityIndexOption) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support an EquityIndexOption without an EquityOptionDataBundle");
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
  public Double visitVarianceSwap(VarianceSwap derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitVarianceSwap(). Try VarianceSwapPresentValueCalculator");
  }

}
