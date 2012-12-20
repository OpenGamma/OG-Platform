/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;

/**
 * Convert from a specialized future to a simple one.
 */
public final class SimpleFutureConverter extends AbstractInstrumentDerivativeVisitor<Object, SimpleFuture> {

  private static SimpleFutureConverter s_instance;

  /**
   * Class to convert between Commodity future objects and SimpleFuture objects
   */
  private SimpleFutureConverter() {
  }

  public static SimpleFutureConverter getInstance() {
    if (s_instance == null) {
      s_instance = new SimpleFutureConverter();
    }
    return s_instance;
  }

  @Override
  public SimpleFuture visitAgricultureFuture(AgricultureFuture visitor) {
    return new SimpleFuture(visitor.getExpiry(), visitor.getSettlement(), visitor.getReferencePrice(), visitor.getUnitAmount(), visitor.getCurrency());
  }

  @Override
  public SimpleFuture visitEnergyFuture(EnergyFuture visitor) {
    return new SimpleFuture(visitor.getExpiry(), visitor.getSettlement(), visitor.getReferencePrice(), visitor.getUnitAmount(), visitor.getCurrency());
  }

  @Override
  public SimpleFuture visitMetalFuture(MetalFuture visitor) {
    return new SimpleFuture(visitor.getExpiry(), visitor.getSettlement(), visitor.getReferencePrice(), visitor.getUnitAmount(), visitor.getCurrency());
  }
}
