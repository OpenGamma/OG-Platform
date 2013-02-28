/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFXFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrument;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrumentVisitor;
import com.opengamma.util.money.CurrencyAmount;

/**
 * @deprecated This should be removed when SimpleFuturePresentValueFunctionDeprecated is
 */
@Deprecated
public class SimpleFuturePresentValueCalculatorDeprecated implements SimpleInstrumentVisitor<SimpleFutureDataBundleDeprecated, CurrencyAmount> {

  @Override
  public CurrencyAmount visit(final SimpleInstrument derivative, final SimpleFutureDataBundleDeprecated data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public CurrencyAmount visitSimpleFuture(final SimpleFuture future, final SimpleFutureDataBundleDeprecated data) {
    final double t = future.getExpiry();
    return CurrencyAmount.of(future.getCurrency(), future.getUnitAmount() * data.getSpotValue() * Math.exp(t * (data.getFundingCurve().getInterestRate(t) - data.getCostOfCarry())));
  }

  @Override
  public CurrencyAmount visitSimpleFXFuture(final SimpleFXFuture future, final SimpleFutureDataBundleDeprecated data) {
    throw new UnsupportedOperationException("Cannot price simple FX future with this calculator");
  }

  @Override
  public CurrencyAmount visit(final SimpleInstrument derivative) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

  @Override
  public CurrencyAmount visitSimpleFuture(final SimpleFuture future) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

  @Override
  public CurrencyAmount visitSimpleFXFuture(final SimpleFXFuture future) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

}
