/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;

/**
 * Convert from a specialized future to a simple one.
 */
public final class SimpleFutureConverter extends InstrumentDerivativeVisitorAdapter<Object, SimpleFuture> {
  /** The converter */
  private static SimpleFutureConverter s_instance;

  /**
   * Class to convert between Commodity future objects and SimpleFuture objects
   */
  private SimpleFutureConverter() {
  }

  /**
   * @return A static instance of this converter
   */
  public static SimpleFutureConverter getInstance() {
    if (s_instance == null) {
      s_instance = new SimpleFutureConverter();
    }
    return s_instance;
  }

  @Override
  public SimpleFuture visitAgricultureFuture(final AgricultureFuture visitor) {
    return new SimpleFuture(visitor.getExpiry(), visitor.getSettlement(), visitor.getReferencePrice(), visitor.getUnitAmount(), visitor.getCurrency());
  }

  @Override
  public SimpleFuture visitEnergyFuture(final EnergyFuture visitor) {
    return new SimpleFuture(visitor.getExpiry(), visitor.getSettlement(), visitor.getReferencePrice(), visitor.getUnitAmount(), visitor.getCurrency());
  }

  @Override
  public SimpleFuture visitMetalFuture(final MetalFuture visitor) {
    return new SimpleFuture(visitor.getExpiry(), visitor.getSettlement(), visitor.getReferencePrice(), visitor.getUnitAmount(), visitor.getCurrency());
  }

}
