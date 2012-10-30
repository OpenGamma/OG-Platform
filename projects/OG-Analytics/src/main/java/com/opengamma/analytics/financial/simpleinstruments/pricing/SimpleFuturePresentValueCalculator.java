/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.pricing;

import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFXFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrument;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrumentVisitor;

import org.apache.commons.lang.Validate;

/** Computes PV as the difference between Live and last day's closing prices */
public class SimpleFuturePresentValueCalculator implements SimpleInstrumentVisitor<SimpleFutureDataBundle, Double> {

  @Override
  public Double visit(SimpleInstrument derivative, SimpleFutureDataBundle data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public Double visitSimpleFuture(SimpleFuture future, SimpleFutureDataBundle data) {
    return future.getUnitAmount() * (data.getMarketPrice() - future.getReferencePrice());
  }

  @Override
  public Double visitSimpleFXFuture(SimpleFXFuture future, SimpleFutureDataBundle data) {
    throw new UnsupportedOperationException("Cannot price simple FX future with this calculator");
  }

  @Override
  public Double visit(final SimpleInstrument derivative) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

  @Override
  public Double visitSimpleFuture(final SimpleFuture future) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

  @Override
  public Double visitSimpleFXFuture(final SimpleFXFuture future) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

}
