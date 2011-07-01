/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity;

import com.opengamma.financial.equity.future.derivative.EquityFuture;
import com.opengamma.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;

import org.apache.commons.lang.Validate;

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public abstract class AbstractEquityDerivativeVisitor<S, T> implements EquityDerivativeVisitor<S, T> {

  @Override
  public T visit(final EquityDerivative derivative, final S data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public T visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEquityIndexDividendFuture()");
  }

  @Override
  public T visitEquityFuture(final EquityFuture future, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitEquityFuture()");
  }

  @Override
  public T visitVarianceSwap(VarianceSwap derivative, final S data) {
    throw new UnsupportedOperationException("This visitor (" + this.getClass() + ") does not support visitVarianceSwap()");
  }

}
