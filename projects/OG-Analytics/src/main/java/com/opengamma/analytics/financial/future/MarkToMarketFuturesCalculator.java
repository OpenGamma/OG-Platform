/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.future;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.util.ArgumentChecker;


/**
 * 
 */
public abstract class MarkToMarketFuturesCalculator extends InstrumentDerivativeVisitorAdapter<SimpleFutureDataBundle, Double> {

  @Override
  public Double visitAgricultureFuture(final AgricultureFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return getPresentValue(dataBundle, future.getReferencePrice(), future.getUnitAmount());
  }

  @Override
  public Double visitEnergyFuture(final EnergyFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return getPresentValue(dataBundle, future.getReferencePrice(), future.getUnitAmount());
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return getPresentValue(dataBundle, future.getStrike(), future.getUnitAmount());
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return getPresentValue(dataBundle, future.getStrike(), future.getUnitAmount());
  }

  @Override
  public Double visitMetalFuture(final MetalFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return getPresentValue(dataBundle, future.getReferencePrice(), future.getUnitAmount());
  }

  protected abstract double getPresentValue(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount);

  /**
   * Calculates the present value
   */
  public static final class PresentValueCalculator extends MarkToMarketFuturesCalculator {

    @Override
    protected double getPresentValue(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount) {
      return (dataBundle.getMarketPrice() - strike) * unitAmount;
    }

  }
}
