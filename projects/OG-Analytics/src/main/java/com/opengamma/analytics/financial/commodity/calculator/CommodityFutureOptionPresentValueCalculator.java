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
 *
 */
public class CommodityFutureOptionPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {

  private static final CommodityFutureOptionPresentValueCalculator s_instance = new CommodityFutureOptionPresentValueCalculator();
  private static final CommodityFutureOptionBlackMethod PRICER = CommodityFutureOptionBlackMethod.getInstance();

  public static CommodityFutureOptionPresentValueCalculator getInstance() {
    return s_instance;
  }

  /**
   *
   */
  public CommodityFutureOptionPresentValueCalculator() {
  }

  @Override
  public Double visitAgricultureFutureOption(final AgricultureFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return PRICER.presentValue(derivative, data);
  }

  @Override
  public Double visitEnergyFutureOption(final EnergyFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return PRICER.presentValue(derivative, data);
  }

  @Override
  public Double visitMetalFutureOption(final MetalFutureOption derivative, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return PRICER.presentValue(derivative, data);
  }

  @Override
  public Double visitAgricultureFutureOption(final AgricultureFutureOption derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support an visitAgricultureFutureOption without a StaticReplicationDataBundle");
  }

  @Override
  public Double visitEnergyFutureOption(final EnergyFutureOption derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support an visitEnergyFutureOption without a StaticReplicationDataBundle");
  }

  @Override
  public Double visitMetalFutureOption(final MetalFutureOption derivative) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support an visitMetalFutureOption without a StaticReplicationDataBundle");
  }

}
