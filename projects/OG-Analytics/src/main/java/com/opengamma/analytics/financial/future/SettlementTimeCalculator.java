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

/**
 * 
 */
public final class SettlementTimeCalculator extends InstrumentDerivativeVisitorAdapter<Object, Double> {
  private static final SettlementTimeCalculator INSTANCE = new SettlementTimeCalculator();

  public static SettlementTimeCalculator getInstance() {
    return INSTANCE;
  }

  private SettlementTimeCalculator() {
  }

  @Override
  public Double visitAgricultureFuture(final AgricultureFuture future) {
    return future.getExpiry();
  }

  @Override
  public Double visitEnergyFuture(final EnergyFuture future) {
    return future.getExpiry();
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future) {
    return future.getTimeToSettlement();
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future) {
    return future.getTimeToSettlement();
  }

  @Override
  public Double visitMetalFuture(final MetalFuture future) {
    return future.getExpiry();
  }
}
