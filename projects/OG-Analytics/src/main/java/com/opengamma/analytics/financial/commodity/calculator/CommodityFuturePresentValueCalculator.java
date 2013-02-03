/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculator;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.commodity.derivative.SimpleFutureConverter;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFXFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrument;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrumentVisitor;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Class for pricing commodity futures
 *
 * Computes PV as the difference between Live and last day's closing prices
 *
 */
public class CommodityFuturePresentValueCalculator implements SimpleInstrumentVisitor<SimpleFutureDataBundle, Double> {

  /** need to convert to SimpleFuture so we can use existing machinery */
  private static final SimpleFutureConverter SIMPLE_FUTURE_CONVERTER = SimpleFutureConverter.getInstance();

  /**
   *
   */
  public CommodityFuturePresentValueCalculator() {
  }

  /**
   * Main visitor entry point, takes a derivative and market data and returns the current value
   *
   * @param derivative the derivative to price
   * @param data market data
   * @return the current value
   */
  public Double visit(AgricultureFuture derivative, SimpleFutureDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return SIMPLE_FUTURE_CONVERTER.visitAgricultureFuture(derivative).accept(this, data);
  }

  /**
   * Main visitor entry point, takes a derivative and market data and returns the current value
   *
   * @param derivative the derivative to price
   * @param data market data
   * @return the current value
   */
  public Double visit(EnergyFuture derivative, SimpleFutureDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return SIMPLE_FUTURE_CONVERTER.visitEnergyFuture(derivative).accept(this, data);
  }

  /**
   * Main visitor entry point, takes a derivative and market data and returns the current value
   *
   * @param derivative the derivative to price
   * @param data market data
   * @return the current value
   */
  public Double visit(MetalFuture derivative, SimpleFutureDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return SIMPLE_FUTURE_CONVERTER.visitMetalFuture(derivative).accept(this, data);
  }

  @Override
  public Double visit(SimpleInstrument derivative, SimpleFutureDataBundle data) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  /**
   * Compute PV as the difference between Live and last day's closing prices
   */
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
