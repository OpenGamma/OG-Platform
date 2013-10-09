/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculator;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the vega of commodity future options using the Black method.
 */
public final class CommodityFutureOptionBlackVegaCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** A static instance of this calculator */
  private static final CommodityFutureOptionBlackVegaCalculator s_instance = new CommodityFutureOptionBlackVegaCalculator();
  /** The Black pricer */
  private static final CommodityFutureOptionBlackMethod PRICER = CommodityFutureOptionBlackMethod.getInstance();

  /**
   * @return The static instance of this calculator
   */
  public static CommodityFutureOptionBlackVegaCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor.
   */
  private CommodityFutureOptionBlackVegaCalculator() {
  }

  @Override
  public Double visitAgricultureFutureOption(final AgricultureFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return PRICER.vega(derivative, data);
  }

  @Override
  public Double visitEnergyFutureOption(final EnergyFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return PRICER.vega(derivative, data);
  }

  @Override
  public Double visitMetalFutureOption(final MetalFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return PRICER.vega(derivative, data);
  }

}
